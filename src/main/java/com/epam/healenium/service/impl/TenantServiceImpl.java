package com.epam.healenium.service.impl;

import com.epam.healenium.config.MultiTenantManager;
import com.epam.healenium.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Slf4j(topic = "healenium")
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final MultiTenantManager multiTenantManager;
    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void addAllTenants() {
        String sql = "SELECT schema_name FROM tenant";
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : resultList) {
            String schema = (String) row.get("schema_name");
            try {
                multiTenantManager.addTenant(schema);
            } catch (SQLException e) {
                log.error("[Add All Tenants] Exception: ", e);
            }
        }
    }

    @Override
    public void addTenant(String tenant) {
        try {
            multiTenantManager.addTenant(tenant);
        } catch (SQLException e) {
            log.error("[Add Tenant] Exception: ", e);
        }
    }

    @Override
    public void setCurrentTenant(Map<String, String> headers) {
        multiTenantManager.setCurrentTenant(headers.get("schema"));
    }

    @Override
    public void setCurrentTenant(String key) {
        multiTenantManager.setCurrentTenant(key);
    }

    @Override
    public void deleteTenant(String schema) {
        multiTenantManager.deleteTenant(schema);
    }


}
