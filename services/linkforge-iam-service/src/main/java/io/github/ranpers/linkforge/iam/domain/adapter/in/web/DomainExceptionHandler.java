package io.github.ranpers.linkforge.iam.domain.adapter.in.web;

import io.github.ranpers.linkforge.iam.domain.application.DomainAvailabilityChangeDeniedException;
import io.github.ranpers.linkforge.iam.domain.application.DomainHostAlreadyExistsException;
import io.github.ranpers.linkforge.iam.domain.application.DomainNotFoundException;
import io.github.ranpers.linkforge.iam.domain.application.DomainWriteDeniedException;
import io.github.ranpers.linkforge.iam.domain.domain.InvalidDomainHostException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = DomainAvailabilityController.class)
public class DomainExceptionHandler {
    @ExceptionHandler({
            DomainAvailabilityChangeDeniedException.class,
            DomainWriteDeniedException.class
    })
    ProblemDetail denied(RuntimeException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @ExceptionHandler(DomainNotFoundException.class)
    ProblemDetail notFound(DomainNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DomainHostAlreadyExistsException.class)
    ProblemDetail conflict(DomainHostAlreadyExistsException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidDomainHostException.class)
    ProblemDetail invalidHost(InvalidDomainHostException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
