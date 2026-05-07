package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.service.WorkflowAdapterBinder;

/**
 * Story 4.1 AC6 — proves {@link com.wkspower.platform.domain.service.NullAdapter} passes the
 * compliance contract. Tests 5 / 6 are vacuously satisfied because NullAdapter emits no signals.
 */
class NullAdapterComplianceTest extends WorkflowAdapterComplianceTest {

  @Override
  protected WorkflowAdapter newAdapterUnderTest(WorkflowAdapterBinder binder) {
    return nullAdapter;
  }
}
