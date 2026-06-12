package com.wks.caseengine.audit.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.wks.caseengine.audit"
})
public class AuditAutoConfiguration {
}
