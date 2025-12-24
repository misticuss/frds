package com.frds.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:09
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IndexMetadata {
  private String name;
  private String tableName;
  private List<String> columns = new ArrayList<>();
  private boolean unique;
  private boolean active;
  private String expression; // для функциональных индексов
  private String description;
  private Long statistics;

  public String getType() {
    if (unique) {
      return "UNIQUE";
    } else {
      return "NON-UNIQUE";
    }
  }

  public String getFullDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append(getType()).append(" INDEX ");
    sb.append(name).append(" ON ").append(tableName);
    sb.append("(").append(String.join(", ", columns)).append(")");

    if (expression != null && !expression.isEmpty()) {
      sb.append(" EXPRESSION: ").append(expression);
    }

    if (!active) {
      sb.append(" [INACTIVE]");
    }

    return sb.toString();
  }
}
