package io.github.ranpers.linkforge.link.resolution.application.port.out;

import java.util.List;
import java.util.UUID;

public record UserSecurityRestrictionSet(
        UUID userId,
        long revision,
        List<UserSecurityRestriction> restrictions
) {
    public UserSecurityRestrictionSet {
        restrictions = List.copyOf(restrictions);
    }
}
