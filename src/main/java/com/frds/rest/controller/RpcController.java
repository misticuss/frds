package com.frds.rest.controller;

import com.frds.rest.exception.ObjectNotFoundException;
import com.frds.rest.model.ProcedureMetadata;
import com.frds.rest.model.TableMetadata;
import com.frds.rest.service.MetadataService;
import com.frds.rest.service.PpcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 15:17
 */

@RestController
@RequestMapping("/rpc")
@Tag(name = "RPC", description = "Удаленный вызов хранимых процедур и функций")
@SecurityRequirement(name = "basicAuth")
public class RpcController {

  private final PpcService procedureService;
  private final MetadataService metadataService;

  public RpcController(PpcService procedureService, MetadataService metadataService) {
    this.procedureService = procedureService;
    this.metadataService = metadataService;
  }

  @GetMapping("/{procedure}")
  @Operation(summary = "Информация о хранимой процедуре",
      description = "Возвращает список всех параметров и исходного кода процедуры")
  public ResponseEntity<ProcedureMetadata> getProcedureInfo(
      @PathVariable String procedure) {
    ProcedureMetadata info = metadataService.getAllProcedures().get(procedure);
    if (info == null)
      throw new ObjectNotFoundException(String.format("Процедура с именем '%s' не найдена", procedure));
    return ResponseEntity.ok(info);
  }

  @PostMapping("/{procedure}")
  @Operation(summary = "Выполнить хранимую процедуру",
      description = "Выполняет указанную хранимую  процедуру с параметрами")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "Процедура успешно выполнена",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = com.frds.rest.model.ApiResponse.class)
          )
      )
  })
  public ResponseEntity<Object> executeProcedure(
      @PathVariable String procedure,
      @RequestBody(required = false) Map<String, Object> parameters) {

    Object res = procedureService.executeProcedure(
        procedure,
        parameters != null ? parameters : new HashMap<>()
    );
    return res != null ? ResponseEntity.ok(res) : ResponseEntity.status(201).body(Map.of(
        "status", 201,
        "message", "Процедура выполнена успешно",
        "entity", procedure
    ));
  }
}
