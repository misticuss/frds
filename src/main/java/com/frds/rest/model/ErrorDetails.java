package com.frds.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:13
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
  private int status;
  private String error;
  private String message;
  private Boolean isFatal;
  private String exception;
  private Object details;

  public ErrorDetails() {

  }
}