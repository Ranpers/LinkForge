package io.github.ranpers.linkforge.link.control.application.port.out;

public interface LinkControlCheckpoint {
    void ensureExists(String streamKey);

    long lockAndGetRevision(String streamKey);

    void advance(String streamKey, long revision);
}
