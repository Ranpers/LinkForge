package io.github.ranpers.linkforge.link.resolution.adapter.out.cache;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.github.ranpers.linkforge.link.resolution.application.port.out.DomainRuntimeState;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeCache;
import io.github.ranpers.linkforge.link.resolution.application.port.out.LinkRuntimeFacts;
import io.github.ranpers.linkforge.link.resolution.application.port.out.RuntimeCacheMutationException;
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

/**
 * 使用修订号和带所有权令牌的写屏障维护解析缓存。
 *
 * @implNote 读、普通回源写、屏障建立和事务完成均由 Lua 脚本原子执行。相关数据键和
 * 屏障键共享 Redis Cluster hash tag，保证脚本在集群部署中位于同一槽位。
 */
@Component
public class RedisLinkRuntimeCache implements LinkRuntimeCache {

    private static final Logger log = LoggerFactory.getLogger(RedisLinkRuntimeCache.class);
    private static final DefaultRedisScript<Long> PUT_IF_NEWER = new DefaultRedisScript<>(
            """
            if redis.call('EXISTS', KEYS[2]) == 1 then
                return -1
            end
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
    private static final DefaultRedisScript<String> READ_IF_UNFENCED = new DefaultRedisScript<>(
            """
            if redis.call('EXISTS', KEYS[2]) == 1 then
                return nil
            end
            return redis.call('HGET', KEYS[1], 'payload')
            """,
            String.class
    );
    private static final DefaultRedisScript<Long> BEGIN_MUTATION = new DefaultRedisScript<>(
            """
            redis.call('SET', KEYS[2], ARGV[1], 'PX', ARGV[2])
            redis.call('DEL', KEYS[1])
            return 1
            """,
            Long.class
    );
    private static final DefaultRedisScript<Long> COMPLETE_MUTATION = new DefaultRedisScript<>(
            """
            local owner = redis.call('GET', KEYS[2])
            if owner and owner ~= ARGV[4] then
                return 0
            end
            local current = redis.call('HGET', KEYS[1], 'revision')
            if (not current) or tonumber(ARGV[1]) >= tonumber(current) then
                redis.call('HSET', KEYS[1], 'revision', ARGV[1], 'payload', ARGV[2])
                redis.call('PEXPIRE', KEYS[1], ARGV[3])
            end
            if owner then
                redis.call('DEL', KEYS[2])
            end
            return 1
            """,
            Long.class
    );
    private static final DefaultRedisScript<Long> CANCEL_MUTATION = new DefaultRedisScript<>(
            """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """,
            Long.class
    );

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final ResolutionCacheProperties properties;
    private final Counter mutationBeginFailures;
    private final Counter mutationCompletionSucceeded;
    private final Counter mutationCompletionSuperseded;
    private final Counter mutationCompletionFailures;
    private final Counter mutationCancellationSuperseded;

    public RedisLinkRuntimeCache(
            StringRedisTemplate redis,
            ObjectMapper objectMapper,
            ResolutionCacheProperties properties,
            MeterRegistry meterRegistry
    ) {
        properties.validate();
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.mutationBeginFailures = mutationCounter(meterRegistry, "begin_failed");
        this.mutationCompletionSucceeded = mutationCounter(meterRegistry, "completed");
        this.mutationCompletionSuperseded = mutationCounter(meterRegistry, "superseded");
        this.mutationCompletionFailures = mutationCounter(meterRegistry, "complete_failed");
        this.mutationCancellationSuperseded = mutationCounter(meterRegistry, "cancel_superseded");
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

    @Override
    public String beginLinkMutation(String host, String linkCode) {
        return beginMutation(linkKey(host, linkCode));
    }

    @Override
    public void completeLinkMutation(LinkRuntimeFacts link, String token) {
        completeMutation(
                linkKey(link.host(), link.linkCode()),
                link.revision(),
                link,
                properties.getLinkTtl(),
                token
        );
    }

    @Override
    public void cancelLinkMutation(String host, String linkCode, String token) {
        cancelMutation(linkKey(host, linkCode), token);
    }

    @Override
    public String beginDomainMutation(UUID domainId) {
        return beginMutation(domainKey(domainId));
    }

    @Override
    public void completeDomainMutation(DomainRuntimeState domain, String token) {
        completeMutation(
                domainKey(domain.domainId()),
                domain.revision(),
                domain,
                properties.getControlTtl(),
                token
        );
    }

    @Override
    public void cancelDomainMutation(UUID domainId, String token) {
        cancelMutation(domainKey(domainId), token);
    }

    @Override
    public String beginRestrictionsMutation(UUID userId) {
        return beginMutation(restrictionsKey(userId));
    }

    @Override
    public void completeRestrictionsMutation(UserSecurityRestrictionSet restrictions, String token) {
        completeMutation(
                restrictionsKey(restrictions.userId()),
                restrictions.revision(),
                restrictions,
                properties.getControlTtl(),
                token
        );
    }

    @Override
    public void cancelRestrictionsMutation(UUID userId, String token) {
        cancelMutation(restrictionsKey(userId), token);
    }

    private <T> Optional<T> read(String key, Class<T> type) {
        try {
            String json = redis.execute(
                    READ_IF_UNFENCED,
                    List.of(key, mutationKey(key))
            );
            if (json == null) {
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
                    List.of(key, mutationKey(key)),
                    Long.toString(revision),
                    json,
                    Long.toString(ttlMillis(baseTtl))
            );
        } catch (RuntimeException exception) {
            log.warn("Redis 跳转缓存写入失败，PostgreSQL 仍为权威: key={}", key, exception);
        }
    }

    private String beginMutation(String key) {
        String token = UUID.randomUUID().toString();
        try {
            Long result = redis.execute(
                    BEGIN_MUTATION,
                    List.of(key, mutationKey(key)),
                    token,
                    Long.toString(properties.getMutationFenceTtl().toMillis())
            );
            if (result == null || result != 1L) {
                throw new IllegalStateException("Redis 未确认写屏障");
            }
            return token;
        } catch (RuntimeException exception) {
            mutationBeginFailures.increment();
            throw new RuntimeCacheMutationException("无法建立运行时缓存写屏障", exception);
        }
    }

    private void completeMutation(
            String key,
            long revision,
            Object value,
            Duration ttl,
            String token
    ) {
        try {
            String json = objectMapper.writeValueAsString(value);
            Long result = redis.execute(
                    COMPLETE_MUTATION,
                    List.of(key, mutationKey(key)),
                    Long.toString(revision),
                    json,
                    Long.toString(ttlMillis(ttl)),
                    token
            );
            if (result == null) {
                throw new IllegalStateException("Redis 未返回写屏障完成结果");
            }
            if (result == 0L) {
                mutationCompletionSuperseded.increment();
                log.debug("Redis 写屏障已由更新事务接管: key={}", key);
                return;
            }
            if (result != 1L) {
                throw new IllegalStateException("Redis 返回未知写屏障完成结果: " + result);
            }
            mutationCompletionSucceeded.increment();
        } catch (RuntimeException exception) {
            mutationCompletionFailures.increment();
            throw new RuntimeCacheMutationException("无法完成运行时缓存变更", exception);
        }
    }

    private void cancelMutation(String key, String token) {
        try {
            Long result = redis.execute(CANCEL_MUTATION, List.of(mutationKey(key)), token);
            if (result == null) {
                throw new IllegalStateException("Redis 未返回写屏障撤销结果");
            }
            if (result == 0L) {
                mutationCancellationSuperseded.increment();
                log.debug("Redis 写屏障撤销已失去所有权: key={}", key);
            }
        } catch (RuntimeException exception) {
            throw new RuntimeCacheMutationException("无法释放运行时缓存写屏障", exception);
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
        return "lf:link:{" + host + ":" + linkCode + "}";
    }

    private static String domainKey(UUID domainId) {
        return "lf:domain-state:{" + domainId + "}";
    }

    private static String restrictionsKey(UUID userId) {
        return "lf:user-link-restrictions:{" + userId + "}";
    }

    private static String mutationKey(String key) {
        return key + ":mutation";
    }

    private static Counter mutationCounter(MeterRegistry meterRegistry, String result) {
        return Counter.builder("linkforge.cache.mutation.operations")
                .description("Redis runtime-cache mutation fence outcomes")
                .tag("result", result)
                .register(meterRegistry);
    }
}
