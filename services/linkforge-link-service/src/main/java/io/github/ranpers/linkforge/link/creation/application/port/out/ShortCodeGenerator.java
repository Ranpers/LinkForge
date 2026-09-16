package io.github.ranpers.linkforge.link.creation.application.port.out;

/**
 * 为自动分配流程提供不可预测的短码候选值。
 */
public interface ShortCodeGenerator {

    /**
     * 生成一个不保证数据库唯一的候选短码。
     *
     * @return 非空的 10 位 Base62 系统短码
     * @apiNote 调用者必须通过数据库唯一约束裁决候选值，不能把随机性视为唯一性保证
     */
    String nextCode();
}
