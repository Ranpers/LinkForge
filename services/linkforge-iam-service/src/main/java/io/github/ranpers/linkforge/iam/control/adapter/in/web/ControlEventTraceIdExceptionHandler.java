package io.github.ranpers.linkforge.iam.control.adapter.in.web;

import io.github.ranpers.linkforge.iam.control.domain.InvalidControlEventTraceIdException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ControlEventTraceIdExceptionHandler {

    @ExceptionHandler(InvalidControlEventTraceIdException.class)
    ProblemDetail invalid(InvalidControlEventTraceIdException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
