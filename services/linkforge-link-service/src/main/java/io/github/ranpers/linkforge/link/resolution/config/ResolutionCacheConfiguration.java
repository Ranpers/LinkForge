package io.github.ranpers.linkforge.link.resolution.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ResolutionCacheProperties.class)
public class ResolutionCacheConfiguration {
}
