package io.github.ranpers.linkforge.iam.user.adapter.in.web;

import io.github.ranpers.linkforge.iam.user.domain.InvalidUserDataException;
import io.github.ranpers.linkforge.iam.user.domain.UsernameAlreadyExistsException;
import io.github.ranpers.linkforge.webmvc.problem.ApiProblems;
import io.github.ranpers.linkforge.webmvc.validation.InvalidCursorException;
import io.github.ranpers.linkforge.webmvc.validation.InvalidQueryParameterException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 将未由功能级异常处理器处理的 IAM HTTP 异常转换为稳定的问题响应。
 *
 * @implNote 最低优先级确保领域功能自己的异常映射先于全局兜底执行。这里刻意不为
 * {@link IllegalArgumentException} 提供处理器：领域不变量、配置读取和内部解码失败都用它表达，
 * 统一映射成 400 会把这些服务端缺陷伪装成调用方输入问题。只有专门表示调用方输入的
 * {@link InvalidCursorException} 与 {@link InvalidQueryParameterException} 才按 400 返回。
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
        return invalidRequest(describe(exception.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "))));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail handleMethodValidation(HandlerMethodValidationException exception) {
        return invalidRequest(describe(exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> parameterName(result) + ": " + error.getDefaultMessage()))
                .collect(Collectors.joining("; "))));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        return invalidRequest(describe(exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "))));
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

    @ExceptionHandler(InvalidCursorException.class)
    ProblemDetail handleInvalidCursor(InvalidCursorException exception) {
        return ApiProblems.create(
                HttpStatus.BAD_REQUEST,
                "INVALID_CURSOR",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidQueryParameterException.class)
    ProblemDetail handleInvalidQueryParameter(InvalidQueryParameterException exception) {
        return invalidRequest(exception.getMessage());
    }

    /**
     * 处理路由已匹配但 HTTP 方法不被支持的情况。
     *
     * @implNote 该异常只实现 {@code ErrorResponse} 而不继承 {@link ErrorResponseException}，
     * 缺少本处理器时会落入 {@link Exception} 兜底并被报成 500。网关按路径转发，方法判定发生在
     * 服务内部，因此这里必须显式返回 405。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return ApiProblems.create(
                HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                exception.getMethod() + " 不支持该资源"
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail handleNoResource() {
        return ApiProblems.create(HttpStatus.NOT_FOUND, "NOT_FOUND", "请求的资源不存在");
    }

    @ExceptionHandler(ErrorResponseException.class)
    ProblemDetail handleFrameworkError(ErrorResponseException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ApiProblems.create(status, "HTTP_" + status.value(), frameworkDetail(status, exception));
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception exception) {
        log.error("未处理异常", exception);
        return internalError();
    }

    /**
     * 返回可以安全暴露给调用方的框架异常详情。
     *
     * @implNote 4xx 详情由框架依据请求内容生成，可以原样回传；5xx 详情可能包含内部状态，
     * 一律替换为固定文案，完整原因只写进服务端日志。
     */
    private static String frameworkDetail(HttpStatus status, ErrorResponseException exception) {
        if (status.is5xxServerError()) {
            log.error("框架异常映射为 {}", status.value(), exception);
            return "系统暂时无法处理请求";
        }
        String detail = exception.getBody().getDetail();
        return detail == null ? status.getReasonPhrase() : detail;
    }

    private static String parameterName(ParameterValidationResult result) {
        String name = result.getMethodParameter().getParameterName();
        return name == null ? "参数" : name;
    }

    private static String describe(String detail) {
        return detail.isEmpty() ? "请求参数校验失败" : detail;
    }

    private static ProblemDetail internalError() {
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
