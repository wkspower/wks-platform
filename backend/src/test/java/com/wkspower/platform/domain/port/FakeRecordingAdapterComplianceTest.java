package com.wkspower.platform.domain.port;

import com.wkspower.platform.domain.service.WorkflowAdapterBinder;

/**
 * Story 4.1 AC6 — proves {@link FakeRecordingWorkflowAdapter} passes the compliance contract. Exercises
 * tests 5 and 6 non-trivially (signal emission + kind validation) so the harness itself is proven
 * against a non-NullAdapter implementation.
 */
class FakeRecordingWorkflowAdapterComplianceTest_KEEP extends WorkflowAdapterComplianceTest {

  private FakeRecordingWorkflowAdapter fake;

  @Override
  protected WorkflowAdapter newAdapterUnderTest(WorkflowAdapterBinder binder) {
    this.fake = new FakeRecordingWorkflowAdapter(binder);
    return fake;
  }

  @Override
  protected boolean adapterEmitsSignals() {
    return true;
  }

  @Override
  protected void emitSignal(ExecutionSignal signal) {
    fake.emit(signal);
  }
}
