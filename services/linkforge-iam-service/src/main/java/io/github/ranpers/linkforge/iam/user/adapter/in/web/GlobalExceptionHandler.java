package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.iam.user.domain.InvalidUserDataException;
import io.github.ranpers.linkforge.iam.user.domain.UsernameAlreadyExistsException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 将未由功能级异常处理器处理的 IAM HTTP 异常转换为稳定的问题响应。
 *
 * @implNote 最低优先级确保领域功能自己的异常映射先于全局兜底执行。
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    ProblemDetail handleUsernameExists(UsernameAlreadyExistsException exception) {
        return ApiProblems.create(
                HttpStatus.CONFLICT,
                "USERNAME_ALREADY_EXISTS",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidUserDataException.class)
    ProblemDetail handleInvalidUserData(InvalidUserDataException exception) {
        return invalidRequest(exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied() {
        return ApiProblems.create(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "当前主体无权执行该操作"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return invalidRequest(detail);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleMethodValidation() {
        return invalidRequest("请求参数校验失败");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        return invalidRequest(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return invalidRequest(exception.getName() + ": 参数格式错误");
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class
    })
    ProblemDetail handleMissingRequestValue(Exception exception) {
        return invalidRequest(exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail handleNotReadable() {
        return invalidRequest("请求体缺失或格式错误");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        return invalidRequest(exception.getMessage());
    }

    @ExceptionHandler(ErrorResponseException.class)
    ProblemDetail handleFrameworkError(ErrorResponseException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String detail = exception.getBody().getDetail();
        return ApiProblems.create(
                status,
                "HTTP_" + status.value(),
                detail == null ? status.getReasonPhrase() : detail
        );
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        log.error("未处理异常", exception);
        return ApiProblems.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "系统暂时无法处理请求"
        );
    }

    private static ProblemDetail invalidRequest(String detail) {
        return ApiProblems.create(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST",
                detail
        );
    }
}
