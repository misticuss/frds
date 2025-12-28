package com.frds.rest.controller;

import com.frds.rest.model.ErrorDetails;
import com.frds.rest.service.TableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 15:15
 */

@RestController
@RequestMapping("/api/{entity}")
@Tag(name = "ENTITY", description = "CRUD операции с сущностями базы данных")
@SecurityRequirement(name = "basicAuth")
public class TableController {

  private final TableService tableService;

  public TableController(TableService tableService) {
    this.tableService = tableService;
  }

  @GetMapping
  @Operation(summary = "Получить все записи",
      description = "Возвращает список записей из указанной таблицы с поддержкой пагинации")
  public ResponseEntity<List<Map<String, Object>>> getTableData(
      @Parameter(description = "Имя таблицы", required = true, example = "EMPLOYEE")
      @PathVariable String entity,

      @Parameter(description = "Количество записей", example = "100")
      @RequestParam(required = false) Integer limit,

      @Parameter(description = "Смещение", example = "0")
      @RequestParam(required = false) Integer offset,

      @Parameter(description = "Поля для выборки (через запятую)", example = "EMP_NO,FIRST_NAME,LAST_NAME")
      @RequestParam(required = false) String fields,

      @Parameter(description = "Условие фильтрации")
      @RequestParam(required = false) String filter,

      @Parameter(description = "Сортировка", example = "EMP_NO DESC")
      @RequestParam(required = false) String sort) {

    HttpHeaders headers = new HttpHeaders();
    List<Map<String, Object>> data = tableService.getTableData(
        entity, limit, offset, fields, filter, sort);
    Long count = tableService.getTableCount(entity, filter);
    long r1 = offset == null ? 0 : offset;
    long l = limit == null ? 0 : limit;
    long r2 = r1 + l;
    if (r2 > count || limit == null)
      r2 = count;
    headers.add("Content-Range", "items " + r1 + "-" + r2 + "/" + count);
    return ResponseEntity.ok().headers(headers).body(data);
  }

  @GetMapping("/{key}")
  @Operation(summary = "Получить запись по ключу",
      description = "Возвращает одну запись по ключу")
  public ResponseEntity<Map<String, Object>> getRecordById(
      @Parameter(description = "Имя таблицы") @PathVariable String entity,
      @Parameter(description = "Идентификатор записи") @PathVariable String key) {
    return ResponseEntity.ok(tableService.getRecordById(entity, key));
  }

  @PostMapping
  @Operation(summary = "Создать новую запись",
      description = "Создает новую запись в указанной таблице")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Запись успешно создана",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = com.frds.rest.model.ApiResponse.class)
          )
      )
  })
  public ResponseEntity<Map<String, Object>> createRecord(
      @Parameter(description = "Имя таблицы") @PathVariable String entity,
      @RequestBody Map<String, Object> data) {
    return ResponseEntity.status(201).body(tableService.createRecord(entity, data));
  }

  @PutMapping("/{key}")
  @Operation(summary = "Обновить запись",
      description = "Полностью обновляет существующую запись")
  public ResponseEntity<Map<String, Object>> updateRecord(
      @Parameter(description = "Имя таблицы") @PathVariable String entity,
      @Parameter(description = "Идентификатор записи") @PathVariable String key,
      @RequestBody Map<String, Object> data) {

    Map<String, Object> updated = tableService.updateRecord(entity, key, data, true);
    return ResponseEntity.ok(updated);
  }

  @PatchMapping("/{key}")
  @Operation(summary = "Частично обновить запись",
      description = "Частично обновляет существующую запись")
  public ResponseEntity<Map<String, Object>> patchRecord(
      @Parameter(description = "Имя таблицы") @PathVariable String entity,
      @Parameter(description = "Идентификатор записи") @PathVariable String key,
      @RequestBody Map<String, Object> data) {

    Map<String, Object> patched = tableService.updateRecord(entity, key, data, false);
    return ResponseEntity.ok(patched);
  }

  @DeleteMapping("/{key}")
  @Operation(summary = "Удалить запись",
      description = "Удаляет запись из таблицы")
  @ApiResponses({
      @ApiResponse(
          responseCode = "204",
          description = "Запись успешно удалена",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = com.frds.rest.model.ApiResponse.class)
          )
      )
  })
  public ResponseEntity<Void> deleteRecord(
      @Parameter(description = "Имя таблицы") @PathVariable String entity,
      @Parameter(description = "Идентификатор записи") @PathVariable String key) {

    tableService.deleteRecord(entity, key);
    return ResponseEntity.noContent().build();
  }
}
