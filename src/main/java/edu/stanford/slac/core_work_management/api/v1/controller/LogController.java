package edu.stanford.slac.core_work_management.api.v1.controller;

import edu.stanford.slac.ad.eed.baselib.api.v1.dto.ApiResultResponse;
import edu.stanford.slac.core_work_management.api.v1.dto.NewLogEntry;
import edu.stanford.slac.core_work_management.service.ELogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController()
@RequestMapping("/v1/log")
@AllArgsConstructor
@Profile("elog-support")
@Schema(description = "Set of api for the log entries management")
public class LogController {
    private final ELogService logService;

    @PostMapping(
            path = "/work/{workId}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a log entry")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@workAuthorizationService.checkLoggingOnWork(#authentication, #workId)")
    public ApiResultResponse<Boolean> createWorkLogEntry(
            Authentication authentication,
            @PathVariable("workId") @NotEmpty String workId,
            @ModelAttribute @Valid NewLogEntry entry,
            @RequestPart(value = "files", required = false)
            MultipartFile[] files
    ) {
        // create new log entry
        logService.createNewLogEntry(workId, entry, files);
        return ApiResultResponse.of(true);
    }

    @PostMapping(
            path = "/work/{workId}/activity/{activityId}",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @Operation(summary = "Create a log entry")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@workAuthorizationService.checkLoggingOnActivity(#authentication, #workId, #activityId)")
    public ApiResultResponse<Boolean> createActivityLogEntry(
            Authentication authentication,
            @PathVariable() @NotEmpty String workId,
            @PathVariable() @NotEmpty String activityId,
            @ModelAttribute @Valid NewLogEntry entry,
            @RequestPart(value = "files", required = false)
            MultipartFile[] files
    ) {
        // create new log entry
        logService.createNewLogEntry(workId, activityId, entry, files);
        return ApiResultResponse.of(true);
    }
}
