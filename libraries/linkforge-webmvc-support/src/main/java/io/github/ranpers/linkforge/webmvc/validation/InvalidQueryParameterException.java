package io.github.ranpers.linkforge.webmvc.validation;

/**
 * 表示请求参数在语义上不合法，且可以明确归因到调用方输入。
 *
 * @apiNote 只应由解析调用方输入的边界代码抛出。领域不变量、配置读取和内部解码失败必须继续使用
 * 普通 {@link IllegalArgumentException}，否则这些服务端缺陷会被当成 400 返回，掩盖真实故障。
 */
public class InvalidQueryParameterException extends IllegalArgumentException {

    public InvalidQueryParameterException(String message) {
        super(message);
    }
}
