package io.github.ranpers.linkforge.iam.infrastructure.persistence.mybatis;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TextArrayTypeHandlerTest {

    private final TextArrayTypeHandler typeHandler = new TextArrayTypeHandler();

    @Test
    void shouldBindListAsNativeTextArray() throws Exception {
        PreparedStatement statement = mock(PreparedStatement.class);
        Connection connection = mock(Connection.class);
        Array array = mock(Array.class);
        when(statement.getConnection()).thenReturn(connection);
        when(connection.createArrayOf("text", new Object[]{"USER", "ADMIN"})).thenReturn(array);

        typeHandler.setNonNullParameter(statement, 1, List.of("USER", "ADMIN"), JdbcType.ARRAY);

        verify(statement).setArray(1, array);
    }

    @Test
    void shouldReadListFromEverySupportedJdbcSource() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        CallableStatement statement = mock(CallableStatement.class);
        Array array = mock(Array.class);
        when(array.getArray()).thenReturn(new String[]{"group:create", "link:read"});
        when(resultSet.getArray("codes")).thenReturn(array);
        when(resultSet.getArray(1)).thenReturn(array);
        when(statement.getArray(1)).thenReturn(array);

        assertEquals(List.of("group:create", "link:read"), typeHandler.getNullableResult(resultSet, "codes"));
        assertEquals(List.of("group:create", "link:read"), typeHandler.getNullableResult(resultSet, 1));
        assertEquals(List.of("group:create", "link:read"), typeHandler.getNullableResult(statement, 1));
    }

    @Test
    void shouldTreatMissingArrayAsEmptyList() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getArray("codes")).thenReturn(null);

        assertEquals(List.of(), typeHandler.getNullableResult(resultSet, "codes"));
    }
}
