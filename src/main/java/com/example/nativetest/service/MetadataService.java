package com.example.nativetest.service;

import com.example.nativetest.data.entity.DbConnectionInfo;
import com.example.nativetest.data.repository.DbConnectionInfoRepository;
import com.example.nativetest.rest.exception.EntityNotFoundException;
import com.example.nativetest.rest.exception.MetadataServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.*;

@Component(MetadataService.NAME)
public class MetadataService {

    public static final String NAME = "MetadataService";

    protected Set<Integer> numericTypes = new HashSet<>() {{
        add(Types.SMALLINT);
        add(Types.INTEGER);
        add(Types.BIGINT);
        add(Types.REAL);
        add(Types.DOUBLE);
        add(Types.NUMERIC);
    }};

    protected Set<Integer> typesSuitableForMinMax = new HashSet<>() {{
        add(Types.DATE);
        add(Types.TIME);
        add(Types.TIME_WITH_TIMEZONE);
        add(Types.TIMESTAMP_WITH_TIMEZONE);
        add(Types.TIMESTAMP);
        add(Types.VARCHAR);
        add(Types.CHAR);
    }};

    @Autowired
    protected DbConnectionInfoRepository repository;

    public List<Map<String, String>> getTables(String name) {
        DbConnectionInfo info = repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Database connection info", name));
        List<Map<String, String>> tables = new ArrayList<>();
        try (Connection con = DriverManager
                .getConnection(
                        getConnectionUrl(info),
                        info.getUsername(),
                        info.getPassword())) {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, null, new String[]{"TABLE"});
            while (resultSet.next()) {
                Map<String, String> m = new HashMap<>();
                String tableName = resultSet.getString("TABLE_NAME");
                m.put("name", tableName);
                String type = resultSet.getString("TABLE_TYPE");
                m.put("type", type);
                String cat = resultSet.getString("TABLE_CAT");
                if (StringUtils.hasText(cat)) {
                    m.put("catalog", cat);
                }
                String schem = resultSet.getString("TABLE_SCHEM");
                if (StringUtils.hasText(schem)) {
                    m.put("schema", schem);
                }
                String typeCat = resultSet.getString("TYPE_CAT");
                if (StringUtils.hasText(typeCat)) {
                    m.put("typesCatalog", typeCat);
                }
                String typeSchem = resultSet.getString("TYPE_SCHEM");
                if (StringUtils.hasText(typeSchem)) {
                    m.put("typesSchema", typeSchem);
                }
                String typeName = resultSet.getString("TYPE_NAME");
                if (StringUtils.hasText(typeName)) {
                    m.put("typeName", typeName);
                }
                String remarks = resultSet.getString("REMARKS");
                if (StringUtils.hasText(remarks)) {
                    m.put("remarks", remarks);
                }
                tables.add(m);
            }
        } catch (SQLException e) {
            throw new MetadataServiceException(e);
        }
        return tables;
    }

    public List<Map<String, String>> getSchemas(String name) {
        DbConnectionInfo info = repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Database connection info", name));
        List<Map<String, String>> schemas = new ArrayList<>();

        try (Connection con = DriverManager
                .getConnection(
                        getConnectionUrl(info),
                        info.getUsername(),
                        info.getPassword())) {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet resultSet = metaData.getSchemas();
            while (resultSet.next()) {
                Map<String, String> m = new HashMap<>();
                String schemaName = resultSet.getString("TABLE_SCHEM");
                m.put("schema", schemaName);
                String catName = resultSet.getString("TABLE_CATALOG");
                if (StringUtils.hasText(catName)) {
                    m.put("catalog", catName);
                }
                schemas.add(m);
            }
        } catch (SQLException e) {
            throw new MetadataServiceException(e);
        }
        return schemas;
    }

    public List<Map<String, String>> getColumns(String name, String tableName) {
        DbConnectionInfo info = repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Database connection info", name));
        List<Map<String, String>> columns = new ArrayList<>();
        Set<String> pkNames = new HashSet<>();
        try (Connection con = DriverManager
                .getConnection(
                        getConnectionUrl(info),
                        info.getUsername(),
                        info.getPassword())) {
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet resultSet = metaData.getPrimaryKeys(null, null, tableName);
            while (resultSet.next()) {
                String pkName = resultSet.getString("COLUMN_NAME");
                pkNames.add(pkName);
            }
            resultSet = metaData.getColumns(null, null, tableName, null);
            while (resultSet.next()) {
                Map<String, String> m = new HashMap<>();
                String columnName = resultSet.getString("COLUMN_NAME");
                m.put("name", columnName);
                if (pkNames.contains(columnName)) {
                    m.put("primaryKey", "YES");
                }
                int dataType = resultSet.getInt("DATA_TYPE");
                m.put("dataType", JDBCType.valueOf(dataType).getName());
                String typeName = resultSet.getString("TYPE_NAME");
                if (StringUtils.hasText(typeName)) {
                    m.put("typeName", typeName);
                }
                int columnSize = resultSet.getInt("COLUMN_SIZE");
                m.put("columnSize", String.valueOf(columnSize));
                String nullable = resultSet.getString("IS_NULLABLE");
                if (StringUtils.hasText(nullable)) {
                    m.put("nullable", nullable);
                }
                String autoincrement = resultSet.getString("IS_AUTOINCREMENT");
                if (StringUtils.hasText(autoincrement)) {
                    m.put("autoincrement", autoincrement);
                }
                String generatedColumn = resultSet.getString("IS_GENERATEDCOLUMN");
                if (StringUtils.hasText(generatedColumn)) {
                    m.put("generatedColumn", generatedColumn);
                }
                columns.add(m);
            }
        } catch (SQLException e) {
            throw new MetadataServiceException(e);
        }
        return columns;
    }

    public List<Map<String, Object>> getDataPreview(String name, String tableName) {
        DbConnectionInfo info = repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Database connection info", name));

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(getConnectionUrl(info));
        dataSource.setUsername(info.getUsername());
        dataSource.setPassword(info.getPassword());

        try {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            if (resultSet.next()) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.setMaxRows(50);

                return jdbcTemplate.queryForList("select * from " + tableName);
            } else {
                throw new MetadataServiceException("No table with name "
                        + tableName
                        + " found for database connection "
                        + name);
            }
        } catch (SQLException e) {
            throw new MetadataServiceException(e);
        }
    }

    public List<Map<String, Object>> getColumnStats(String name, String tableName) {
        DbConnectionInfo info = repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Database connection info", name));

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(getConnectionUrl(info));
        dataSource.setUsername(info.getUsername());
        dataSource.setPassword(info.getPassword());

        try {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{"TABLE"});
            if (resultSet.next()) {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

                List<Map<String, Object>> result = new ArrayList<>();

                ResultSet columnsResultSet = metaData.getColumns(null, null, tableName, null);
                while (columnsResultSet.next()) {
                    Map<String, Object> columnStats = new HashMap<>();
                    String columnName = columnsResultSet.getString("COLUMN_NAME");
                    columnStats.put("name", columnName);
                    int dataType = columnsResultSet.getInt("DATA_TYPE");
                    columnStats.put("dataType", JDBCType.valueOf(dataType).getName());
                    if (numericTypes.contains(dataType)) {
                        columnStats.putAll(
                                jdbcTemplate.queryForMap(
                                        "SELECT AVG("
                                                + columnName
                                                + ") AS mean, PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY "
                                                + columnName
                                                + ") AS median, MIN("
                                                + columnName
                                                + ") AS minimum, MAX("
                                                + columnName
                                                + ") AS maximum from "
                                                + tableName
                                )
                        );
                    } else if (typesSuitableForMinMax.contains(dataType)) {
                        columnStats.putAll(
                                jdbcTemplate.queryForMap(
                                        "SELECT MIN("
                                                + columnName
                                                + ") AS minimum, MAX("
                                                + columnName
                                                + ") AS maximum from "
                                                + tableName)
                        );
                    }
                    result.add(columnStats);
                }
                return result;
            } else {
                throw new MetadataServiceException("No table with name "
                        + tableName
                        + " found for database connection "
                        + name);
            }
        } catch (SQLException e) {
            throw new MetadataServiceException(e);
        }
    }

    public List<Map<String, Object>> getTableStats(String name) {
        DbConnectionInfo info = repository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Database connection info", name));

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(getConnectionUrl(info));
        dataSource.setUsername(info.getUsername());
        dataSource.setPassword(info.getPassword());
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            ResultSet resultSet = metaData.getTables(null, null, null, new String[]{"TABLE"});
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            while (resultSet.next()) {
                Map<String, Object> tableStats = new HashMap<>();
                String tableName = resultSet.getString("TABLE_NAME");
                tableStats.put("name", tableName);
                ResultSet columnsResultSet = metaData.getColumns(null, null, tableName, null);
                columnsResultSet.last();
                tableStats.put("columns", columnsResultSet.getRow());
                tableStats.put("rows",
                        jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM " + tableName,
                                Integer.class)
                );
                result.add(tableStats);
            }
            return result;
        } catch (SQLException e) {
            throw new MetadataServiceException(e);
        }
    }

    protected String getConnectionUrl(DbConnectionInfo info) {
        StringBuilder sb = new StringBuilder("jdbc:postgresql://");
        sb.append(info.getHostname());
        sb.append(":");
        sb.append(info.getPort());
        sb.append("/");
        sb.append(info.getDatabaseName());
        return sb.toString();
    }
}
