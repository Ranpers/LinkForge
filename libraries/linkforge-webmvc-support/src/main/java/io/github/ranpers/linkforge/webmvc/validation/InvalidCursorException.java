package io.github.ranpers.linkforge.webmvc.validation;

/**
 * 表示分页游标缺失配对字段、编码损坏或超出允许长度。
 *
 * @implNote 继承 {@link InvalidQueryParameterException}，使未单独处理本类型的服务至少回退到
 * 400，而不会因为缺少处理器把调用方输入问题报成 500。
 */
public class InvalidCursorException extends InvalidQueryParameterException {

    public InvalidCursorException(String message) {
        super(message);
    }
}
