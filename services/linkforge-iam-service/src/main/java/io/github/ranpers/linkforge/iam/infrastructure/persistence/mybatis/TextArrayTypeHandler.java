package io.github.ranpers.linkforge.iam.infrastructure.persistence.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * PostgreSQL text[] 与 Java List&lt;String&gt; 的 MyBatis 映射。
 *
 * <p>供聚合查询(array_agg)把角色码/权限码等集合收进单行返回,
 * 避免为凑齐一个聚合对象而串行补发查询。</p>
 */
@MappedTypes(List.class)
@MappedJdbcTypes(value = JdbcType.ARRAY, includeNullJdbcType = true)
public final class TextArrayTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(
            PreparedStatement statement,
            int parameterIndex,
            List<String> parameter,
            JdbcType jdbcType
    ) throws SQLException {
        Array array = statement.getConnection().createArrayOf("text", parameter.toArray());
        statement.setArray(parameterIndex, array);
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
        return readArray(resultSet.getArray(columnName));
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, int columnIndex) throws SQLException {
        return readArray(resultSet.getArray(columnIndex));
    }

    @Override
    public List<String> getNullableResult(CallableStatement statement, int columnIndex) throws SQLException {
        return readArray(statement.getArray(columnIndex));
    }

    private List<String> readArray(Array array) throws SQLException {
        if (array == null) {
            return List.of();
        }
        try {
            return List.of((String[]) array.getArray());
        } finally {
            array.free();
        }
    }
}
