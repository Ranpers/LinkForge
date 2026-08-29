package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.grant.application.AffectedPair;
import io.github.ranpers.linkforge.iam.grant.application.port.out.GrantOutboxStore;
import io.github.ranpers.linkforge.iam.infrastructure.id.UuidV7;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public class MybatisGrantOutboxStore implements GrantOutboxStore {

    private static final String EVENT_TYPE = "UserDomainGrantChanged";

    private final GrantProjectionMapper mapper;

    public MybatisGrantOutboxStore(GrantProjectionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void appendGrantChanged(AffectedPair pair, boolean granted, long revision, OffsetDateTime occurredAt) {
        UUID eventId = UuidV7.generate();
        String streamKey = "USER_DOMAIN:" + pair.userId() + ":" + pair.domainId();
        // 载荷字段均为受控值(UUID/常量/数值/ISO 时间戳),无转义风险,故手工构造扁平 JSON
        String payload = "{\"eventId\":\"" + eventId
                + "\",\"eventType\":\"" + EVENT_TYPE
                + "\",\"streamKey\":\"" + streamKey
                + "\",\"revision\":" + revision
                + ",\"occurredAt\":\"" + occurredAt
                + "\",\"userId\":\"" + pair.userId()
                + "\",\"domainId\":\"" + pair.domainId()
                + "\",\"granted\":" + granted
                + "}";
        mapper.insertOutbox(eventId, EVENT_TYPE, streamKey, pair.domainId().toString(), payload);
    }
}
