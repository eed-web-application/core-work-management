/*
 * -----------------------------------------------------------------------------
 * Title      : LOVController
 * ----------------------------------------------------------------------------
 * File       : LOVController.java
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

package edu.stanford.slac.core_work_management.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.ApiResultResponse;
import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import edu.stanford.slac.core_work_management.api.v1.dto.ActivityTypeSubtypeDTO;
import edu.stanford.slac.core_work_management.api.v1.dto.LOVDomainTypeDTO;
import edu.stanford.slac.core_work_management.api.v1.dto.LOVElementDTO;
import edu.stanford.slac.core_work_management.service.LOVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/v1/lov")
@AllArgsConstructor
@Schema(description = "Set of api for the LOV management")
public class LOVController {
    LOVService lovService;

    @Operation(summary = "Return all the lov values for a work field. for the Bucket domain uses 'bucket' as subtype")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(
            path = "/{domainType}/{subtypeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@baseAuthorizationService.checkAuthenticated(#authentication)")
    public ApiResultResponse<List<String>> findAllFieldThatAreLOV(
            Authentication authentication,
            @NotEmpty @PathVariable LOVDomainTypeDTO domainType,
            @NotEmpty @PathVariable String subtypeId
    ) {
        if(domainType==LOVDomainTypeDTO.Bucket && !subtypeId.equalsIgnoreCase("bucket")) {
            throw ControllerLogicException.builder()
                    .errorMessage("Invalid subtype for Bucket domain, it should be 'bucket'")
                    .errorCode(-1)
                    .errorDomain("LOVController::findAllFieldThatAreLOV")
                    .build();
        }
        return ApiResultResponse.of(lovService.findAllLOVField(domainType, subtypeId));
    }

    @Operation(summary = "Return all the lov values for a work field.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(
            path = "/{domainType}/{subtypeId}/{fieldName}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("@baseAuthorizationService.checkAuthenticated(#authentication)")
    public ApiResultResponse<List<LOVElementDTO>> findValuesByDomainAndFieldName(
            Authentication authentication,
            @PathVariable LOVDomainTypeDTO domainType,
            @NotEmpty @PathVariable String subtypeId,
            @NotEmpty @PathVariable String fieldName
    ) {
        return ApiResultResponse.of(lovService.findAllByDomainAndFieldName(domainType, subtypeId, fieldName));
    }
}
