package com.frds.db.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.Date;
import java.util.Map;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:10
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabaseInfo {
  private String productName;
  private String productVersion;
  private String driverName;
  private String driverVersion;
  private String url;
  private String username;
  private Integer defaultTransactionIsolation;
  private String databaseName;
  private String catalog;
  private String schema;
  private Integer maxConnections;
  private Integer maxColumnsInTable;
  private Integer maxTableNameLength;
  private Integer maxColumnNameLength;
  private Date connectionTime;
  private Map<String, Object> serverInfo;

  public String getFullInfo() {
    return String.format("%s %s (Driver: %s %s)",
        productName, productVersion, driverName, driverVersion);
  }
}
