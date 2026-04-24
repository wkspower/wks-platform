package com.wkspower.platform.api.error;

import com.wkspower.platform.domain.exception.ErrorDetail;
import com.wkspower.platform.domain.exception.WksConfigException;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only controller — throws a {@link WksConfigException} with three stubbed entries so {@link
 * ConfigProbeWebMvcTest} can assert the 422 multi-error envelope shape end-to-end.
 */
@RestController
@Profile("test")
class ConfigProbeController {

  @GetMapping("/_test/config-probe")
  void probe() {
    throw new WksConfigException(
        List.of(
            new ErrorDetail("WKS-CFG-101", "caseType.yaml missing required field", "name", 3),
            new ErrorDetail("WKS-CFG-102", "status 'foo' not declared", "status", 17),
            ErrorDetail.ofField("WKS-CFG-103", "role 'admin' not found", "roles[0]")));
  }
}
