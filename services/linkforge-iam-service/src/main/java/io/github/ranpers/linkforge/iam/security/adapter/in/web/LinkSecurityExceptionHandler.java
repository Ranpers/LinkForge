package io.github.ranpers.linkforge.iam.security.adapter.in.web;

import io.github.ranpers.linkforge.iam.security.application.LinkSecurityRestrictionNotFoundException;
import io.github.ranpers.linkforge.iam.security.application.SecurityDispositionDeniedException;
import io.github.ranpers.linkforge.iam.security.application.SecurityTargetUserNotFoundException;
import io.github.ranpers.linkforge.iam.security.application.UserSecurityStatusConflictException;
import io.github.ranpers.linkforge.iam.security.domain.InvalidLinkSecurityRestrictionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = {
        LinkSecurityRestrictionController.class,
        UserSecurityStatusController.class
})
public class LinkSecurityExceptionHandler {

    @ExceptionHandler(SecurityDispositionDeniedException.class)
    ProblemDetail denied(SecurityDispositionDeniedException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler({
            SecurityTargetUserNotFoundException.class,
            LinkSecurityRestrictionNotFoundException.class
    })
    ProblemDetail notFound(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(UserSecurityStatusConflictException.class)
    ProblemDetail conflict(UserSecurityStatusConflictException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidLinkSecurityRestrictionException.class)
    ProblemDetail invalid(InvalidLinkSecurityRestrictionException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
