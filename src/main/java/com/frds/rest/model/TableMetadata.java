package com.frds.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:06
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableMetadata {
  private String name;
  private String description;
  private String viewSource; // null для таблиц, не null для представлений
  private List<ColumnMetadata> columns = new ArrayList<>();
  private List<String> primaryKeys = new ArrayList<>();
  private List<ForeignKeyMetadata> foreignKeys;
  private List<IndexMetadata> indices;
  private Long estimatedRowCount;

  public boolean isView() {
    return viewSource != null;
  }
}
