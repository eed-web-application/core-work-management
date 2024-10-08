package edu.stanford.slac.core_work_management.api.v1.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Define a domain")
public record DomainDTO(
        @Schema(description = "The domain id")
        String id,
        @Schema(description = "The domain name")
        String name,
        @Schema(description = "The domain description")
        String description,
        @Schema(description = "The list of the workflows associated with the domain")
        Set<WorkflowDTO> workflows,
        @Schema(description = "The work type status statistics of the domain")
        List<WorkTypeStatusStatisticsDTO> workTypeStatusStatistics,
        @Schema(description = "The created date")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime createdDate,
        @Schema(description = "The user that created ")
        String createdBy,
        @Schema(description = "The last modified date")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime lastModifiedDate,
        @Schema(description = "The user that last modified the domain")
        String lastModifiedBy
) {
}
