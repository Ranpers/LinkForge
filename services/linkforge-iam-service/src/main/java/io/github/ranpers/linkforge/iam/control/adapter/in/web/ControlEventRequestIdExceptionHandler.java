package io.github.ranpers.linkforge.iam.control.adapter.in.web;

import io.github.ranpers.linkforge.iam.control.domain.InvalidControlEventRequestIdException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ControlEventRequestIdExceptionHandler {

    @ExceptionHandler(InvalidControlEventRequestIdException.class)
    ProblemDetail invalid(InvalidControlEventRequestIdException exception) {
        return ApiProblems.create(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_ID", exception.getMessage());
    }
}
