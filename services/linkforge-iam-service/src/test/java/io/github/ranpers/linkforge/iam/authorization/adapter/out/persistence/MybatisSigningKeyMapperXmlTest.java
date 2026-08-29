package io.github.ranpers.linkforge.iam.authorization.adapter.out.persistence;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MybatisSigningKeyMapperXmlTest {

    @Test
    void shouldParseSigningKeyMapperXml() throws Exception {
        String resource = "mapper/authorization/SigningKeyMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(
                    inputStream,
                    configuration,
                    resource,
                    configuration.getSqlFragments()
            ).parse();
        }
        String namespace = SigningKeyMapper.class.getName();

        assertTrue(configuration.hasStatement(namespace + ".acquireInitializationLock"));
        assertTrue(configuration.hasStatement(namespace + ".findActive"));
        assertTrue(configuration.hasStatement(namespace + ".insert"));
    }
}
