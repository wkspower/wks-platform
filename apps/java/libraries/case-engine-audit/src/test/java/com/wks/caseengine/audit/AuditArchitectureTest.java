package com.wks.caseengine.audit;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class AuditArchitectureTest {

    @Test
    void verifyArchitecture() {
        ApplicationModules.of("com.wks.caseengine.audit").verify();
    }
}
