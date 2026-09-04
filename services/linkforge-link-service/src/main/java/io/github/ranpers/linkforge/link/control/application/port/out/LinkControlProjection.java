package io.github.ranpers.linkforge.link.control.application.port.out;

import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;

public interface LinkControlProjection {
    int apply(LinkControlEvent event);
}
