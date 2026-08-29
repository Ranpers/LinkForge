package io.github.ranpers.linkforge.iam.config.mybatis;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UuidTypeHandlerTest {

    private final UuidTypeHandler typeHandler = new UuidTypeHandler();
    private final UUID uuid = UUID.fromString("0198f7c4-3ee6-7000-8000-000000000001");

    @Test
    void shouldBindUuidAsNativeJdbcObject() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);

        typeHandler.setNonNullParameter(statement, 1, uuid, JdbcType.OTHER);

        verify(statement).setObject(1, uuid);
    }

    @Test
    void shouldReadUuidFromEverySupportedJdbcSource() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        CallableStatement statement = mock(CallableStatement.class);
        when(resultSet.getObject("id", UUID.class)).thenReturn(uuid);
        when(resultSet.getObject(1, UUID.class)).thenReturn(uuid);
        when(statement.getObject(1, UUID.class)).thenReturn(uuid);

        assertEquals(uuid, typeHandler.getNullableResult(resultSet, "id"));
        assertEquals(uuid, typeHandler.getNullableResult(resultSet, 1));
        assertEquals(uuid, typeHandler.getNullableResult(statement, 1));
    }
}
