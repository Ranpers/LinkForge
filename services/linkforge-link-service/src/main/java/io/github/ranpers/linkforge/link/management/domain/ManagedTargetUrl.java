package io.github.ranpers.linkforge.link.management.domain;

import java.net.URI;
import java.net.URISyntaxException;

public record ManagedTargetUrl(String value) {
    public ManagedTargetUrl {
        if (value == null || value.isBlank()) {
            throw new InvalidManagedTargetUrlException("fullUrl 不能为空");
        }
        String normalized = value.trim();
        if (normalized.length() > 2048) {
            throw new InvalidManagedTargetUrlException("fullUrl 长度不能超过 2048");
        }
        try {
            URI uri = new URI(normalized);
            if (!(uri.getScheme() != null
                    && ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme())))
                    || uri.getHost() == null) {
                throw new InvalidManagedTargetUrlException(
                        "fullUrl 必须是包含主机名的 HTTP(S) URL"
                );
            }
            value = uri.toASCIIString();
        } catch (URISyntaxException exception) {
            throw new InvalidManagedTargetUrlException("fullUrl 不是合法 URL", exception);
        }
    }
}
