package io.github.ranpers.linkforge.webmvc.validation;

/**
 * 表示分页游标编码损坏、超出允许长度或缺少配对字段。
 *
 * @implNote 只由 Web 边界的 {@link io.github.ranpers.linkforge.webmvc.pagination.CursorCodec}
 * 抛出，专用于表达调用方送来的游标不合法，因此服务端一律映射成 400。
 * application 层的输入对象刻意不使用本类型：那里的同名校验拦截的是适配器或内部调用缺陷，
 * 应当报成 500，而不是伪装成调用方输入错误。
 */
public class InvalidCursorException extends IllegalArgumentException {

    public InvalidCursorException(String message) {
        super(message);
    }
}
