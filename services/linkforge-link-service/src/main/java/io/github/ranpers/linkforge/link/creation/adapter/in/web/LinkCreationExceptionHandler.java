package io.github.ranpers.linkforge.link.creation.adapter.in.web;

import io.github.ranpers.linkforge.link.creation.application.IamAuthorizationUnavailableException;
import io.github.ranpers.linkforge.link.creation.application.IdempotencyConflictException;
import io.github.ranpers.linkforge.link.creation.application.InvalidLinkGroupException;
import io.github.ranpers.linkforge.link.creation.application.LinkCreationDeniedException;
import io.github.ranpers.linkforge.link.creation.domain.InvalidShortLinkException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ShortLinkCreationController.class)
public class LinkCreationExceptionHandler {

    @ExceptionHandler(InvalidShortLinkException.class)
    ProblemDetail invalidLink(InvalidShortLinkException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidLinkGroupException.class)
    ProblemDetail invalidGroup(InvalidLinkGroupException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(LinkCreationDeniedException.class)
    ProblemDetail denied(LinkCreationDeniedException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, exception.getMessage()
        );
        detail.setProperty("reasonCode", exception.reasonCode());
        detail.setProperty("decisionId", exception.decisionId());
        return detail;
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    ProblemDetail idempotencyConflict(IdempotencyConflictException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail uniqueConflict(DataIntegrityViolationException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "同一域名下的短码已经存在"
        );
    }

    @ExceptionHandler(IamAuthorizationUnavailableException.class)
    ProblemDetail iamUnavailable(IamAuthorizationUnavailableException exception) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "IAM 当前不可用，已拒绝创建短链"
        );
    }
}
