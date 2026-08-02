package com.villamanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class TableExportRequest {
    private String fileName;
    private String title;
    private String sheetName;
    private List<String> headers;
    private List<List<Object>> rows;
}
