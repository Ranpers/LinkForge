package io.github.ranpers.linkforge.iam.user.application.port.in;

import java.util.Optional;

public interface LoadLoginUserUseCase {

    Optional<LoginUser> loadByUsername(String username);
}
