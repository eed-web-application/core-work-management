/*
 * -----------------------------------------------------------------------------
 * Title      : LOVService
 * ----------------------------------------------------------------------------
 * File       : LOVService.java
 * Author     : Claudio Bisegni, bisegni@slac.stanford.edu
 * ----------------------------------------------------------------------------
 * This file is part of core-work-management. It is subject to
 * the license terms in the LICENSE.txt file found in the top-level directory
 * of this distribution and at:
 * <a href="https://confluence.slac.stanford.edu/display/ppareg/LICENSE.html"/>.
 * No part of core-work-management, including this file, may be
 * copied, modified, propagated, or distributed except according to the terms
 *  contained in the LICENSE.txt file.
 * ----------------------------------------------------------------------------
 */

package edu.stanford.slac.core_work_management.service;

import edu.stanford.slac.core_work_management.api.v1.dto.*;
import edu.stanford.slac.core_work_management.api.v1.mapper.LOVMapper;
import edu.stanford.slac.core_work_management.exception.LOVValueNotFound;
import edu.stanford.slac.core_work_management.model.BucketSlot;
import edu.stanford.slac.core_work_management.model.LOVElement;
import edu.stanford.slac.core_work_management.model.value.LOVField;
import edu.stanford.slac.core_work_management.exception.LOVFieldReferenceNotFound;
import edu.stanford.slac.core_work_management.model.Work;
import edu.stanford.slac.core_work_management.repository.LOVElementRepository;
import edu.stanford.slac.core_work_management.repository.WorkTypeRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.of;
import static edu.stanford.slac.ad.eed.baselib.exception.Utility.assertion;
import static edu.stanford.slac.ad.eed.baselib.exception.Utility.wrapCatch;
import static java.util.Map.*;

@Service
@Validated
@AllArgsConstructor
public class LOVService {
    private final LOVMapper lovMapper;
    private final WorkTypeRepository workTypeRepository;
    private final LOVElementRepository lovElementRepository;


    public List<String> createNew(
            @NotEmpty String groupName,
            @Valid List<NewLOVElementDTO> lovElementDTOs
    ) {
        return lovElementDTOs.stream()
                .map(e -> lovMapper.toModelByGroupName(groupName, e))
                .map(newElement -> wrapCatch(
                        () -> lovElementRepository.save(newElement),
                        -1
                ).getId())
                .collect(Collectors.toList());
    }

    /**
     * Create a new LOV element
     *
     * @param lovDomainDTO the domain of the LOV element
     * @param subtypeId    the subtype id
     * @param fieldName    the name of the field to associate
     * @param groupName    the list new LOV element
     */
    public void associateDomainFieldToGroupName(
            @NotNull LOVDomainTypeDTO lovDomainDTO,
            String domainId,
            String subtypeId,
            @NotEmpty String fieldName,
            @NotEmpty String groupName
    ) {
        var fieldReferences = getLOVFieldReference(lovDomainDTO, domainId, subtypeId);
        assertion(
                LOVFieldReferenceNotFound.byFieldName().errorCode(-1).fieldName(fieldName).build(),
                () -> fieldReferences.containsKey(fieldName)
        );
        addFieldReferenceToGroupName(
                groupName,
                of(fieldReferences.get(fieldName))
        );
    }

    /**
     * Find all the LOV elements by domain and field reference
     *
     * @param lovDomainDTO the domain of the LOV element
     * @param fieldName    the field name
     * @return the list of LOV elements
     */
    public List<LOVElementDTO> findAllByDomainAndFieldName(LOVDomainTypeDTO lovDomainDTO, String domainId, String subtypeId, String fieldName) {
        var fieldReferences = getLOVFieldReference(lovDomainDTO, domainId, subtypeId);
        assertion(
                LOVFieldReferenceNotFound.byFieldName().errorCode(-1).fieldName(fieldName).build(),
                () -> fieldReferences.containsKey(fieldName)
        );
        return wrapCatch(
                () -> lovElementRepository.findByFieldReferenceContains(
                                fieldReferences.get(fieldName)
                        )
                        .stream()
                        .map(lovMapper::toDTO).toList(),
                -1
        );
    }

    /**
     * Find all the LOV elements by field reference
     * return all the possible value that a field identified by field reference can use
     *
     * @param fieldReference the field reference
     * @return the list of LOV elements
     */
    public List<LOVElementDTO> findAllByFieldReference(String fieldReference) {
        return wrapCatch(
                () -> lovElementRepository.findByFieldReferenceContains(fieldReference)
                        .stream()
                        .map(lovMapper::toDTO).toList(),
                -1
        );
    }

    /**
     * Find all the LOV elements by domain and field reference
     *
     * @param groupName the group name of the LOV elements
     * @return the list of LOV elements
     */
    public List<LOVElementDTO> findAllByGroupName(String groupName) {
        return wrapCatch(
                () -> lovElementRepository.findByGroupNameIs(
                                groupName
                        )
                        .stream()
                        .map(lovMapper::toDTO).toList(),
                -1
        );
    }

