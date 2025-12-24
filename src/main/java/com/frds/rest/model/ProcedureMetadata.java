package com.frds.rest.model;

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
  private String returnType;
  private boolean isFunction;

  public String getSignature() {
    StringBuilder signature = new StringBuilder(name);
    signature.append("(");

    List<String> paramStrings = new ArrayList<>();
    for (ParameterMetadata param : parameters) {
      paramStrings.add(param.toString());
    }

    signature.append(String.join(", ", paramStrings));
    signature.append(")");

    if (returnType != null && !returnType.isEmpty()) {
      signature.append(" RETURNS ").append(returnType);
    }

    return signature.toString();
  }

  public boolean isSelectable() {
    return "SELECTABLE".equals(getType()) && getOutputParams() > 0;
  }
}
