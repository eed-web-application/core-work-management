package edu.stanford.slac.core_work_management.api.v1.dto;

import java.time.LocalDateTime;

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
@Schema(description = "Define a new activity for an work-plan")
public record LocationDTO(
        @Schema(description = "The unique identifier of the location")
        String id,
        @Schema(description = "The domain where the location is belong to")
        DomainDTO domain,
        @Schema(description = "The unique identifier of the parent location")
        String parentId,
        @Schema(description = "The name of the location")
        String name,
        @Schema(description = "The description of the location")
        String description,
        @Schema(description = "The user id that represent the location manager")
        String locationManagerUserId,
        @Schema(description = "The created date of the location")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime createdDate,
        @Schema(description = "The user that created the location")
        String createdBy,
        @Schema(description = "The last modified date of the location")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime lastModifiedDate,
        @Schema(description = "The user that last modified the location")
        String lastModifiedBy
){}
