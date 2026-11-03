package com.example.DynamicCode.deploy.dto;

import java.util.List;

public record ServerInventoryResponse(
        Long id,
        String name,
        String ip,
        String user,
        boolean connected,
        String error,
        List<InventorySection> sections) {
}
