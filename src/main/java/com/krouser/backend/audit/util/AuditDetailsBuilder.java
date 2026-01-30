package com.krouser.backend.audit.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AuditDetailsBuilder {

    private final Map<String, String> detailsMap = new LinkedHashMap<>();

    public AuditDetailsBuilder add(String key, Object value) {
        if (key != null && value != null) {
            detailsMap.put(key, String.valueOf(value));
        }
        return this;
    }

    public String build() {
        if (detailsMap.isEmpty()) {
            return null;
        }
        String combined = detailsMap.entrySet().stream()
                .map(e -> escape(e.getKey()) + "=" + escape(e.getValue()))
                .collect(Collectors.joining(";"));
        return truncate(combined, 2000);
    }

    private String escape(String input) {
        if (input == null)
            return "";
        return input.replace(";", "{semi}").replace("=", "{eq}");
    }

    private String truncate(String input, int maxLength) {
        if (input == null)
            return null;
        if (input.length() <= maxLength)
            return input;
        return input.substring(0, maxLength);
    }
}
