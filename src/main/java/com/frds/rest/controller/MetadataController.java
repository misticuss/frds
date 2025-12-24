package com.frds.rest.controller;

import com.frds.rest.exception.ObjectNotFoundException;
import com.frds.rest.model.TableMetadata;
import com.frds.rest.service.MetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * @author Anton Zhulin
 * Date: 16.12.2025
 * Time: 15:07
 */

@RestController
@RequestMapping("/meta")
@Tag(name = "META", description = "Метаданные БД")
public class MetadataController {
  @Value("${app.api.openapi.title}")
  private String title;

  @Value("${app.api.openapi.description}")
  private String description;

  @Value("${app.api.openapi.version}")
  private String version;


  private final MetadataService metadataService;

  public MetadataController(MetadataService metadataService) {
    this.metadataService = metadataService;
  }

  @GetMapping("/")
  @Operation(summary = "Информация о БД",
      description = "Возвращает список таблиц, представлений и процедур, содержащихся в БД")
  public ResponseEntity<MetaInfo> getApiInfo() {
    MetaInfo info = new MetaInfo();
    info.setName(title);
    info.setVersion(version);
    info.setDescription(description);
    info.setTables(metadataService.getAllTables().keySet());
    info.setProcedures(metadataService.getAllProcedures().keySet());

    return ResponseEntity.ok(info);
  }

  @GetMapping("/{entity}")
  @Operation(summary = "Информация о сущности в БД",
      description = "Возвращает список всех полей, первичных ключей и другой информации")
  public ResponseEntity<TableMetadata> getEntityInfo(@PathVariable String entity) {
    TableMetadata info = metadataService.getAllTables().get(entity);
    if (info == null)
      throw new ObjectNotFoundException("Сущность не найдена");
    return ResponseEntity.ok(info);
  }

  // Вспомогательный класс для информации об API
  @Data
  public static class MetaInfo {
    private String name;
    private String version;
    private String description;
    private Set<String> tables;
    private Set<String> procedures;
  }
}
