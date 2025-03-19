package com.wks.caseengine.entity;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenantDatabase {
	
    private UUID uid;

    private String name;
    
    private String jdbcUrl;
    
    private String jdbcUserName;
    
    private String jdbcPassword;

    private int poolSize;
    
    private int minIdle;
    
    private long idleTimeout;
    
    private long connectionTimeout;
    
    private long maxLifeTime;
   
}
