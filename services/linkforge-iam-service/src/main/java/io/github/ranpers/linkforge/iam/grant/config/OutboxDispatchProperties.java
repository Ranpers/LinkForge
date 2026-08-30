package io.github.ranpers.linkforge.iam.grant.config;

import io.github.ranpers.linkforge.iam.grant.application.OutboxDispatchSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "linkforge.outbox")
public class OutboxDispatchProperties {

    private boolean enabled;
    private String topic = "linkforge.iam.authorization.v1";
    private Duration pollInterval = Duration.ofSeconds(1);
    private int batchSize = 50;
    private int maxAttempts = 8;
    private Duration baseBackoff = Duration.ofSeconds(1);
    private Duration maxBackoff = Duration.ofMinutes(5);
    private Duration sendTimeout = Duration.ofSeconds(10);

    public OutboxDispatchSettings settings() {
        return new OutboxDispatchSettings(
                batchSize,
                maxAttempts,
                baseBackoff,
                maxBackoff,
                sendTimeout
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Duration getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Duration pollInterval) {
        this.pollInterval = pollInterval;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Duration getBaseBackoff() {
        return baseBackoff;
    }

    public void setBaseBackoff(Duration baseBackoff) {
        this.baseBackoff = baseBackoff;
    }

    public Duration getMaxBackoff() {
        return maxBackoff;
    }

    public void setMaxBackoff(Duration maxBackoff) {
        this.maxBackoff = maxBackoff;
    }

    public Duration getSendTimeout() {
        return sendTimeout;
    }

    public void setSendTimeout(Duration sendTimeout) {
        this.sendTimeout = sendTimeout;
    }
}
