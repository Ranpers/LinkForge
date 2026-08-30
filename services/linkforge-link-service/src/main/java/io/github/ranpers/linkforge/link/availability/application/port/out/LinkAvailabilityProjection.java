package io.github.ranpers.linkforge.link.availability.application.port.out;

import io.github.ranpers.linkforge.link.availability.domain.AuthorizationEvent;

public interface LinkAvailabilityProjection {

    /** 只修改当前事件拥有的 disabled_reason 位。 */
    int applyTargetState(AuthorizationEvent event);
}
