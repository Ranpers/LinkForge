package io.github.ranpers.linkforge.link.group.adapter.in.web;

import io.github.ranpers.linkforge.link.group.application.GroupAlreadyExistsException;
import io.github.ranpers.linkforge.link.group.application.GroupNotFoundException;
import io.github.ranpers.linkforge.link.group.domain.InvalidGroupNameException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = GroupController.class)
public class GroupExceptionHandler {

    @ExceptionHandler(InvalidGroupNameException.class)
    ProblemDetail invalidName(InvalidGroupNameException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(GroupNotFoundException.class)
    ProblemDetail notFound(GroupNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(GroupAlreadyExistsException.class)
    ProblemDetail conflict(GroupAlreadyExistsException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    /**
     * 兜底并发改名场景：预检与写入之间的竞态由数据库唯一索引拦下。
     *
     * <p>此时事务已回滚，本方法只负责把冲突翻译成 409，不重试写入。</p>
     */
    @ExceptionHandler(DuplicateKeyException.class)
    ProblemDetail duplicateKey(DuplicateKeyException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "同名分组已存在");
    }
}
