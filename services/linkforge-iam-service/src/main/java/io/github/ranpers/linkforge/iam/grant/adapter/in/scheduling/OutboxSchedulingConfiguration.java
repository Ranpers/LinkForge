package io.github.ranpers.linkforge.iam.grant.adapter.in.scheduling;

import io.github.ranpers.linkforge.iam.grant.config.OutboxDispatchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@EnableConfigurationProperties(OutboxDispatchProperties.class)
public class OutboxSchedulingConfiguration {
}
