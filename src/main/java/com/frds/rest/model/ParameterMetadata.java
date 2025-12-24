package com.frds.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:08
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParameterMetadata {
  private String name;
  private String type; // INPUT или OUTPUT
  private String dataType;
  private Integer position;
  private String description;
  private Object defaultValue;
  private boolean nullable;
  private Integer length;
  private Integer scale;
  private Integer precision;

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (type != null && type.equals("OUTPUT")) {
      sb.append("OUT ");
    }

    sb.append(name);

    if (dataType != null) {
      sb.append(" ").append(dataType);

      if (length != null && length > 0) {
        if (scale != null && scale > 0) {
          sb.append("(").append(length).append(",").append(scale).append(")");
        } else {
          sb.append("(").append(length).append(")");
        }
      }
    }

    if (!nullable) {
      sb.append(" NOT NULL");
    }

    if (defaultValue != null) {
      sb.append(" DEFAULT ").append(defaultValue);
    }

    return sb.toString();
  }
}
