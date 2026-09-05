package io.github.ranpers.linkforge.link.creation.adapter.out.persistence;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ShortLinkMapperXmlTest {

    @Test
    void parsesCreationStatements() throws Exception {
        String resource = "mapper/creation/ShortLinkMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(
                    inputStream,
                    configuration,
                    resource,
                    configuration.getSqlFragments()
            ).parse();
        }
        String namespace =
                "io.github.ranpers.linkforge.link.creation.adapter.out.persistence.ShortLinkMapper";
        assertTrue(configuration.hasStatement(namespace + ".findByIdempotencyKey"));
        assertTrue(configuration.hasStatement(namespace + ".groupBelongsToUser"));
        assertTrue(configuration.hasStatement(namespace + ".tryInsert"));
        assertTrue(configuration.hasStatement(namespace + ".existsById"));
        assertTrue(configuration.hasStatement(namespace + ".existsByDomainAndCode"));
    }
}
