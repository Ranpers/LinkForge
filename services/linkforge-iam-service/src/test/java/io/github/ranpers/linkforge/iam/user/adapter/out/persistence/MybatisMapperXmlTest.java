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
    void shouldParseLoginUserMapperXml() throws Exception {
        Configuration configuration = parse("mapper/user/LoginUserMapper.xml");
        String namespace =
                "io.github.ranpers.linkforge.iam.user.adapter.out.persistence.auth.LoginUserMapper";

        assertTrue(configuration.hasStatement(namespace + ".findByUsername"));
        assertTrue(configuration.hasResultMap(namespace + ".loginUserRowMap"));
    }

    private Configuration parse(String resource) throws Exception {
        Configuration configuration = new Configuration();
        // 与 application.yml 的 mybatis-plus.type-handlers-package 保持一致
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
