package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.service.BackendAdapterBinder;

/**
 * Story 4.1 AC6 — proves {@link FakeRecordingAdapter} passes the compliance contract. Exercises
 * tests 5 and 6 non-trivially (signal emission + kind validation) so the harness itself is proven
 * against a non-NullAdapter implementation.
 */
class FakeRecordingAdapterComplianceTest extends BackendAdapterComplianceTest {

  private FakeRecordingAdapter fake;

  @Override
  protected BackendAdapter newAdapterUnderTest(BackendAdapterBinder binder) {
    this.fake = new FakeRecordingAdapter(binder);
    return fake;
  }

  @Override
  protected boolean adapterEmitsSignals() {
    return true;
  }

  @Override
  protected void emitSignal(BackendSignal signal) {
    fake.emit(signal);
  }
}
