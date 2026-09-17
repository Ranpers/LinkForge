package io.github.ranpers.linkforge.link.creation.adapter.in.web;

import io.github.ranpers.linkforge.link.creation.application.IamAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.creation.application.IdempotencyConflictException;
import io.github.ranpers.linkforge.link.creation.application.InvalidLinkGroupException;
import io.github.ranpers.linkforge.link.creation.application.LinkCreationDeniedException;
import io.github.ranpers.linkforge.link.creation.application.ShortCodeAllocationException;
import io.github.ranpers.linkforge.link.creation.application.ShortCodeAlreadyExistsException;
import io.github.ranpers.linkforge.link.creation.domain.InvalidShortLinkException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = ShortLinkCreationController.class)
public class LinkCreationExceptionHandler {

    @ExceptionHandler(InvalidShortLinkException.class)
    ProblemDetail invalidLink(InvalidShortLinkException exception) {
        return ApiProblems.create(HttpStatus.BAD_REQUEST, "INVALID_SHORT_LINK", exception.getMessage());
    }

    @ExceptionHandler(InvalidLinkGroupException.class)
    ProblemDetail invalidGroup(InvalidLinkGroupException exception) {
        return ApiProblems.create(HttpStatus.BAD_REQUEST, "INVALID_LINK_GROUP", exception.getMessage());
    }

    @ExceptionHandler(LinkCreationDeniedException.class)
    ProblemDetail denied(LinkCreationDeniedException exception) {
        return ApiProblems.authorization(
                HttpStatus.FORBIDDEN,
                "LINK_CREATION_DENIED",
                exception.getMessage(),
                exception.reasonCode(),
                exception.decisionId()
        );
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail idempotencyConflict(IdempotencyConflictException exception) {
        return ApiProblems.create(HttpStatus.CONFLICT, "IDEMPOTENCY_CONFLICT", exception.getMessage());
    }

    @ExceptionHandler(ShortCodeAlreadyExistsException.class)
    ProblemDetail shortCodeAlreadyExists(ShortCodeAlreadyExistsException exception) {
        return ApiProblems.create(
                HttpStatus.CONFLICT,
                "SHORT_CODE_ALREADY_EXISTS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ShortCodeAllocationException.class)
    ProblemDetail shortCodeAllocationFailed(ShortCodeAllocationException exception) {
        return ApiProblems.create(
                HttpStatus.SERVICE_UNAVAILABLE,
                "SHORT_CODE_ALLOCATION_FAILED",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IamAuthorizationUnavailableException.class)
    ProblemDetail iamUnavailable() {
        return ApiProblems.create(
                HttpStatus.SERVICE_UNAVAILABLE,
                "IAM_UNAVAILABLE",
                "IAM 当前不可用，已拒绝创建短链"
        );
    }
}
