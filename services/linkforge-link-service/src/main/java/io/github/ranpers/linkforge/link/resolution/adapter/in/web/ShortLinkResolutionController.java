package io.github.ranpers.linkforge.link.resolution.adapter.in.web;

import io.github.ranpers.linkforge.link.resolution.application.port.in.ResolveShortLinkUseCase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Locale;

@RestController
@RequestMapping("/r")
public class ShortLinkResolutionController {

    private final ResolveShortLinkUseCase resolveShortLink;

    public ShortLinkResolutionController(ResolveShortLinkUseCase resolveShortLink) {
        this.resolveShortLink = resolveShortLink;
    }

    @GetMapping("/{linkCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String linkCode,
            HttpServletRequest request
    ) {
        String host = request.getServerName().toLowerCase(Locale.ROOT);
        var result = resolveShortLink.resolve(host, linkCode);
        return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT)
                .location(URI.create(result.targetUrl()))
                .build();
    }
}
