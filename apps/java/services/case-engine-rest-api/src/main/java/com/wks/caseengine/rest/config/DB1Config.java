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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
	    basePackages = {
	        "com.wks.caseengine.rest.db1.repository", // Repository package for DB1
	        "com.wks.caseengine.product.repository",
	        "com.wks.caseengine.repository"// Additional repository package
	    },
	    entityManagerFactoryRef = "db1EntityManagerFactory",
	    transactionManagerRef = "db1TransactionManager"
	)

public class DB1Config {

    @Value("${spring.datasource.db1.url}")
    private String url;

    @Value("${spring.datasource.db1.username}")
    private String userName;

    @Value("${spring.datasource.db1.password}")
    private String password;

    @Autowired
    private Environment env;
  
    @Value("${SQL_SERVER_1_USERNAME}")
    private String envvv;

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.db1")
    public DataSourceProperties db1DataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.db1")
    public DataSource db1DataSource() {


    	
        return db1DataSourceProperties().initializeDataSourceBuilder()
        		.url(url)
        		.username(userName)
        		.password(password)
        		.driverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        		.build();
    }

    @Primary
    @Bean(name = "db1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db1EntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(db1DataSource())
                .packages(
                    "com.wks.caseengine.rest.db1.entity", 
                    "com.wks.caseengine.entity"
                ) // Include both packages
                .persistenceUnit("db1")
                .properties(hibernateProperties())
                .build();
    }

    
    private Map<String, Object> hibernateProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "none");  // Set to "update", "create", etc. for DB2
        properties.put("hibernate.dialect", "org.hibernate.dialect.SQLServerDialect");  // Set SQL Server dialect
        return properties;
    }

    @Primary
    @Bean
    public PlatformTransactionManager db1TransactionManager(
        @Qualifier("db1EntityManagerFactory") EntityManagerFactory db1EntityManagerFactory) {
        return new JpaTransactionManager(db1EntityManagerFactory);
    }
    
    @Primary
    @Bean(name = "db1JdbcTemplate")
    public JdbcTemplate db1JdbcTemplate(@Qualifier("db1DataSource") DataSource db1DataSource) {
        return new JdbcTemplate(db1DataSource);
    }

}
