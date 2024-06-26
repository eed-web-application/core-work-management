package edu.stanford.slac.core_work_management.exception;

import edu.stanford.slac.ad.eed.baselib.exception.ControllerLogicException;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static edu.stanford.slac.ad.eed.baselib.exception.Utility.getAllMethodInCall;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Domain has not been found")
public class DomainNotFound extends ControllerLogicException {
    @Builder(builderMethodName = "notFoundById")
    public DomainNotFound(Integer errorCode, String id) {
        super(errorCode,
                String.format("The Domain with id '%s' has not been found", id),
                getAllMethodInCall()
        );
    }

    @Builder(builderMethodName = "notFoundByName")
    public DomainNotFound(Integer errorCode, String name, int fakeParam) {
        super(errorCode,
                String.format("The Domain '%s' has not been found", name),
                getAllMethodInCall()
        );
    }
}