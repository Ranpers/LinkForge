package io.github.ranpers.linkforge.link.creation.adapter.out.persistence;

import io.github.ranpers.linkforge.link.creation.domain.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface ShortLinkMapper {
    ShortLinkRow findByIdempotencyKey(
            @Param("userId") UUID userId,
            @Param("idempotencyKey") String idempotencyKey
    );

    boolean groupBelongsToUser(
            @Param("groupId") UUID groupId,
            @Param("userId") UUID userId
    );

    int insertIfIdempotencyAbsent(@Param("link") ShortLink link);
}
