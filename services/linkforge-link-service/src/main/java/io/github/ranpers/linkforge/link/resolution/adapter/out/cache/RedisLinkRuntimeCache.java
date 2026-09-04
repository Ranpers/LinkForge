package io.github.ranpers.linkforge.link.resolution.adapter.out.cache;

import io.github.ranpers.linkforge.link.resolution.application.port.out.DomainRuntimeState;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFacts;
import io.github.ranpers.linkforge.link.resolution.application.port.out.UserSecurityRestrictionSet;
import io.github.ranpers.linkforge.link.resolution.config.ResolutionCacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RedisLinkRuntimeCache implements LinkRuntimeCache {

    private static final Logger log = LoggerFactory.getLogger(RedisLinkRuntimeCache.class);
    private static final DefaultRedisScript<Long> PUT_IF_NEWER = new DefaultRedisScript<>(
            """
            local current = redis.call('HGET', KEYS[1], 'revision')
            if (not current) or tonumber(ARGV[1]) >= tonumber(current) then
                redis.call('HSET', KEYS[1], 'revision', ARGV[1], 'payload', ARGV[2])
                redis.call('PEXPIRE', KEYS[1], ARGV[3])
                return 1
            end
            return 0
            """,
            Long.class
    );

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final ResolutionCacheProperties properties;

    public RedisLinkRuntimeCache(
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            ResolutionCacheProperties properties
    ) {
        properties.validate();
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public Optional<LinkRuntimeFacts> findLink(String host, String linkCode) {
        return read(linkKey(host, linkCode), LinkRuntimeFacts.class);
    }

    @Override
    public Optional<DomainRuntimeState> findDomain(UUID domainId) {
        return read(domainKey(domainId), DomainRuntimeState.class);
    }

    @Override
    public Optional<UserSecurityRestrictionSet> findRestrictions(UUID userId) {
        return read(restrictionsKey(userId), UserSecurityRestrictionSet.class);
    }

    @Override
    public void putLink(LinkRuntimeFacts link) {
        writeIfNewer(
                linkKey(link.host(), link.linkCode()),
                link.revision(),
                link,
                properties.getLinkTtl()
        );
    }

    @Override
    public void putDomain(DomainRuntimeState domain) {
        writeIfNewer(
                domainKey(domain.domainId()),
                domain.revision(),
                domain,
                properties.getControlTtl()
        );
    }

    @Override
    public void putRestrictions(UserSecurityRestrictionSet restrictions) {
        writeIfNewer(
                restrictionsKey(restrictions.userId()),
                restrictions.revision(),
                restrictions,
                properties.getControlTtl()
        );
    }

    private <T> Optional<T> read(String key, Class<T> type) {
        try {
            Object payload = redis.opsForHash().get(key, "payload");
            if (!(payload instanceof String json)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, type));
        } catch (RuntimeException exception) {
            log.warn("Redis 跳转缓存读取失败，回源 PostgreSQL: key={}", key, exception);
            return Optional.empty();
        }
    }

    private void writeIfNewer(String key, long revision, Object value, Duration baseTtl) {
        try {
            String json = objectMapper.writeValueAsString(value);
            redis.execute(
                    PUT_IF_NEWER,
                    List.of(key),
                    Long.toString(revision),
                    json,
                    Long.toString(ttlMillis(baseTtl))
            );
        } catch (RuntimeException exception) {
            log.warn("Redis 跳转缓存写入失败，PostgreSQL 仍为权威: key={}", key, exception);
        }
    }

    private long ttlMillis(Duration baseTtl) {
        long jitterBound = properties.getJitter().toMillis();
        long jitter = jitterBound == 0
                ? 0
                : ThreadLocalRandom.current().nextLong(jitterBound + 1);
        return Math.addExact(baseTtl.toMillis(), jitter);
    }

    private static String linkKey(String host, String linkCode) {
        return "lf:link:" + host + ":" + linkCode;
    }

    private static String domainKey(UUID domainId) {
        return "lf:domain-state:" + domainId;
    }

    private static String restrictionsKey(UUID userId) {
        return "lf:user-link-restrictions:" + userId;
    }
}
