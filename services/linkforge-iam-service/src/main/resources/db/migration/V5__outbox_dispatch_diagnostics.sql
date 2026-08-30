-- Outbox 投递诊断字段:parked 行必须保留最后一次失败原因,否则无法人工处置。
ALTER TABLE t_outbox_event
    ADD COLUMN last_attempt_at timestamptz,
    ADD COLUMN last_error varchar(2000);

COMMENT ON COLUMN t_outbox_event.last_attempt_at IS '最近一次 Kafka 投递尝试时间;认领但进程崩溃时可能不更新';
COMMENT ON COLUMN t_outbox_event.last_error IS '最近一次投递失败摘要;成功后清空,parked 时供告警与人工处置';
