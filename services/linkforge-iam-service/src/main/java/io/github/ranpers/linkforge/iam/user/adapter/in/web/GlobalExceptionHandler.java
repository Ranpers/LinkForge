package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.iam.user.domain.InvalidUserDataException;
import io.github.ranpers.linkforge.iam.user.domain.UsernameAlreadyExistsException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
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
 * 将未由功能级异常处理器处理的用户接口异常转换为稳定的错误响应。
 *
 * @implNote 最低优先级确保领域功能自己的异常映射先于全局兜底执行。
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    ResponseEntity<Result<Void>> handleUsernameExists() {
        return build(ErrorCode.USERNAME_ALREADY_EXISTS);
    }

    @ExceptionHandler(InvalidUserDataException.class)
    ResponseEntity<Result<Void>> handleInvalidUserData(InvalidUserDataException e) {
        return build(ErrorCode.PARAM_INVALID, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Result<Void>> handleAccessDenied() {
        return build(ErrorCode.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(ErrorCode.PARAM_INVALID, detail);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<Result<Void>> handleMethodValidation() {
        return build(ErrorCode.PARAM_INVALID, "请求参数校验失败");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Result<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        return build(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return build(ErrorCode.PARAM_INVALID, exception.getName() + ": 参数格式错误");
    }

    @ExceptionHandler({
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class
    })
    ResponseEntity<Result<Void>> handleMissingRequestValue(Exception exception) {
        return build(ErrorCode.PARAM_INVALID, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Result<Void>> handleNotReadable() {
        return build(ErrorCode.PARAM_INVALID, "请求体缺失或格式错误");
    }

    @ExceptionHandler(ErrorResponseException.class)
    ResponseEntity<ProblemDetail> handleFrameworkError(ErrorResponseException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getBody());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Result<Void>> handleUnexpected(Exception e) {
        log.error("未处理异常", e);
        return build(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.message());
    }

    private ResponseEntity<Result<Void>> build(ErrorCode errorCode) {
        return build(errorCode, errorCode.message());
    }

    private ResponseEntity<Result<Void>> build(ErrorCode errorCode, String message) {
        return ResponseEntity.status(errorCode.httpStatus()).body(Result.fail(errorCode, message));
    }
}
