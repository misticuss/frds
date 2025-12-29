package com.frds.db.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:07
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcedureMetadata {
  private String name;
  private String description;
  private String type;
  private int inputParams;
  private int outputParams;
  private String sourceCode;
  private List<ParameterMetadata> parameters = new ArrayList<>();

  public boolean isSelectable() {
    return "SELECTABLE".equals(getType()) && getOutputParams() > 0;
  }
}
