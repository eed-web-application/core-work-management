package edu.stanford.slac.core_work_management.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import edu.stanford.slac.core_work_management.api.v1.validator.NullOrNotEmpty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Close Work DTO")
public record ReviewWorkDTO(
        @NullOrNotEmpty(message = "The followUpDescription description must be null or not empty")
        String followUpDescription
) {
}