    /**
     * Add a field reference to all element of the same group name
     *
     * @param groupName the group name of the LOV elements
     */
    public void addFieldReferenceToGroupName(String groupName, List<String> fieldReference) {
        lovElementRepository.findByGroupNameIs(groupName)
                .forEach(
                        lovElement -> {
                            lovElement.getFieldReference().addAll(fieldReference);
                            wrapCatch(
                                    () -> lovElementRepository.save(lovElement),
                                    -1
                            );
                        }
                );
    }

    /**
     * Remove a field reference to all element of the same group name
     *
     * @param groupName the group name of the LOV elements
     */
    public void removeFieldReferenceFromGroupName(String groupName, List<String> fieldReference) {
        lovElementRepository.findByGroupNameIs(groupName)
                .forEach(
                        lovElement -> {
                            lovElement.getFieldReference().removeAll(fieldReference);
                            wrapCatch(
                                    () -> lovElementRepository.save(lovElement),
                                    -1
                            );
                        }
                );
    }

    /**
     * Find  the LOV value by his unique id
     *
     * @param id the id of the love to retrieve
     * @return the list of LOV elements
     */
    public String findLovValueById(String id) {
        return wrapCatch(
                () -> lovElementRepository.
                        findById(id)
                        .map(LOVElement::getValue)
                        .orElseThrow(
                                () -> LOVValueNotFound
                                        .byId()
                                        .errorCode(-1)
                                        .id(id)
                                        .build()
                        ),
                -1
        );
    }

    /**
     * Find  the LOV value by his unique id
     *
     * @param id the id of the love to retrieve
     * @return the list of LOV elements
     */
    public Optional<LOVElementDTO> findLovValueByIdNoException(String id) {
        return wrapCatch(
                () -> lovElementRepository.findById(id),
                -1
        ).map(lovMapper::toDTO);
    }

    /**
     * Find all the field that are LOV for a specific domain
     *
     * @param lovDomainTypeDTO the domain for which the field reference is needed
     * @return the field reference of the LOV element
     */
    public List<String> findAllLOVField(LOVDomainTypeDTO lovDomainTypeDTO, String domainId, String subtypeId) {
        var allFieldReference = getLOVFieldReference(lovDomainTypeDTO, domainId, subtypeId);
        // check if field reference is attached to some lov
        var onlyLOVMap = allFieldReference.entrySet().stream()
                .filter(entry -> lovElementRepository.existsByFieldReferenceContains(entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return onlyLOVMap.keySet().stream().toList();
    }

    /**
     * Check if a field reference is in use
     *
     * @param fieldReference the field reference to check
     * @return true if the field reference is in use, false otherwise
     */
    public boolean checkIfFieldReferenceIsInUse(String fieldReference) {
        return wrapCatch(
                () -> lovElementRepository.existsByFieldReferenceContains(fieldReference),
                -1
        );
    }

    /**
     * Return a full list of field/lov reference for each domain
     *
     * @param lovDomainDTO the domain for which the field reference is needed
     * @return the field reference of the LOV element
     */
    public HashMap<String, String> getLOVFieldReference(LOVDomainTypeDTO lovDomainDTO, String domainId, String subtypeId) {
        return switch (lovDomainDTO) {
            case LOVDomainTypeDTO.Work -> {
                var resultHash = Arrays.stream(Work.class.getDeclaredFields())
                        .filter(field -> field.isAnnotationPresent(LOVField.class))
                        .collect(Collectors.toMap(
                                Field::getName,
                                field -> field.getAnnotation(LOVField.class).fieldReference(),
                                (existing, replacement) -> existing,
                                HashMap::new
                        ));
                if (subtypeId != null) resultHash.putAll(getLOVFieldReferenceFromWorkType(domainId, subtypeId));
                yield resultHash;
            }
            case Bucket -> {
                var resultHash = Arrays.stream(BucketSlot.class.getDeclaredFields())
                        .filter(field -> field.isAnnotationPresent(LOVField.class))
                        .collect(Collectors.toMap(
                                Field::getName,
                                field -> field.getAnnotation(LOVField.class).fieldReference(),
                                (existing, replacement) -> existing,
                                HashMap::new
                        ));
                yield resultHash;
            }
        };
    }

    /**
     * Return a full list of field/lov reference from work type
     *
     * @param workTypeId the id of the work type
     * @return the field reference of the LOV element
     */
    private Map<String, String> getLOVFieldReferenceFromWorkType(String domainId, String workTypeId) {
        HashMap<String, String> result = new HashMap<>();
        if (workTypeId == null) return result;
        workTypeRepository
                .findByDomainIdAndId(domainId, workTypeId)
                .ifPresent(
                        wt -> {
                            if (wt.getCustomFields() != null) {
                                wt.getCustomFields().forEach(
                                        customField -> {
                                            if (customField.getLovFieldReference() != null) {
                                                result.put(customField.getName(), customField.getLovFieldReference());
                                            }
                                        }
                                );
                            }
                        }
                );
        return result;
    }

    /**
     * Check if a group name exists
     *
     * @param groupName the group name to check
     * @return true if the group name exists, false otherwise
     */
    public Boolean existsByGroupName(String groupName) {
        return wrapCatch(
                () -> lovElementRepository.existsByGroupNameIs(groupName),
                -1
        );
    }
}
