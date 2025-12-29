package com.frds.db.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:08
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForeignKeyMetadata {
  private String name;
  private String constraintName;
  private List<String> columns = new ArrayList<>();
  private String referencedTable;
  private List<String> referencedColumns = new ArrayList<>();
  private String updateRule; // CASCADE, RESTRICT, SET NULL, NO ACTION
  private String deleteRule; // CASCADE, RESTRICT, SET NULL, NO ACTION
  private boolean isDeferrable;
  private boolean initiallyDeferred;

  public String getDescription() {
    return String.format("FOREIGN KEY (%s) REFERENCES %s(%s) " +
                         "ON UPDATE %s ON DELETE %s",
        String.join(", ", columns),
        referencedTable,
        String.join(", ", referencedColumns),
        updateRule,
        deleteRule);
  }
}
