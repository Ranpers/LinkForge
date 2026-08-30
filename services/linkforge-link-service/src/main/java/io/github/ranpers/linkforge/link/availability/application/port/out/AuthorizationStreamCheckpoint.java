package io.github.ranpers.linkforge.link.availability.application.port.out;

public interface AuthorizationStreamCheckpoint {

    void ensureExists(String streamKey);

    long lockAndGetRevision(String streamKey);

    void advance(String streamKey, long revision);
}
