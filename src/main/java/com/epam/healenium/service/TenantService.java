package com.epam.healenium.service;

import java.util.Map;

public interface TenantService {

    void addTenant(String tenant);

    void setCurrentTenant(Map<String, String> headers);

    void setCurrentTenant(String key);

    void deleteTenant(String schema);
}
