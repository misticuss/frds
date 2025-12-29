package com.frds.db.service;

import com.frds.rest.exception.ObjectNotFoundException;
import com.frds.db.model.ProcedureMetadata;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 15:59
 */

@Service
public class PpcService {

  private final NamedParameterJdbcTemplate namedJdbcTemplate;
  private final MetadataService metadataService;

  public PpcService(JdbcTemplate jdbcTemplate, MetadataService metadataService) {
    this.namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    this.metadataService = metadataService;
  }

  /**
   * Выполнение хранимой процедуры
   */
  public Object executeProcedure(String procedureName, Map<String, Object> parameters) {
    ProcedureMetadata meta = getProcedureMetadata(procedureName);
    Set<String> inputParameters = new HashSet<>();
    meta.getParameters().forEach(pm -> {
      if ("INPUT".equals(pm.getType())) {
        if (!pm.isNullable() && !parameters.containsKey(pm.getName()))
          throw new RuntimeException(String.format("Данные не содержат обязательного параметра '%s'", pm.getName()));
        inputParameters.add(pm.getName());
      }
    });
    StringBuilder sql = new StringBuilder();
    if (meta.isSelectable())
      sql.append("SELECT * FROM \"").append(procedureName).append("\"");
    else
      sql.append("EXECUTE PROCEDURE \"").append(procedureName).append("\"");
    MapSqlParameterSource paramSource = new MapSqlParameterSource();
    if (meta.getInputParams() > 0) {
      sql.append("(");
      List<String> placeholders = new ArrayList<>();
      for (Map.Entry<String, Object> entry : parameters.entrySet()) {
        if (!inputParameters.contains(entry.getKey()))
          throw new RuntimeException(String.format("Данные содержат неизвестный параметр '%s'", entry.getKey()));
        placeholders.add(":" + entry.getKey());
        paramSource.addValue(entry.getKey(), entry.getValue());
      }

      sql.append(String.join(", ", placeholders)).append(")");
    }
    if (meta.isSelectable()) {
      return namedJdbcTemplate.queryForList(sql.toString(), paramSource);
    } else {
      namedJdbcTemplate.update(sql.toString(), paramSource);
      return null;
    }
  }

  /**
   * Выполнение скалярной функции
   */
  public Object executeFunction(String functionName, Map<String, Object> parameters) {
    StringBuilder sql = new StringBuilder("SELECT \"");
    sql.append(functionName).append("\"(");

    if (!parameters.isEmpty()) {
      List<String> paramPlaceholders = new ArrayList<>();
      MapSqlParameterSource paramSource = new MapSqlParameterSource();

      int index = 1;
      for (Map.Entry<String, Object> entry : parameters.entrySet()) {
        String paramName = "p" + index++;
        paramPlaceholders.add(":" + paramName);
        paramSource.addValue(paramName, entry.getValue());
      }

      sql.append(String.join(", ", paramPlaceholders));
    }

    sql.append(") FROM RDB$DATABASE");

    try {
      return parameters.isEmpty() ?
          namedJdbcTemplate.queryForObject(sql.toString(), new MapSqlParameterSource(), Object.class) :
          namedJdbcTemplate.queryForObject(sql.toString(), parameters, Object.class);
    } catch (Exception e) {
      throw new RuntimeException("Ошибка выполнения функции " + functionName + ": " + e.getMessage(), e);
    }
  }

  /**
   * Получение SQL типа из строкового описания
   */
  private int getSqlType(String dataType) {
    if (dataType == null) {
      return Types.OTHER;
    }

    String typeUpper = dataType.toUpperCase();

    if (typeUpper.contains("CHAR") || typeUpper.contains("TEXT") || typeUpper.contains("VARCHAR")) {
      return Types.VARCHAR;
    } else if (typeUpper.contains("INT")) {
      return Types.INTEGER;
    } else if (typeUpper.contains("DECIMAL") || typeUpper.contains("NUMERIC")) {
      return Types.DECIMAL;
    } else if (typeUpper.contains("DOUBLE") || typeUpper.contains("FLOAT")) {
      return Types.DOUBLE;
    } else if (typeUpper.contains("DATE")) {
      return Types.DATE;
    } else if (typeUpper.contains("TIME")) {
      return Types.TIME;
    } else if (typeUpper.contains("TIMESTAMP")) {
      return Types.TIMESTAMP;
    } else if (typeUpper.contains("BLOB")) {
      return Types.BLOB;
    } else if (typeUpper.contains("BOOLEAN")) {
      return Types.BOOLEAN;
    } else {
      return Types.OTHER;
    }
  }

  /**
   * Извлечение функций из исходного кода процедур
   * (Firebird не имеет отдельной системной таблицы для функций)
   */
  private List<String> extractFunctionsFromSource() {
    List<String> functions = new ArrayList<>();
    Pattern functionPattern = Pattern.compile(
        "DECLARE\\s+(?:EXTERNAL\\s+)?FUNCTION\\s+\"([^\"]+)\"",
        Pattern.CASE_INSENSITIVE
    );

    metadataService.getAllProcedures().values().forEach(procedure -> {
      if (procedure.getSourceCode() != null) {
        Matcher matcher = functionPattern.matcher(procedure.getSourceCode());
        while (matcher.find()) {
          functions.add(matcher.group(1));
        }
      }
    });

    return functions.stream()
        .distinct()
        .sorted()
        .toList();
  }

  private ProcedureMetadata getProcedureMetadata(String procedureName) {
    return Optional.ofNullable(metadataService.getAllProcedures().get(procedureName))
        .orElseThrow(() -> new ObjectNotFoundException(
            String.format("БД не содержит процедуры %s", procedureName.toLowerCase())));
  }
}
