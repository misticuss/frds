package com.frds.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:07
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColumnMetadata {
  private String name;
  private String type;
  private Integer length;
  private Integer scale;
  private Integer precision;
  private boolean nullable;
  private boolean computed;
  private String defaultValue;
  private String description;
  private String generatorName;
  private String characterSet;
  private String collation;
  private boolean isPrimaryKey;
  private boolean isForeignKey;
  private boolean isUnique;
  private boolean isAutoIncrement;
  private String referencedTable;
  private String referencedColumn;

  public String getFullType() {
    if (type == null) {
      return null;
    }

    StringBuilder fullType = new StringBuilder(type);

    if (length != null && length > 0) {
      if (scale != null && scale > 0) {
        fullType.append("(").append(length).append(",").append(scale).append(")");
      } else {
        fullType.append("(").append(length).append(")");
      }
    }

    if (characterSet != null && !characterSet.isEmpty()) {
      fullType.append(" CHARACTER SET ").append(characterSet);
    }

    if (collation != null && !collation.isEmpty()) {
      fullType.append(" COLLATE ").append(collation);
    }

    if (!nullable) {
      fullType.append(" NOT NULL");
    }

    if (defaultValue != null && !defaultValue.isEmpty()) {
      fullType.append(" DEFAULT ").append(defaultValue);
    }

    return fullType.toString();
  }
}
