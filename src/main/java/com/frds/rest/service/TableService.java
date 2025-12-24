package com.frds.rest.service;

import com.frds.rest.exception.ObjectNotFoundException;
import com.frds.rest.model.ColumnMetadata;
import com.frds.rest.model.TableMetadata;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 15:19
 */

@Service
public class TableService {

  private final NamedParameterJdbcTemplate namedJdbcTemplate;
  private final MetadataService metadataService;

  public TableService(JdbcTemplate jdbcTemplate, MetadataService metadataService) {
    this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    this.metadataService = metadataService;
  }

  public List<Map<String, Object>> getTableData(String tableName,
                                                Integer limit,
                                                Integer offset,
                                                String fields,
                                                String filter,
                                                String sort) {

    StringBuilder sql = new StringBuilder("SELECT ");

    if (limit != null)
      sql.append("FIRST ").append(limit).append(" ");
    if (offset != null)
      sql.append("SKIP ").append(offset).append(" ");

    // Выбор полей
    if (StringUtils.hasText(fields)) {
      sql.append(fields);
    } else {
      sql.append("*");
    }

    sql.append(" FROM \"").append(tableName).append("\"");

    // Фильтрация
    if (StringUtils.hasText(filter)) {
      sql.append(" WHERE ").append(filter);
    }

    // Сортировка
    if (StringUtils.hasText(sort)) {
      sql.append(" ORDER BY ").append(sort);
    }

    return namedJdbcTemplate.queryForList(sql.toString(), new HashMap<>());
  }

  public Map<String, Object> getRecordById(String tableName, String keys) {
    MapSqlParameterSource params = new MapSqlParameterSource();
    String whereClause = prepareKeys(tableName, keys, params);
    String sql = "SELECT * FROM \"" + tableName + "\" WHERE " + whereClause;
    try {
      return namedJdbcTemplate.queryForMap(sql, params);
    } catch (EmptyResultDataAccessException e) {
      throw new ObjectNotFoundException("Запись с указанным ключом не найдена", e);
    }
  }

  public Map<String, Object> createRecord(String tableName, Map<String, Object> data) {
    StringBuilder columns = new StringBuilder();
    StringBuilder values = new StringBuilder();
    MapSqlParameterSource params = new MapSqlParameterSource();
    List<ColumnMetadata> columnMetadata = getTableMetadata(tableName).getColumns();
    columnMetadata.forEach(cm -> {
      if (cm.isComputed())
        throw new RuntimeException(String.format("Данные содержат вычисляемое поле '%s'", cm.getName()));
      /*if (!cm.isNullable() && !data.containsKey(cm.getName()))
        throw new RuntimeException(String.format("Данные не содержат обязательного поля '%s'", cm.getName()));*/
    });
    data.forEach((key, value) -> {
      if (columns.length() > 0) {
        columns.append(", ");
        values.append(", ");
      }
      columns.append("\"").append(key).append("\"");
      values.append(":").append(key);
      params.addValue(key, value);
    });

    String sql = String.format(
        "INSERT INTO \"%s\" (%s) VALUES (%s) RETURNING %s",
        tableName, columns, values, getTableMetadata(tableName).getColumns().stream()
            .map(s -> "\"" + s.getName() + "\"").collect(Collectors.joining(","))
    );

    return namedJdbcTemplate.queryForMap(sql, params);
  }

  public Map<String, Object> updateRecord(String tableName,
                                          String keys,
                                          Map<String, Object> data, boolean partitional) {
    primaryKeysValidation(tableName);
    MapSqlParameterSource params = new MapSqlParameterSource();
    StringBuilder setClause = new StringBuilder();
    List<ColumnMetadata> columnMetadata = getTableMetadata(tableName).getColumns();
    Set<String> computed = new HashSet<>();
    Set<String> primary = new HashSet<>();
    Set<String> nulls = new HashSet<>();
    Set<String> columns = new HashSet<>();
    columnMetadata.forEach(cm -> {
      if (partitional && !cm.isNullable() && !cm.isPrimaryKey() && !data.containsKey(cm.getName()))
        throw new RuntimeException(String.format("Данные не содержат обязательного поля '%s'", cm.getName()));
      if (cm.isPrimaryKey())
        primary.add(cm.getName());
      if (cm.isComputed())
        computed.add(cm.getName());
      if (!cm.isPrimaryKey() && !cm.isComputed() && cm.isNullable() && !data.containsKey(cm.getName()))
        nulls.add(cm.getName());
      columns.add(cm.getName());
    });
    data.forEach((key, value) -> {
      if (!columns.contains(key))
        throw new RuntimeException(String.format("Данные содержат неизвестный столбец '%s'", key));
      if (!computed.contains(key) && !primary.contains(key)) {
        if (setClause.length() > 0) {
          setClause.append(", ");
        }
        setClause.append("\"").append(key).append("\" = :").append(key);
        params.addValue(key, value);
      }
    });
    if (partitional)
      nulls.forEach(name -> {
        if (setClause.length() > 0) {
          setClause.append(", ");
        }
        setClause.append("\"").append(name).append("\" = NULL");
      });
    String whereClause = prepareKeys(tableName, keys, params);

    String sql = String.format(
        "UPDATE \"%s\" SET %s WHERE %s RETURNING %s",
        tableName, setClause, whereClause, getTableMetadata(tableName).getColumns().stream()
            .map(s -> "\"" + s.getName() + "\"").collect(Collectors.joining(","))
    );

    Map<String, Object> res = namedJdbcTemplate.queryForMap(sql, params);
    if (res.get(primary.stream().findFirst().get()) == null)
      throw new ObjectNotFoundException("Запись с указанным ключом не найдена");
    return res;
  }

  private void primaryKeysValidation(String tableName) {
    if (getTableMetadata(tableName).getPrimaryKeys().isEmpty())
      throw new RuntimeException(String.format("Для сущности '%s' не определён первичный ключ"));
  }

  public void deleteRecord(String tableName, String key) {
    primaryKeysValidation(tableName);
    MapSqlParameterSource params = new MapSqlParameterSource();
    String whereClause = prepareKeys(tableName, key, params);

    String sql = String.format(
        "DELETE FROM \"%s\" WHERE %s",
        tableName, whereClause
    );

    if (namedJdbcTemplate.update(sql, params) == 0)
      throw new ObjectNotFoundException("Запись с указанным ключом не найдена");
  }

  private String prepareKeys(String tableName, String stringKeys, MapSqlParameterSource params) {
    String[] keys = stringKeys.split(",");
    // Ищем первичный ключ или поле с именем ID
    List<String> primaryKeys = getTableMetadata(tableName).getPrimaryKeys();
    if (primaryKeys.size() != keys.length)
      throw new RuntimeException(String
          .format("Переданные значения сегментов ключа не соответствуют метаданным PRIMARY KEY %s (%s)",
              tableName, stringKeys));
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < primaryKeys.size(); i++) {
      String key = primaryKeys.get(i);
      //.append("\"").append(tableName).append("\".")
      sb.append("\"").append(key).append("\" = :").append(key);
      params.addValue(key, keys[i]);
    }
    return sb.toString();
  }

  private TableMetadata getTableMetadata(String tableName) {
    return Optional.ofNullable(metadataService.getAllTables().get(tableName))
        .orElseThrow(() -> new ObjectNotFoundException(
            String.format("БД не содержит таблицы %s", tableName.toLowerCase())));
  }
}
