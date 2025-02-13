/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.rest.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.wks.caseengine.rest.db2.repository", // Repository package for DB2
    entityManagerFactoryRef = "db2EntityManagerFactory",
    transactionManagerRef = "db2TransactionManager"
)
public class DB2Config {
	
	
	  @Value("${spring.datasource.db2.url}")
	   private String url;

	  @Value("${spring.datasource.db2.username}")
	   private String userName;

	  @Value("${spring.datasource.db2.password}")
	   private String password;

	  @Bean
    @ConfigurationProperties("spring.datasource.db2")
    public DataSourceProperties db2DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.db2")
    public DataSource db2DataSource() {
        return db2DataSourceProperties().initializeDataSourceBuilder()
//        		.url("jdbc:sqlserver://216.48.180.83;databaseName=AOP;trustServerCertificate=true")
//        		.username("sa")
//        		.password("#Qwer123")
        		.url(url)
        		.username(userName)
        		.password(password)
        		.driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        		.build();
    }

    @Bean(name = "db2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db2EntityManagerFactory(
        EntityManagerFactoryBuilder builder) {
        return builder
            .dataSource(db2DataSource())
            .packages("com.wks.caseengine.rest.db2.entity") // Change to your model package
            .persistenceUnit("db2")
            .properties(hibernatePropertiesForDb2())
            .build();
    }
    
    private Map<String, Object> hibernatePropertiesForDb2() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "update");  // Set to "update", "create", etc. for DB2
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");  // Set SQL Server dialect
        return properties;
    }

    @Bean
    public PlatformTransactionManager db2TransactionManager(
        @Qualifier("db2EntityManagerFactory") EntityManagerFactory db2EntityManagerFactory) {
        return new JpaTransactionManager(db2EntityManagerFactory);
    }
}