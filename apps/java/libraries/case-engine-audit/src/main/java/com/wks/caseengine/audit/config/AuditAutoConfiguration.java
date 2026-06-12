package com.wks.caseengine.audit.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ConditionalOnProperty(name = "wks.audit.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = {
        "com.wks.caseengine.audit"
})
public class AuditAutoConfiguration {
}
