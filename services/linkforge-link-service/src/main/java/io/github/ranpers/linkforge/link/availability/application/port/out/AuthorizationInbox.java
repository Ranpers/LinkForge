package io.github.ranpers.linkforge.link.availability.application.port.out;

import io.github.ranpers.linkforge.link.availability.domain.AuthorizationEvent;

public interface AuthorizationInbox {

    /** @return true=首次收到；false=eventId 已处理。 */
    boolean recordIfNew(AuthorizationEvent event);
}
