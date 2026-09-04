package io.github.ranpers.linkforge.link.control.application.port.out;

import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;

public interface LinkControlCache {
    void projectAfterCommit(LinkControlEvent event);
}
