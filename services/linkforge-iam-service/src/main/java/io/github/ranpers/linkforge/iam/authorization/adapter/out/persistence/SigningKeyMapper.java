package io.github.ranpers.linkforge.iam.authorization.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SigningKeyMapper {

    void acquireInitializationLock(@Param("lockId") long lockId);

    SigningKeyDO findActive();

    int insert(SigningKeyDO signingKey);
}
