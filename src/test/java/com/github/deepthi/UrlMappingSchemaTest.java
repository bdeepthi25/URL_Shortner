
package com.github.deepthi;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("schema-test")
class UrlMappingSchemaTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldCreateOrUpdateUrlMappingSchema() throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();

            Set<String> columnNames = new HashSet<>();

            try (ResultSet columns = metaData.getColumns(
                    connection.getCatalog(),
                    "public",
                    "url_mapping",
                    "%")) {

                while (columns.next()) {
                    columnNames.add(columns.getString("COLUMN_NAME"));
                }
            }

            assertThat(columnNames).containsExactlyInAnyOrder(
                    "id",
                    "short_code",
                    "original_url",
                    "created_at",
                    "expires_at"
            );
        }
    }

    @Test
    void shouldHaveExpectedColumnTypesLengthsAndNullability()
            throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();

            Map<String, Integer> columnTypes = new HashMap<>();
            Map<String, Integer> columnSizes = new HashMap<>();
            Map<String, Integer> columnNullability = new HashMap<>();

            try (ResultSet columns = metaData.getColumns(
                    connection.getCatalog(),
                    "public",
                    "url_mapping",
                    "%")) {

                while (columns.next()) {

                    String columnName = columns.getString("COLUMN_NAME");

                    columnTypes.put(
                            columnName,
                            columns.getInt("DATA_TYPE"));

                    columnSizes.put(
                            columnName,
                            columns.getInt("COLUMN_SIZE"));

                    columnNullability.put(
                            columnName,
                            columns.getInt("NULLABLE"));
                }
            }

            // Verify database column types.
            assertThat(columnTypes.get("id"))
                    .isEqualTo(Types.BIGINT);

            assertThat(columnTypes.get("short_code"))
                    .isEqualTo(Types.VARCHAR);

            assertThat(columnTypes.get("original_url"))
                    .isEqualTo(Types.VARCHAR);

            assertThat(columnTypes.get("created_at"))
                    .isEqualTo(Types.TIMESTAMP);

            assertThat(columnTypes.get("expires_at"))
                    .isEqualTo(Types.TIMESTAMP);

            // Verify VARCHAR column lengths.
            assertThat(columnSizes.get("short_code"))
                    .isEqualTo(10);

            assertThat(columnSizes.get("original_url"))
                    .isEqualTo(2048);

            // Verify required columns do not allow NULL.
            assertThat(columnNullability.get("id"))
                    .isEqualTo(DatabaseMetaData.columnNoNulls);

            assertThat(columnNullability.get("short_code"))
                    .isEqualTo(DatabaseMetaData.columnNoNulls);

            assertThat(columnNullability.get("original_url"))
                    .isEqualTo(DatabaseMetaData.columnNoNulls);

            assertThat(columnNullability.get("created_at"))
                    .isEqualTo(DatabaseMetaData.columnNoNulls);

            // expires_at is optional.
            assertThat(columnNullability.get("expires_at"))
                    .isEqualTo(DatabaseMetaData.columnNullable);
        }
    }

    @Test
    void shouldHavePrimaryKeyAndUniqueShortCodeConstraint()
            throws SQLException {

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();

            Set<String> primaryKeyColumns = new HashSet<>();

            try (ResultSet primaryKeys = metaData.getPrimaryKeys(
                    connection.getCatalog(),
                    "public",
                    "url_mapping")) {

                while (primaryKeys.next()) {
                    primaryKeyColumns.add(
                            primaryKeys.getString("COLUMN_NAME"));
                }
            }

            // id must be the primary key.
            assertThat(primaryKeyColumns)
                    .containsExactly("id");

            Set<String> uniqueColumns = new HashSet<>();

            try (ResultSet indexes = metaData.getIndexInfo(
                    connection.getCatalog(),
                    "public",
                    "url_mapping",
                    true,
                    false)) {

                while (indexes.next()) {

                    String columnName = indexes.getString("COLUMN_NAME");

                    if (columnName != null) {
                        uniqueColumns.add(columnName);
                    }
                }
            }

            // short_code must have a unique index or constraint.
            assertThat(uniqueColumns)
                    .contains("short_code");
        }
    }
}
