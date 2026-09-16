package io.github.ranpers.linkforge.link.control.application.port.out;

/**
 * 维护每个控制事件流已应用的最高修订号。
 *
 * @apiNote 确保检查点存在、加锁读取和推进修订号必须参与同一数据库事务。
 */
public interface LinkControlCheckpoint {

    /**
     * 幂等地创建初始修订号为零的检查点。
     *
     * @param streamKey 事件定义的稳定聚合流标识
     */
    void ensureExists(String streamKey);

    /**
     * 加排他锁并读取当前已应用修订号。
     *
     * @param streamKey 事件定义的稳定聚合流标识
     * @return 当前已应用的最高修订号
     */
    long lockAndGetRevision(String streamKey);

    /**
     * 在投影成功后推进检查点。
     *
     * @param streamKey 事件定义的稳定聚合流标识
     * @param revision 严格大于当前值的新修订号
     */
    void advance(String streamKey, long revision);
}
