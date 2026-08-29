package io.github.ranpers.linkforge.iam.config.jwk;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface JwkKeyMapper {

    boolean acquireInitializationLock(@Param("lockId") long lockId);

    StoredJwk findActive();

    int insert(StoredJwk jwk);
}
