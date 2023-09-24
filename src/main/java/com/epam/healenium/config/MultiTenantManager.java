package com.epam.healenium.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "healenium")
@Configuration
public class MultiTenantManager {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.hikari.schema}")
    private String defaultSchema;

    private final ThreadLocal<String> currentTenant = new ThreadLocal<>();
    private final Map<Object, Object> tenantDataSources = new ConcurrentHashMap<>();
    private final DataSourceProperties properties;
    private AbstractRoutingDataSource multiTenantDataSource;

    public MultiTenantManager(DataSourceProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DataSource dataSource() {
        multiTenantDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return currentTenant.get();
            }
        };
        DataSource defaultDataSource = getDefDataSource(properties, properties.getUrl(), defaultSchema);
        tenantDataSources.put(defaultSchema, defaultDataSource);
        multiTenantDataSource.setTargetDataSources(tenantDataSources);
        multiTenantDataSource.setDefaultTargetDataSource(defaultDataSource);
        multiTenantDataSource.afterPropertiesSet();
        return multiTenantDataSource;
    }

    public void addTenant(String schema) throws SQLException {
        String replace = url.replace("?" + defaultSchema, "?" + schema);
        DataSource dataSource = getDefDataSource(properties, replace, schema);

        // Check that new connection is 'live'. If not - throw exception
        try (Connection c = dataSource.getConnection()) {
            tenantDataSources.put(schema, dataSource);
            multiTenantDataSource.afterPropertiesSet();
        }
    }

    public void setCurrentTenant(String tenantId) {
        currentTenant.set(tenantId);
    }

    public DataSource getDefDataSource(DataSourceProperties dataSourceProperties, String url, String schema) {
        HikariDataSource build = DataSourceBuilder.create(dataSourceProperties.getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(dataSourceProperties.getDriverClassName())
                .url(dataSourceProperties.getUrl())
                .url(url)
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .build();
        build.setSchema(schema);
        return build;
    }

    public void deleteTenant(String schema) {
        HikariDataSource ds = (HikariDataSource) tenantDataSources.get(schema);
        ds.close();
        tenantDataSources.remove(schema);
    }

}
