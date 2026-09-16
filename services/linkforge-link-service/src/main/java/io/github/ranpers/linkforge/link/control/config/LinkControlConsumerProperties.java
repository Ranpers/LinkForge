package io.github.ranpers.linkforge.link.control.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "linkforge.link-control-consumer")
public class LinkControlConsumerProperties {
    private boolean enabled;
    private String topic = "linkforge.iam.link-control.v1";
    private String groupId = "linkforge-link-control-v1";
    private int concurrency = 1;
    private int maxAttempts = 5;
    private Duration retryBackoff = Duration.ofSeconds(1);
    private String dltSuffix = ".DLT";

    public void validate() {
        if (topic == null || topic.isBlank() || groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException("Link 控制 Topic 和消费组不能为空");
        }
        if (concurrency < 1 || maxAttempts < 1) {
            throw new IllegalArgumentException("并发数和最大尝试次数必须大于 0");
        }
        if (retryBackoff == null || retryBackoff.isZero() || retryBackoff.isNegative()) {
            throw new IllegalArgumentException("重试间隔必须大于 0");
        }
        if (dltSuffix == null || dltSuffix.isBlank()) {
            throw new IllegalArgumentException("DLT 后缀不能为空");
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    public int getConcurrency() { return concurrency; }
    public void setConcurrency(int concurrency) { this.concurrency = concurrency; }
    public int getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(int maxAttempts) { this.maxAttempts = maxAttempts; }
    public Duration getRetryBackoff() { return retryBackoff; }
    public void setRetryBackoff(Duration retryBackoff) { this.retryBackoff = retryBackoff; }
    public String getDltSuffix() { return dltSuffix; }
    public void setDltSuffix(String dltSuffix) { this.dltSuffix = dltSuffix; }
}
