package io.github.ranpers.linkforge.iam.authorization.application.port.in;

import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;

public interface LoadSigningKeyUseCase {

    SigningKey loadOrCreate();
}
