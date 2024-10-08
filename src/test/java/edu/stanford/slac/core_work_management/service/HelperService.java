package edu.stanford.slac.core_work_management.service;

import edu.stanford.slac.core_work_management.api.v1.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Service
public class HelperService {
    @Autowired
    WorkService workService;
    @Autowired
    LOVService lovService;
    @Autowired
    DomainService domainService;

    public List<String> ensureWorkAndActivitiesTypes(String domainId, NewWorkTypeDTO newWorkTypeDTO, List<NewWorkTypeDTO> subWork) {
        List<String> listIds = new ArrayList<>();
        String newWorkTypeId = assertDoesNotThrow(
                () -> domainService.createNew(
                        domainId,
                        newWorkTypeDTO
                )
        );
        assertThat(newWorkTypeId).isNotNull();
        listIds.add(newWorkTypeId);
        return listIds;
    }

    /**
     * Get custom field by name
     */
    public ReadWATypeCustomFieldDTO getCustomFiledByName(WorkTypeDTO workTypeDTO, String customFieldName) {
        return workTypeDTO.customFields().stream()
                .filter(customField -> customField.name().compareToIgnoreCase(customFieldName) == 0)
                .findFirst().orElseThrow();
    }

    /**
     * Fetch work and check status
     */
    public boolean checkStatusOnWork(String domainId, String workId, WorkflowStateDTO state){
        return checkStatusOnWork(domainId, workId, state, null);
    }

    /**
     * Fetch work and check status
     */
    public boolean checkStatusOnWork(String domainId, String workId, WorkflowStateDTO state, String commentContains){
        var foundFullWork =  assertDoesNotThrow(
                ()->workService.findWorkById(domainId, workId, WorkDetailsOptionDTO.builder().build())
        );
        assertThat(foundFullWork).isNotNull();
        return foundFullWork.currentStatus().status().equals(state) &&
                (commentContains == null || foundFullWork.currentStatus().comment().contains(commentContains));
    }

    /**
     * Fetch work and check status and history from latest to oldest status value
     */
    public boolean checkStatusAndHistoryOnWork(String domainId, String workId, List<WorkflowStateDTO> workStatusList){
        var foundFullWork =  assertDoesNotThrow(
                ()->workService.findWorkById(domainId, workId, WorkDetailsOptionDTO.builder().build())
        );
        assertThat(foundFullWork).isNotNull();
        if(workStatusList != null && !workStatusList.isEmpty()) {
            if(foundFullWork.currentStatus().status().equals(workStatusList.getFirst())) {
                for(int idx = 0; idx < foundFullWork.statusHistory().size(); idx++)
                    if (!foundFullWork.statusHistory().get(idx).status().equals(workStatusList.get(idx + 1))) {
                        return false;
                    }
            } else
                return false;
        } else
            return false;
        return true;
    }

    /**
     * Get custom field by name
     */
    public List<LOVElementDTO> getCustomFiledLOVValue(LOVDomainTypeDTO lovDomainDTO, String domainId, String subtypeId, String fieldName) {
        return lovService.findAllByDomainAndFieldName(lovDomainDTO, domainId, subtypeId, fieldName);
    }

    /**
     * Get custom field by name
     */
    public String toString(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return localDateTime.format(formatter);
    }
}
