package io.github.ranpers.linkforge.link.management.adapter.in.web;

import io.github.ranpers.linkforge.link.management.application.LinkManagementAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.management.application.LinkManagementDeniedException;
import io.github.ranpers.linkforge.link.management.application.LinkStateConflictException;
import io.github.ranpers.linkforge.link.management.application.ShortLinkNotFoundException;
import io.github.ranpers.linkforge.link.management.domain.InvalidManagedTargetUrlException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ShortLinkManagementController.class)
public class LinkManagementExceptionHandler {

    @ExceptionHandler(InvalidManagedTargetUrlException.class)
    ProblemDetail invalid(InvalidManagedTargetUrlException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ShortLinkNotFoundException.class)
    ProblemDetail notFound(ShortLinkNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(LinkManagementDeniedException.class)
    ProblemDetail denied(LinkManagementDeniedException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                exception.getMessage()
        );
        detail.setProperty("reasonCode", exception.reasonCode());
        detail.setProperty("decisionId", exception.decisionId());
        return detail;
    }

    @ExceptionHandler(LinkStateConflictException.class)
    ProblemDetail stateConflict(LinkStateConflictException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(LinkManagementAuthorizationUnavailableException.class)
    ProblemDetail iamUnavailable(LinkManagementAuthorizationUnavailableException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "IAM 当前不可用，已拒绝管理短链"
        );
    }
}
