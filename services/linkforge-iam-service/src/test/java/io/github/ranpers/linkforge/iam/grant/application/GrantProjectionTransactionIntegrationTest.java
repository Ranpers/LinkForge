package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.role.domain.RoleCode;
import io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role.MybatisUserRoleAssignment;
import io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role.UserRoleMapper;
import io.github.ranpers.linkforge.iam.user.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(
        properties = {
                "spring.security.oauth2.authorizationserver.client.smoke-test.registration.client-secret={noop}integration-secret"
        }
)
class GrantProjectionTransactionIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18-alpine")
    );

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private MybatisUserRoleAssignment roleAssignment;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void shouldCommitBindingProjectionAndOutboxAtomically() {
        UUID userId = insertUser("success");
        UUID domainId = insertDomain("success");
        UUID roleId = roleId(RoleCode.ADMIN);
        jdbc.update(
                "INSERT INTO t_role_domain (role_id, domain_id) VALUES (?, ?)",
                roleId,
                domainId
        );

        roleAssignment.assign(new UserId(userId), RoleCode.ADMIN);

        assertEquals(1, count(
                "SELECT count(*) FROM t_user_role WHERE user_id = ? AND role_id = ?",
                userId,
                roleId
        ));
        assertTrue(Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT granted FROM t_user_domain_grant_state WHERE user_id = ? AND domain_id = ?",
                Boolean.class,
                userId,
                domainId
        )));
        assertEquals(1L, jdbc.queryForObject(
                "SELECT revision FROM t_user_domain_grant_state WHERE user_id = ? AND domain_id = ?",
                Long.class,
                userId,
                domainId
        ));
        assertEquals(1, count(
                "SELECT count(*) FROM t_outbox_event WHERE stream_key = ?",
                streamKey(userId, domainId)
        ));
    }

    @Test
    void shouldRollBackBindingProjectionAndOutboxWhenMutationFails() {
        UUID userId = insertUser("rollback");
        UUID lowDomainId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID highDomainId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        insertDomain(lowDomainId, "rollback-low");
        insertDomain(highDomainId, "rollback-high");
        UUID roleId = roleId(RoleCode.ADMIN);
        jdbc.update(
                "INSERT INTO t_role_domain (role_id, domain_id) VALUES (?, ?), (?, ?)",
                roleId,
                lowDomainId,
                roleId,
                highDomainId
        );
        // 让最后一个流的 Outbox 写入失败,确保失败前绑定、投影及至少一个事件已经写过。
        jdbc.execute("""
                ALTER TABLE t_outbox_event
                ADD CONSTRAINT ck_test_reject_last_outbox
                CHECK (stream_key <> '%s')
                """.formatted(streamKey(userId, highDomainId)));

        assertThrows(
                RuntimeException.class,
                () -> roleAssignment.assign(new UserId(userId), RoleCode.ADMIN)
        );

        assertEquals(0, count(
                "SELECT count(*) FROM t_user_role WHERE user_id = ? AND role_id = ?",
                userId,
                roleId
        ));
        assertEquals(0, count(
                "SELECT count(*) FROM t_user_domain_grant_state WHERE user_id = ? AND domain_id IN (?, ?)",
                userId,
                lowDomainId,
                highDomainId
        ));
        assertEquals(0, count(
                "SELECT count(*) FROM t_outbox_event WHERE stream_key IN (?, ?)",
                streamKey(userId, lowDomainId),
                streamKey(userId, highDomainId)
        ));
    }

    @Test
    void shouldSerializeRoleAssignmentOnRoleTopologyAnchor() throws Exception {
        UUID userId = insertUser("role-lock");
        UUID domainId = insertDomain("role-lock");
        UUID roleId = roleId(RoleCode.ADMIN);
        jdbc.update(
                "INSERT INTO t_role_domain (role_id, domain_id) VALUES (?, ?)",
                roleId,
                domainId
        );

        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> blocker = executor.submit(() -> transaction.executeWithoutResult(status -> {
                assertNotNull(userRoleMapper.lockRoleExclusiveByCode(RoleCode.ADMIN.name()));
                firstLocked.countDown();
                await(releaseFirst, Duration.ofSeconds(10));
            }));

            assertTrue(firstLocked.await(10, TimeUnit.SECONDS), "首个事务未取得角色锚点锁");

            Future<?> assignment = executor.submit(
                    () -> roleAssignment.assign(new UserId(userId), RoleCode.ADMIN)
            );
            assertThrows(TimeoutException.class, () -> assignment.get(300, TimeUnit.MILLISECONDS));
            assertFalse(assignment.isDone(), "角色分配不应绕过另一个事务持有的角色锚点锁");
            assertEquals(0, count(
                    "SELECT count(*) FROM t_user_role WHERE user_id = ? AND role_id = ?",
                    userId,
                    roleId
            ));

            releaseFirst.countDown();
            blocker.get(10, TimeUnit.SECONDS);
            assignment.get(10, TimeUnit.SECONDS);
        } finally {
            releaseFirst.countDown();
        }

        assertEquals(1, count(
                "SELECT count(*) FROM t_user_role WHERE user_id = ? AND role_id = ?",
                userId,
                roleId
        ));
        assertTrue(Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT granted FROM t_user_domain_grant_state WHERE user_id = ? AND domain_id = ?",
                Boolean.class,
                userId,
                domainId
        )));
        assertEquals(1, count(
                "SELECT count(*) FROM t_outbox_event WHERE stream_key = ?",
                streamKey(userId, domainId)
        ));
    }

    @Test
    void shouldAllowConcurrentSharedRoleAnchorLocks() throws Exception {
        CountDownLatch firstLocked = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> first = executor.submit(() -> transaction.executeWithoutResult(status -> {
                assertNotNull(userRoleMapper.lockRoleSharedByCode(RoleCode.USER.name()));
                firstLocked.countDown();
                await(releaseFirst, Duration.ofSeconds(10));
            }));
            assertTrue(firstLocked.await(10, TimeUnit.SECONDS), "首个共享角色锚点锁未取得");

            Future<?> second = executor.submit(() -> transaction.executeWithoutResult(status ->
                    assertNotNull(userRoleMapper.lockRoleSharedByCode(RoleCode.USER.name()))
            ));
            second.get(2, TimeUnit.SECONDS);

            releaseFirst.countDown();
            first.get(10, TimeUnit.SECONDS);
        } finally {
            releaseFirst.countDown();
        }
    }

    private UUID insertUser(String marker) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO t_user (id, username, password) VALUES (?, ?, ?)",
                id,
                marker + "-" + id,
                "{noop}test"
        );
        return id;
    }

    private UUID insertDomain(String marker) {
        return insertDomain(UUID.randomUUID(), marker);
    }

    private UUID insertDomain(UUID id, String marker) {
        jdbc.update(
                "INSERT INTO t_domain (id, domain, name) VALUES (?, ?, ?)",
                id,
                marker + "-" + id + ".example.test",
                marker
        );
        return id;
    }

    private UUID roleId(RoleCode roleCode) {
        UUID id = jdbc.queryForObject(
                "SELECT id FROM t_role WHERE code = ?",
                UUID.class,
                roleCode.name()
        );
        assertNotNull(id);
        return id;
    }

    private int count(String sql, Object... args) {
        Integer result = jdbc.queryForObject(sql, Integer.class, args);
        return result == null ? 0 : result;
    }

    private static String streamKey(UUID userId, UUID domainId) {
        return "USER_DOMAIN:" + userId + ":" + domainId;
    }

    private static void await(CountDownLatch latch, Duration timeout) {
        try {
            if (!latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new AssertionError("等待并发测试信号超时");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError("等待并发测试信号被中断", exception);
        }
    }
}
