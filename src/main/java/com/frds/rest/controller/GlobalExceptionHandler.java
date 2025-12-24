package com.frds.rest.controller;

import com.frds.rest.exception.ObjectNotFoundException;
import com.frds.rest.model.ErrorDetails;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Anton Zhulin
 * Date: 22.12.2025
 * Time: 17:11
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(RuntimeException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ApiResponse(
      responseCode = "400",
      description = "Некорректные параметры или данные запроса",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorDetails.class)
      )
  )
  public ErrorDetails handleRuntimeException(RuntimeException e) {
    ErrorDetails err = new ErrorDetails();
    err.setStatus(HttpStatus.BAD_REQUEST.value());
    err.setMessage("Некорректные параметры или данные запроса");
    err.setException(e.getMessage());
    return err;
  }

  @ExceptionHandler(ObjectNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ApiResponse(
      responseCode = "404",
      description = "Ресурс не найден",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorDetails.class)
      )
  )
  public ErrorDetails handleObjectNotFoundException(ObjectNotFoundException e) {
    ErrorDetails err = new ErrorDetails();
    err.setStatus(HttpStatus.NOT_FOUND.value());
    err.setMessage("Ресурс не найден");
    err.setException(e.getMessage());
    return err;
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ApiResponse(
      responseCode = "500",
      description = "Внутренняя ошибка сервера",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ErrorDetails.class)
      )
  )
  public ErrorDetails handleException(Exception e) {
    ErrorDetails err = new ErrorDetails();
    err.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    err.setMessage("Внутренняя ошибка сервера");
    err.setIsFatal(true);
    err.setException(e.getMessage());
    return err;
  }
}
