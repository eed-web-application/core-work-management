/*
 * -----------------------------------------------------------------------------
 * Title      : LocationService
 * ----------------------------------------------------------------------------
 * File       : LocationService.java
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

import edu.stanford.slac.ad.eed.baselib.exception.PersonNotFound;
import edu.stanford.slac.ad.eed.baselib.service.PeopleGroupService;
import edu.stanford.slac.core_work_management.api.v1.dto.LocationDTO;
import edu.stanford.slac.core_work_management.api.v1.dto.LocationFilterDTO;
import edu.stanford.slac.core_work_management.api.v1.dto.NewLocationDTO;
import edu.stanford.slac.core_work_management.api.v1.dto.NewShopGroupDTO;
import edu.stanford.slac.core_work_management.api.v1.mapper.LocationMapper;
import edu.stanford.slac.core_work_management.api.v1.mapper.ShopGroupMapper;
import edu.stanford.slac.core_work_management.cis_api.dto.InventoryElementDTO;
import edu.stanford.slac.core_work_management.exception.DomainNotFound;
import edu.stanford.slac.core_work_management.exception.LocationNotFound;
import edu.stanford.slac.core_work_management.exception.ShopGroupNotFound;
import edu.stanford.slac.core_work_management.model.Location;
import edu.stanford.slac.core_work_management.model.ShopGroup;
import edu.stanford.slac.core_work_management.repository.ExternalLocationRepository;
import edu.stanford.slac.core_work_management.repository.LocationRepository;
import edu.stanford.slac.core_work_management.repository.ShopGroupRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.*;

/**
 * Service to manage locations
 */
@Log4j2
@Service
@Validated
@AllArgsConstructor
public class LocationService {
    private final DomainService domainService;
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;
    private final ShopGroupService shopGroupService;
    private final PeopleGroupService peopleGroupService;
    private final ExternalLocationRepository externalLocationRepository;

    /**
     * Create a new location
     *
     * @param newLocationDTO the DTO to create the location
     * @return the id of the created location
     */
    public String createNew(@Valid NewLocationDTO newLocationDTO) {
        InventoryElementDTO externalLocationDTO;
        if (
                newLocationDTO.externalLocationIdentifier() != null &&
                        !newLocationDTO.externalLocationIdentifier().isBlank()
        ) {
            // acquire external location info
            externalLocationDTO = externalLocationRepository.getLocationInfo(newLocationDTO.externalLocationIdentifier());
        } else {
            externalLocationDTO = null;
        }
        return saveLocation(locationMapper.toModel(null, newLocationDTO, externalLocationDTO)).getId();
    }

    /**
     * Create a new child location
     *
     * @param newLocationDTO the DTO to create the location
     * @return the id of the created location
     */
    public String createNewChild(String parentId, NewLocationDTO newLocationDTO) {
        InventoryElementDTO externalLocationDTO;
        assertion(
                () -> locationRepository.existsById(parentId),
                LocationNotFound
                        .notFoundById()
                        .errorCode(-1)
                        .locationId(parentId)
                        .build()
        );

        if (
                newLocationDTO.externalLocationIdentifier() != null &&
                        !newLocationDTO.externalLocationIdentifier().isBlank()
        ) {
            // acquire external location info
            externalLocationDTO = externalLocationRepository.getLocationInfo(newLocationDTO.externalLocationIdentifier());
        } else {
            externalLocationDTO = null;
        }
        return saveLocation(locationMapper.toModel(parentId, newLocationDTO, externalLocationDTO)).getId();
    }

    /**
     * Find a location by id
     *
     * @param locationId the id of the location
     * @return the location
     */
    public LocationDTO findById(String locationId) {
        return wrapCatch(
                () -> locationRepository.findById(locationId),
                -1
        )
                .map(locationMapper::toDTO)
                .orElseThrow(
                        () -> LocationNotFound
                                .notFoundById()
                                .errorCode(-1)
                                .locationId(locationId)
                                .build()
                );
    }

    /**
     * Find all locations
     *
     * @param locationFilterDTO the filter to use
     * @return the list of locations
     */
    public List<LocationDTO> findAll(LocationFilterDTO locationFilterDTO) {
        var filter = locationMapper.toModel(locationFilterDTO);
        return wrapCatch(
                () -> locationRepository.findByLocationFilter(filter),
                -1
        )
                .stream()
                .map(locationMapper::toDTO)
                .toList();
    }

    /**
     * Create a new location
     *
     * @param location the DTO to create the shop group
     * @return the id of the created shop group
     */
    private Location saveLocation(@Valid Location location) {
        // check for domain if it is present
        assertion(
                DomainNotFound
                        .notFoundById()
                        .errorCode(-1)
                        .id(location.getDomainId())
                        .build(),
                () -> all(
                        () -> location.getDomainId() != null && !location.getDomainId().isBlank(),
                        () -> domainService.existsById(location.getDomainId())
                )
        );

        // check if are manager exists
        assertion(
                () -> peopleGroupService.findPersonByEMail(location.getLocationManagerUserId()) != null,
                PersonNotFound
                        .personNotFoundBuilder()
                        .errorCode(-2)
                        .email(location.getLocationManagerUserId())
                        .build()
        );

        // save the location
        return wrapCatch(() -> locationRepository.save(location), -3);
    }
}
