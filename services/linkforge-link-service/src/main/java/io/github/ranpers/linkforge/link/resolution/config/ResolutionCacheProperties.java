package io.github.ranpers.linkforge.link.resolution.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "linkforge.resolution-cache")
public class ResolutionCacheProperties {
    private Duration linkTtl = Duration.ofMinutes(10);
    private Duration controlTtl = Duration.ofSeconds(30);
    private Duration mutationFenceTtl = Duration.ofMinutes(1);
    private Duration jitter = Duration.ofSeconds(5);

    public void validate() {
        if (!positive(linkTtl) || !positive(controlTtl) || !positive(mutationFenceTtl)
                || jitter == null || jitter.isNegative()) {
            throw new IllegalArgumentException("缓存与写屏障 TTL 必须为正数，jitter 不能为负数");
        }
    }

    private static boolean positive(Duration duration) {
        return duration != null && !duration.isZero() && !duration.isNegative();
    }

    public Duration getLinkTtl() {
        return linkTtl;
    }

    public void setLinkTtl(Duration linkTtl) {
        this.linkTtl = linkTtl;
    }

    public Duration getControlTtl() {
        return controlTtl;
    }

    public void setControlTtl(Duration controlTtl) {
        this.controlTtl = controlTtl;
    }

    public Duration getMutationFenceTtl() {
        return mutationFenceTtl;
    }

    public void setMutationFenceTtl(Duration mutationFenceTtl) {
        this.mutationFenceTtl = mutationFenceTtl;
    }

    public Duration getJitter() {
        return jitter;
    }

    public void setJitter(Duration jitter) {
        this.jitter = jitter;
    }
}
