package io.github.ranpers.linkforge.link.resolution.adapter.in.web;

import io.github.ranpers.linkforge.link.resolution.application.ShortLinkUnavailableException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import org.springframework.dao.DataAccessException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = ShortLinkResolutionController.class)
public class LinkResolutionExceptionHandler {
    @ExceptionHandler(ShortLinkUnavailableException.class)
    ProblemDetail unavailable(ShortLinkUnavailableException exception) {
        return ApiProblems.create(HttpStatus.NOT_FOUND, "SHORT_LINK_UNAVAILABLE", exception.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    ProblemDetail storageUnavailable(DataAccessException exception) {
        return ApiProblems.create(
                HttpStatus.SERVICE_UNAVAILABLE,
                "STORAGE_UNAVAILABLE",
                "跳转运行状态暂时不可用"
        );
    }
}
