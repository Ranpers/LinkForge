package io.github.ranpers.linkforge.iam.security.adapter.in.web;

import io.github.ranpers.linkforge.iam.security.application.LinkSecurityRestrictionNotFoundException;
import io.github.ranpers.linkforge.iam.security.application.SecurityDispositionDeniedException;
import io.github.ranpers.linkforge.iam.security.application.SecurityTargetUserNotFoundException;
import io.github.ranpers.linkforge.iam.security.application.UserSecurityStatusConflictException;
import io.github.ranpers.linkforge.iam.security.domain.InvalidLinkSecurityRestrictionException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = {
        LinkSecurityRestrictionController.class,
        UserSecurityStatusController.class
})
public class LinkSecurityExceptionHandler {

    @ExceptionHandler(SecurityDispositionDeniedException.class)
    ProblemDetail denied(SecurityDispositionDeniedException exception) {
        return ApiProblems.create(
                HttpStatus.FORBIDDEN,
                "SECURITY_DISPOSITION_DENIED",
                exception.getMessage()
        );
    }

    @ExceptionHandler({
            SecurityTargetUserNotFoundException.class,
            LinkSecurityRestrictionNotFoundException.class
    })
    ProblemDetail notFound(RuntimeException exception) {
        return ApiProblems.create(HttpStatus.NOT_FOUND, "SECURITY_TARGET_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(UserSecurityStatusConflictException.class)
    ProblemDetail conflict(UserSecurityStatusConflictException exception) {
        return ApiProblems.create(
                HttpStatus.CONFLICT,
                "USER_SECURITY_STATUS_CONFLICT",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidLinkSecurityRestrictionException.class)
    ProblemDetail invalid(InvalidLinkSecurityRestrictionException exception) {
        return ApiProblems.create(
                HttpStatus.BAD_REQUEST,
                "INVALID_SECURITY_RESTRICTION",
                exception.getMessage()
        );
    }
}
