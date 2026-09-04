package io.github.ranpers.linkforge.link.resolution.adapter.in.web;

import io.github.ranpers.linkforge.link.resolution.application.ShortLinkUnavailableException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ShortLinkResolutionController.class)
public class LinkResolutionExceptionHandler {
    @ExceptionHandler(ShortLinkUnavailableException.class)
    ProblemDetail unavailable(ShortLinkUnavailableException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    ProblemDetail storageUnavailable(DataAccessException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "跳转运行状态暂时不可用"
        );
    }
}
