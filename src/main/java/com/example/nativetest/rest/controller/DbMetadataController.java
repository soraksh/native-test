package com.example.nativetest.rest.controller;

import com.example.nativetest.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DbMetadataController {

    @Autowired
    protected MetadataService metadataService;

    @GetMapping("/dbmetadata/schemas/{name}")
    protected List<Map<String, String>> getSchemas(@PathVariable String name) {
        return metadataService.getSchemas(name);
    }

    @GetMapping("/dbmetadata/tables/{name}")
    protected List<Map<String, String>> getTables(@PathVariable String name) {
        return metadataService.getTables(name);
    }

    @GetMapping("/dbmetadata/columns/{name}/{tableName}")
    protected List<Map<String, String>> getTableColumns(@PathVariable String name,
                                                        @PathVariable String tableName) {
        return metadataService.getColumns(name, tableName);
    }

    @GetMapping("/dbmetadata/preview/{name}/{tableName}")
    protected List<Map<String, Object>> getDataPreview(@PathVariable String name,
                                                       @PathVariable String tableName) {
        return metadataService.getDataPreview(name, tableName);
    }

    @GetMapping("/dbmetadata/columnstats/{name}/{tableName}")
    protected List<Map<String, Object>> getColumnStats(@PathVariable String name,
                                                            @PathVariable String tableName) {
        return metadataService.getColumnStats(name, tableName);
    }

    @GetMapping("/dbmetadata/tablestats/{name}")
    protected List<Map<String, Object>> getTableStats(@PathVariable String name) {
        return metadataService.getTableStats(name);
    }

}
