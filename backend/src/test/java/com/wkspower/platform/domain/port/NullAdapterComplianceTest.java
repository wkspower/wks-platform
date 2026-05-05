package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.service.BackendAdapterBinder;

/**
 * Story 4.1 AC6 — proves {@link com.wkspower.platform.domain.service.NullAdapter} passes the
 * compliance contract. Tests 5 / 6 are vacuously satisfied because NullAdapter emits no signals.
 */
class NullAdapterComplianceTest extends BackendAdapterComplianceTest {

  @Override
  protected BackendAdapter newAdapterUnderTest(BackendAdapterBinder binder) {
    return nullAdapter;
  }
}
