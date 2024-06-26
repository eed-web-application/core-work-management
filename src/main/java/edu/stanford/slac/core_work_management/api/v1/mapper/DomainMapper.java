package edu.stanford.slac.core_work_management.api.v1.mapper;

import edu.stanford.slac.core_work_management.api.v1.dto.*;
import edu.stanford.slac.core_work_management.exception.WorkTypeNotFound;
import edu.stanford.slac.core_work_management.model.Domain;
import edu.stanford.slac.core_work_management.model.WorkStatusCountStatistics;
import edu.stanford.slac.core_work_management.model.WorkType;
import edu.stanford.slac.core_work_management.repository.WorkTypeRepository;
import edu.stanford.slac.core_work_management.service.StringUtility;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.text.Utilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public abstract class DomainMapper {
    @Autowired
    private WorkTypeRepository workTypeRepository;

    /**
     * Convert a NewDomainDTO to a Domain model
     *
     * @param newDomainDTO the DTO to convert
     * @return the model
     */
    @Mapping(target = "name", source = "name", qualifiedByName = "normalizeName")
    public abstract Domain toModel(NewDomainDTO newDomainDTO);

    /**
     * Convert a Domain model to a DTO
     *
     * @param domain the model to convert
     * @return the DTO
     */
    @Mapping(target = "workTypeStatusStatistics", source = "workTypeStatusStatistics", qualifiedByName = "convertStatistic")
    public abstract DomainDTO toDTO(Domain domain);

    /**
     * Convert a WorkStatusCountStatistics model to a DTO
     *
     * @param model the model to convert
     * @return the DTO
     */
    public abstract WorkStatusCountStatisticsDTO toDTO(WorkStatusCountStatistics model);

    /**
     * Convert a WorkType model to a WorkTypeSummaryDTO
     *
     * @param workType the model to convert
     * @return the DTO
     */
    abstract public WorkTypeSummaryDTO toSummaryDTO(WorkType workType);

    /**
     * Normalize the name of the domain
     *
     * @param name the name to normalize
     * @return the normalized name
     */
    @Named("normalizeName")
    public String modifyName(String name) {
        return name.trim().toLowerCase().replace(" ", "-");
    }

    /**
     * Convert a map of WorkStatusCountStatistics to a map of WorkStatusCountStatisticsDTO
     *
     * @param value the map to convert
     * @return the converted map
     */
    @Named("convertStatistic")
    public List<WorkTypeStatusStatisticsDTO> map(Map<String, List<WorkStatusCountStatistics>> value) {
        List<WorkTypeStatusStatisticsDTO> result = new ArrayList<>();
        if (value == null) return result;
        return value.entrySet().stream()
                .map(
                        entry -> {
                            WorkType workType = workTypeRepository.findById(entry.getKey())
                                    .orElseThrow(() -> WorkTypeNotFound.notFoundById().errorCode(-1).workId(entry.getKey()).build());
                            return WorkTypeStatusStatisticsDTO
                                    .builder()
                                    .workType(toSummaryDTO(workType))
                                    .status(entry.getValue().stream().map(this::toDTO).collect(Collectors.toList()))
                                    .build();
                        }
                )
                .toList();
    }
}