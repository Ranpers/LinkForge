package io.github.ranpers.linkforge.iam.shortdomain.adapter.in.web;

import io.github.ranpers.linkforge.iam.shortdomain.application.ShortDomainAvailabilityChangeDeniedException;
import io.github.ranpers.linkforge.iam.shortdomain.application.ShortDomainHostAlreadyExistsException;
import io.github.ranpers.linkforge.iam.shortdomain.application.ShortDomainNotFoundException;
import io.github.ranpers.linkforge.iam.shortdomain.application.ShortDomainWriteDeniedException;
import io.github.ranpers.linkforge.iam.shortdomain.domain.InvalidShortDomainHostException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = ShortDomainAvailabilityController.class)
public class ShortDomainExceptionHandler {
    @ExceptionHandler({
            ShortDomainAvailabilityChangeDeniedException.class,
            ShortDomainWriteDeniedException.class
    })
    ProblemDetail denied(RuntimeException exception) {
        return ApiProblems.create(HttpStatus.FORBIDDEN, "SHORT_DOMAIN_ACCESS_DENIED", exception.getMessage());
    }

    @ExceptionHandler(ShortDomainNotFoundException.class)
    ProblemDetail notFound(ShortDomainNotFoundException exception) {
        return ApiProblems.create(HttpStatus.NOT_FOUND, "SHORT_DOMAIN_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(ShortDomainHostAlreadyExistsException.class)
    ProblemDetail conflict(ShortDomainHostAlreadyExistsException exception) {
        return ApiProblems.create(
                HttpStatus.CONFLICT,
                "SHORT_DOMAIN_HOST_ALREADY_EXISTS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidShortDomainHostException.class)
    ProblemDetail invalidHost(InvalidShortDomainHostException exception) {
        return ApiProblems.create(HttpStatus.BAD_REQUEST, "INVALID_DOMAIN_HOST", exception.getMessage());
    }
}
