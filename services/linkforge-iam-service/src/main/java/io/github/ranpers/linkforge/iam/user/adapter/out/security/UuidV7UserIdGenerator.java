package io.github.ranpers.linkforge.iam.user.adapter.out.security;

import io.github.ranpers.linkforge.iam.user.application.port.out.UserIdGenerator;
import io.github.ranpers.linkforge.iam.infrastructure.id.UuidV7;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import org.springframework.stereotype.Component;

@Component
public class UuidV7UserIdGenerator implements UserIdGenerator {

    @Override
    public UserId nextId() {
        return new UserId(UuidV7.generate());
    }
}
