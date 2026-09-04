package io.github.ranpers.linkforge.iam.user.adapter.out.persistence;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MybatisMapperXmlTest {

    @Test
    void shouldParseUserRoleMapperXml() throws Exception {
        Configuration configuration = parse("mapper/user/UserRoleMapper.xml");

        assertTrue(configuration.hasStatement(
                "io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role.UserRoleMapper"
                        + ".insertByRoleCode"
        ));
    }

    @Test
    void shouldParseLinkAuthorizationMapperXml() throws Exception {
        Configuration configuration = parse("mapper/grant/LinkAuthorizationMapper.xml");
        String namespace =
                "io.github.ranpers.linkforge.iam.grant.adapter.out.persistence.LinkAuthorizationMapper";

        assertTrue(configuration.hasStatement(namespace + ".findSnapshot"));
        assertTrue(configuration.hasStatement(namespace + ".findManagementSnapshot"));
    }

    @Test
    void shouldParseDomainAvailabilityMapperXml() throws Exception {
        Configuration configuration = parse("mapper/domain/DomainAvailabilityMapper.xml");
        String namespace =
                "io.github.ranpers.linkforge.iam.domain.adapter.out.persistence.DomainAvailabilityMapper";

        assertTrue(configuration.hasStatement(namespace + ".change"));
    }

    @Test
    void shouldParseLinkSecurityRestrictionMapperXml() throws Exception {
        Configuration configuration = parse("mapper/security/LinkSecurityRestrictionMapper.xml");
        String namespace =
                "io.github.ranpers.linkforge.iam.security.adapter.out.persistence.LinkSecurityRestrictionMapper";

        assertTrue(configuration.hasStatement(namespace + ".actorAllowed"));
        assertTrue(configuration.hasStatement(namespace + ".lockTargetUser"));
        assertTrue(configuration.hasStatement(namespace + ".insertRestriction"));
        assertTrue(configuration.hasStatement(namespace + ".restrictionActive"));
        assertTrue(configuration.hasStatement(namespace + ".revokeRestriction"));
        assertTrue(configuration.hasStatement(namespace + ".incrementRevision"));
        assertTrue(configuration.hasStatement(namespace + ".appendSnapshotEvent"));
    }

    @Test
    void shouldParseUserSecurityStatusMapperXml() throws Exception {
        Configuration configuration = parse("mapper/security/UserSecurityStatusMapper.xml");
        String namespace =
                "io.github.ranpers.linkforge.iam.security.adapter.out.persistence.UserSecurityStatusMapper";

        assertTrue(configuration.hasStatement(namespace + ".change"));
    }

    @Test
    void shouldParseOutboxDispatchMapperXml() throws Exception {
        Configuration configuration = parse("mapper/grant/OutboxDispatchMapper.xml");
        String namespace =
                "io.github.ranpers.linkforge.iam.grant.adapter.out.persistence.OutboxDispatchMapper";

        assertTrue(configuration.hasStatement(namespace + ".lockDueRows"));
        assertTrue(configuration.hasStatement(namespace + ".markSent"));
        assertTrue(configuration.hasStatement(namespace + ".scheduleRetry"));
        assertTrue(configuration.hasStatement(namespace + ".park"));
    }

    @Test
    void shouldParseLoginUserMapperXml() throws Exception {
        Configuration configuration = parse("mapper/user/LoginUserMapper.xml");
        String namespace =
                "io.github.ranpers.linkforge.iam.user.adapter.out.persistence.auth.LoginUserMapper";

        assertTrue(configuration.hasStatement(namespace + ".findByUsername"));
        assertTrue(configuration.hasResultMap(namespace + ".loginUserRowMap"));
    }

    private Configuration parse(String resource) throws Exception {
        Configuration configuration = new Configuration();
        // 与 application.yml 的 mybatis-flex.type-handlers-package 保持一致
        configuration.getTypeHandlerRegistry().register(
                "io.github.ranpers.linkforge.iam.infrastructure.persistence.mybatis");
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(
                    inputStream,
                    configuration,
                    resource,
                    configuration.getSqlFragments()
            ).parse();
        }
        return configuration;
    }
}
