package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.iam.user.domain.UsernameAlreadyExistsException;
import io.github.ranpers.linkforge.iam.user.domain.InvalidUserDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 入站 web 适配器统一兜底:领域异常 → 业务错误码,框架异常 → 参数错误/系统错误
 */
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

    /** 方法级 @PreAuthorize 鉴权失败(403):不走 ExceptionTranslationFilter,须在此转译,否则被兜底 Exception 误判为 500 */
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Result<Void>> handleNotReadable() {
        return build(ErrorCode.PARAM_INVALID, "请求体缺失或格式错误");
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
