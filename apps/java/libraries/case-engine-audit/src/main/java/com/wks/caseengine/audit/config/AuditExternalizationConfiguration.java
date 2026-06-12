package com.wks.caseengine.audit.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;
import com.wks.caseengine.command.CommandExecutedEvent;

@Configuration
@ConditionalOnProperty(name = "wks.audit.externalize.enabled", havingValue = "true")
public class AuditExternalizationConfiguration {

    @Bean
    EventExternalizationConfiguration eventExternalization() {
        return EventExternalizationConfiguration.externalizing()
            .select(event -> event instanceof CommandExecutedEvent)
            .route(CommandExecutedEvent.class, event -> RoutingTarget.forTarget("wks-audit-topic").withoutKey())
            .build();
    }
}
