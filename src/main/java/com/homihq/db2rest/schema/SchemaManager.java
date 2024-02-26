package com.homihq.db2rest.schema;

import com.homihq.db2rest.model.DbTable;

import java.util.List;

public interface SchemaManager {
    DbTable getTable(String tableName);

    List<DbTable> findTables(String tableName);

    DbTable getOneTable(String schemaName, String tableName);

    com.homihq.db2rest.dialect.Dialect getDialect();
}
