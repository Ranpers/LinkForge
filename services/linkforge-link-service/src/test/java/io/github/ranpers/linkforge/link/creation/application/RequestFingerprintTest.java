package io.github.ranpers.linkforge.link.creation.application;

import io.github.ranpers.linkforge.link.creation.application.port.in.CreateShortLinkCommand;
import io.github.ranpers.linkforge.link.creation.application.port.in.ShortCodeRequest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RequestFingerprintTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID domainId = UUID.randomUUID();

    @Test
    void autoRequestHasStableFingerprintWithoutGeneratedCandidate() {
        assertEquals(
                RequestFingerprint.of(command(ShortCodeRequest.auto())),
                RequestFingerprint.of(command(ShortCodeRequest.auto()))
        );
    }

    @Test
    void allocationModeAndCustomValueAffectFingerprint() {
        String auto = RequestFingerprint.of(command(ShortCodeRequest.auto()));
        String firstCustom =
                RequestFingerprint.of(command(ShortCodeRequest.custom("my-code")));
        String secondCustom =
                RequestFingerprint.of(command(ShortCodeRequest.custom("my-code-2")));

        assertNotEquals(auto, firstCustom);
        assertNotEquals(firstCustom, secondCustom);
    }

    private CreateShortLinkCommand command(ShortCodeRequest request) {
        return new CreateShortLinkCommand(
                userId,
                null,
                "Example",
                request,
                "https://example.com",
                0,
                domainId,
                null,
                "request-1"
        );
    }
}
