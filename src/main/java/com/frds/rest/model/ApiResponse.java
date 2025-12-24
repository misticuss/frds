package com.frds.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 16:11
 */

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
  private int status;
  private String message;
  private String entity;
}
