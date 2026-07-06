package com.villamanager.util;

import java.util.List;
import java.util.stream.Collectors;

public final class CsvExportUtil {
    private CsvExportUtil() {
    }

    public static String buildCsv(List<String> headers, List<? extends List<?>> rows) {
        StringBuilder csv = new StringBuilder("\uFEFF");
        csv.append(headers.stream().map(CsvExportUtil::cleanCell).collect(Collectors.joining(",")));
        for (List<?> row : rows) {
            csv.append("\n");
            csv.append(row.stream().map(CsvExportUtil::cleanCell).collect(Collectors.joining(",")));
        }
        return csv.toString();
    }

    private static String cleanCell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
