package io.github.ranpers.linkforge.link.creation.application;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkCommand;
import io.github.ranpers.linkforge.link.creation.application.port.in.ShortCodeRequest;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

final class RequestFingerprint {

    private RequestFingerprint() {
    }

    static String of(CreateShortLinkCommand command) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            add(digest, command.groupId());
            add(digest, command.name());
            addShortCodeRequest(digest, command.shortCodeRequest());
            add(digest, command.fullUrl());
            add(digest, command.sortOrder());
            add(digest, command.domainId());
            add(digest, command.expiresAt());
            return java.util.HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JVM 不支持 SHA-256", exception);
        }
    }

    private static void addShortCodeRequest(
            MessageDigest digest,
            ShortCodeRequest request
    ) {
        switch (request) {
            case ShortCodeRequest.Auto() -> add(digest, "AUTO");
            case ShortCodeRequest.Custom custom -> {
                add(digest, "CUSTOM");
                add(digest, custom.shortCode().value());
            }
        }
    }

    private static void add(MessageDigest digest, Object value) {
        byte[] bytes = String.valueOf(value).getBytes(StandardCharsets.UTF_8);
        digest.update(ByteBuffer.allocate(Integer.BYTES).putInt(bytes.length).array());
        digest.update(bytes);
    }
}
