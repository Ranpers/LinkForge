package io.github.ranpers.linkforge.iam.domain.adapter.in.web;

import io.github.ranpers.linkforge.iam.domain.application.DomainAvailabilityChangeDeniedException;
import io.github.ranpers.linkforge.iam.domain.application.DomainHostAlreadyExistsException;
import io.github.ranpers.linkforge.iam.domain.application.DomainNotFoundException;
import io.github.ranpers.linkforge.iam.domain.application.DomainWriteDeniedException;
import io.github.ranpers.linkforge.iam.domain.domain.InvalidDomainHostException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = DomainAvailabilityController.class)
public class DomainExceptionHandler {
    @ExceptionHandler({
            DomainAvailabilityChangeDeniedException.class,
            DomainWriteDeniedException.class
    })
    ProblemDetail denied(RuntimeException exception) {
        return ApiProblems.create(HttpStatus.FORBIDDEN, "DOMAIN_ACCESS_DENIED", exception.getMessage());
    }

    @ExceptionHandler(DomainNotFoundException.class)
    ProblemDetail notFound(DomainNotFoundException exception) {
        return ApiProblems.create(HttpStatus.NOT_FOUND, "DOMAIN_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(DomainHostAlreadyExistsException.class)
    ProblemDetail conflict(DomainHostAlreadyExistsException exception) {
        return ApiProblems.create(
                HttpStatus.CONFLICT,
                "DOMAIN_HOST_ALREADY_EXISTS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidDomainHostException.class)
    ProblemDetail invalidHost(InvalidDomainHostException exception) {
        return ApiProblems.create(HttpStatus.BAD_REQUEST, "INVALID_DOMAIN_HOST", exception.getMessage());
    }
}
