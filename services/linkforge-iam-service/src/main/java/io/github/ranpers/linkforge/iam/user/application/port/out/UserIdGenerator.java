package io.github.ranpers.linkforge.iam.user.application.port.out;

import io.github.ranpers.linkforge.iam.user.domain.UserId;

public interface UserIdGenerator {

    UserId nextId();
}
