package io.github.ranpers.linkforge.gateway.config;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/** 浏览器客户端访问统一入口时允许的精确 Origin 白名单。 */
@Validated
@ConfigurationProperties(prefix = "linkforge.gateway.cors")
public class GatewayCorsProperties {

    @NotEmpty
    private List<String> allowedOrigins = new ArrayList<>(List.of(
            "http://127.0.0.1:5173",
            "http://localhost:5173"
    ));

    public List<String> getAllowedOrigins() {
        return List.copyOf(allowedOrigins);
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = new ArrayList<>(allowedOrigins);
    }
}
