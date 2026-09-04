package io.github.ranpers.linkforge.link.resolution.application.port.in;

public interface ResolveShortLinkUseCase {
    ResolvedShortLink resolve(String host, String linkCode);
}
