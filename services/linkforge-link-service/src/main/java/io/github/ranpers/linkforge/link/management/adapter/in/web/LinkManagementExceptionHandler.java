package io.github.ranpers.linkforge.link.management.adapter.in.web;

import io.github.ranpers.linkforge.link.management.application.LinkManagementAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.management.application.LinkManagementDeniedException;
import io.github.ranpers.linkforge.link.management.application.LinkStateConflictException;
import io.github.ranpers.linkforge.link.management.application.ShortLinkNotFoundException;
import io.github.ranpers.linkforge.link.management.domain.InvalidManagedTargetUrlException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = ShortLinkManagementController.class)
public class LinkManagementExceptionHandler {

    @ExceptionHandler(InvalidManagedTargetUrlException.class)
    ProblemDetail invalid(InvalidManagedTargetUrlException exception) {
        return ApiProblems.create(HttpStatus.BAD_REQUEST, "INVALID_TARGET_URL", exception.getMessage());
    }

    @ExceptionHandler(ShortLinkNotFoundException.class)
    ProblemDetail notFound(ShortLinkNotFoundException exception) {
        return ApiProblems.create(HttpStatus.NOT_FOUND, "SHORT_LINK_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(LinkManagementDeniedException.class)
    ProblemDetail denied(LinkManagementDeniedException exception) {
        return ApiProblems.authorization(
                HttpStatus.FORBIDDEN,
                "LINK_MANAGEMENT_DENIED",
                exception.getMessage(),
                exception.reasonCode(),
                exception.decisionId()
        );
    }

    @ExceptionHandler(LinkStateConflictException.class)
    ProblemDetail stateConflict(LinkStateConflictException exception) {
        return ApiProblems.create(HttpStatus.CONFLICT, "LINK_STATE_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(LinkManagementAuthorizationUnavailableException.class)
    ProblemDetail iamUnavailable(LinkManagementAuthorizationUnavailableException exception) {
        return ApiProblems.create(
                HttpStatus.SERVICE_UNAVAILABLE,
                "IAM_UNAVAILABLE",
                "IAM 当前不可用，已拒绝管理短链"
        );
    }
}
