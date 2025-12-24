package com.frds.rest.service;

import com.frds.rest.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 14:51
 */
@Service
public class MetadataService {

  private final JdbcTemplate jdbcTemplate;

  public MetadataService(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Cacheable("tables")
  public Map<String, TableMetadata> getAllTables() {
    String sql = """
            SELECT 
                rdb$relation_name as table_name,
                rdb$description as description,
                rdb$view_source as view_source
            FROM rdb$relations
            WHERE (rdb$system_flag IS NULL OR rdb$system_flag = 0)
                AND rdb$relation_name NOT LIKE 'RDB$%'
                AND rdb$relation_name NOT LIKE 'SEC$%'
            ORDER BY rdb$relation_name
            """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      TableMetadata table = new TableMetadata();
      String name = rs.getString("table_name").trim();
      table.setName(name);
      table.setDescription(rs.getString("description"));
      table.setViewSource(rs.getString("view_source"));
      List<String> primaryKeys = getPrimaryKeys(name);
      table.setPrimaryKeys(primaryKeys);
      List<ColumnMetadata> columnsForTable = getColumnsForTable(name);
      columnsForTable.stream().filter(cm -> primaryKeys.contains(cm.getName())).forEach(cm -> cm.setPrimaryKey(true));
      table.setColumns(columnsForTable);
      //table.setForeignKeys(getForeignKeys(name));
      //table.setIndices(getIndices(name));
      return table;
    }).stream().collect(Collectors.toMap(TableMetadata::getName, Function.identity()));
  }

  private List<ColumnMetadata> getColumnsForTable(String tableName) {
    String sql = """
            SELECT 
                rf.rdb$field_name as column_name,
                f.rdb$field_type as field_type,
                f.rdb$field_length as field_length,
                f.rdb$field_scale as field_scale,
                f.rdb$field_sub_type as field_sub_type,
                rf.rdb$null_flag as is_nullable,
                rf.rdb$default_source as default_value,
                f.rdb$computed_source as computed_value,
                rf.rdb$description as description,
                rf.rdb$generator_name as generator_name,
                iif(f.rdb$character_set_id is null, null,\s
                    (select trim(cs.rdb$character_set_name) from rdb$character_sets cs
                    where cs.rdb$character_set_id = f.rdb$character_set_id)) as character_set,
                iif(f.rdb$collation_id is null, null,\s
                    (select trim(c.rdb$collation_name) from rdb$collations c
                    where c.rdb$collation_id = f.rdb$collation_id 
                    and c.rdb$character_set_id = f.rdb$character_set_id)) as collation       
            FROM rdb$relation_fields rf
            JOIN rdb$fields f ON rf.rdb$field_source = f.rdb$field_name
            WHERE rf.rdb$relation_name = ?
            ORDER BY rf.rdb$field_position
            """;

    return jdbcTemplate.query(sql, ps -> ps.setString(1, tableName),
        (rs, rowNum) -> {
          ColumnMetadata column = new ColumnMetadata();
          column.setName(rs.getString("column_name").trim());
          column.setType(determineFieldType(rs));
          column.setLength(rs.getInt("field_length"));
          column.setScale(rs.getInt("field_scale"));
          column.setNullable(rs.getObject("is_nullable") == null);
          column.setDefaultValue(rs.getString("default_value"));
          column.setComputed(rs.getString("computed_value") != null);
          column.setDescription(rs.getString("description"));
          column.setGeneratorName(rs.getString("generator_name"));
          column.setCharacterSet(rs.getString("character_set"));
          column.setCollation(rs.getString("collation"));
          return column;
        });
  }

  private List<String> getPrimaryKeys(String tableName) {
    String sql = """
            SELECT s.rdb$field_name as column_name
            FROM rdb$indices i
            JOIN rdb$index_segments s ON i.rdb$index_name = s.rdb$index_name
            WHERE i.rdb$relation_name = ?
                AND i.rdb$index_name STARTING WITH 'RDB$PRIMARY'
            ORDER BY s.rdb$field_position
            """;

    return jdbcTemplate.query(sql,
        ps -> ps.setString(1, tableName),
        (rs, rowNum) -> rs.getString("column_name").trim());
  }

  public Map<String, ProcedureMetadata> getAllProcedures() {
    String sql = """
            SELECT 
                rdb$procedure_name as procedure_name,
                rdb$description as description,
                rdb$procedure_inputs as input_params,
                rdb$procedure_outputs as output_params,
                rdb$procedure_source as source_code,
                rdb$procedure_type as procedure_type
            FROM rdb$procedures
            WHERE rdb$system_flag = 0 OR rdb$system_flag IS NULL
            ORDER BY rdb$procedure_name
            """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      ProcedureMetadata proc = new ProcedureMetadata();
      proc.setName(rs.getString("procedure_name").trim());
      proc.setDescription(rs.getString("description"));
      proc.setInputParams(rs.getInt("input_params"));
      proc.setOutputParams(rs.getInt("output_params"));
      proc.setSourceCode(rs.getString("source_code"));
      proc.setType(rs.getInt("procedure_type") == 1 ? "SELECTABLE" : "EXECUTABLE");
      proc.setParameters(getProcedureParameters(proc.getName()));
      return proc;
    }).stream().collect(Collectors.toMap(ProcedureMetadata::getName, Function.identity()));
  }

  private List<ParameterMetadata> getProcedureParameters(String procedureName) {
    String sql = """
            SELECT 
                rdb$parameter_name as param_name,
                rdb$parameter_type as param_type,
                trim(rdb$field_source) as field_type,
                rdb$description as description
            FROM rdb$procedure_parameters
            WHERE rdb$procedure_name = ?
            ORDER BY rdb$parameter_number
            """;

    return jdbcTemplate.query(sql,
        ps -> ps.setString(1, procedureName),
        (rs, rowNum) -> {
          ParameterMetadata param = new ParameterMetadata();
          param.setName(rs.getString("param_name").trim());
          param.setType(rs.getInt("param_type") == 0 ? "INPUT" : "OUTPUT");
          param.setDataType(rs.getString("field_type"));
          param.setDescription(rs.getString("description"));
          return param;
        });
  }

  private String determineFieldType(java.sql.ResultSet rs) throws java.sql.SQLException {
    int type = rs.getInt("field_type");
    int subType = rs.getInt("field_sub_type");
    int scale = rs.getInt("field_scale");

    Map<Integer, String> typeMap = Map.ofEntries(
        Map.entry(7, scale < 0 ? "DECIMAL" : "SMALLINT"),
        Map.entry(8, scale < 0 ? "DECIMAL" : "INTEGER"),
        Map.entry(10, "FLOAT"),
        Map.entry(12, "DATE"),
        Map.entry(13, "TIME"),
        Map.entry(14, "CHAR"),
        Map.entry(16, "BIGINT"),
        Map.entry(27, "DOUBLE"),
        Map.entry(35, "TIMESTAMP"),
        Map.entry(37, "VARCHAR"),
        Map.entry(261, subType == 1 ? "TEXT" : "BLOB")
    );

    return typeMap.getOrDefault(type, "UNKNOWN");
  }

  // Методы для foreign keys и indices
  private List<ForeignKeyMetadata> getForeignKeys(String tableName) {
    // Реализация получения внешних ключей
    return new ArrayList<>();
  }

  private List<IndexMetadata> getIndices(String tableName) {
    // Реализация получения индексов
    return new ArrayList<>();
  }
}
