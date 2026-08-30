package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

@Mapper
public interface OutboxDispatchMapper {

    List<OutboxEventRow> lockDueRows(@Param("limit") int limit);

    int markSent(@Param("eventId") UUID eventId);

    int scheduleRetry(
            @Param("eventId") UUID eventId,
            @Param("retryCount") int retryCount,
            @Param("delayMillis") long delayMillis,
            @Param("lastError") String lastError
    );

    int park(
            @Param("eventId") UUID eventId,
            @Param("retryCount") int retryCount,
            @Param("lastError") String lastError
    );
}
