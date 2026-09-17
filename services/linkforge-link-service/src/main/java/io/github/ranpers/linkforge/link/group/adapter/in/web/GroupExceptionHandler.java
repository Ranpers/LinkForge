package io.github.ranpers.linkforge.link.group.adapter.in.web;

import io.github.ranpers.linkforge.link.group.application.GroupAlreadyExistsException;
import io.github.ranpers.linkforge.link.group.application.GroupNotFoundException;
import io.github.ranpers.linkforge.link.group.domain.InvalidGroupNameException;
import io.github.ranpers.linkforge.link.infrastructure.web.ApiProblems;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackageClasses = GroupController.class)
public class GroupExceptionHandler {

    @ExceptionHandler(InvalidGroupNameException.class)
    ProblemDetail invalidName(InvalidGroupNameException exception) {
        return ApiProblems.create(HttpStatus.BAD_REQUEST, "INVALID_GROUP_NAME", exception.getMessage());
    }

    @ExceptionHandler(GroupNotFoundException.class)
    ProblemDetail notFound(GroupNotFoundException exception) {
        return ApiProblems.create(HttpStatus.NOT_FOUND, "GROUP_NOT_FOUND", exception.getMessage());
    }

    @ExceptionHandler(GroupAlreadyExistsException.class)
    ProblemDetail conflict(GroupAlreadyExistsException exception) {
        return ApiProblems.create(HttpStatus.CONFLICT, "GROUP_ALREADY_EXISTS", exception.getMessage());
    }

    /**
     * 兜底并发改名场景：预检与写入之间的竞态由数据库唯一索引拦下。
     *
     * <p>此时事务已回滚，本方法只负责把冲突翻译成 409，不重试写入。</p>
     */
    @ExceptionHandler(DuplicateKeyException.class)
    ProblemDetail duplicateKey(DuplicateKeyException exception) {
        return ApiProblems.create(HttpStatus.CONFLICT, "GROUP_ALREADY_EXISTS", "同名分组已存在");
    }
}
