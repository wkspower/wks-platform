package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseTypeRef;
import com.wkspower.platform.domain.port.ExecutionSignalHandler;
import com.wkspower.platform.domain.port.ExecutionSignalSubscription;
import com.wkspower.platform.domain.port.WorkflowAdapter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Story 4.1 Task 5 — direct unit coverage of {@link WorkflowAdapterBinder} per AC4. */
class WorkflowAdapterBinderTest {

  private final NullAdapter nullAdapter = new NullAdapter();
  private final WorkflowAdapterBinder binder = new WorkflowAdapterBinder(nullAdapter);

  @Test
  void resolve_returnsNullAdapter_whenNothingRegistered() {
    assertThat(binder.resolve(new CaseTypeRef("ct-1", "1.0.0"))).isSameAs(nullAdapter);
  }

  @Test
  void resolve_returnsRegisteredAdapter() {
    CaseTypeRef ref = new CaseTypeRef("ct-1", "1.0.0");
    WorkflowAdapter custom = new RecordingAdapter();

    binder.register(ref, custom);

    assertThat(binder.resolve(ref)).isSameAs(custom);
  }

  @Test
  void resolve_returnsMostRecentlyRegisteredAdapter() {
    CaseTypeRef ref = new CaseTypeRef("ct-1", "1.0.0");
    WorkflowAdapter first = new RecordingAdapter();
    WorkflowAdapter second = new RecordingAdapter();

    binder.register(ref, first);
    binder.register(ref, second);

    assertThat(binder.resolve(ref)).isSameAs(second);
  }

  @Test
  void unregister_fallsBackToNullAdapter() {
    CaseTypeRef ref = new CaseTypeRef("ct-1", "1.0.0");
    binder.register(ref, new RecordingAdapter());

    binder.unregister(ref);

    assertThat(binder.resolve(ref)).isSameAs(nullAdapter);
  }

  @Test
  void unregister_unknownKey_isNoOp() {
    binder.unregister(new CaseTypeRef("never-registered", "1.0.0"));
    assertThat(binder.resolve(new CaseTypeRef("never-registered", "1.0.0"))).isSameAs(nullAdapter);
  }

  @Test
  void concurrentRegisterFromTwoThreads_lastWriteWinsAndDoesNotThrow() throws Exception {
    CaseTypeRef ref = new CaseTypeRef("ct-1", "1.0.0");
    WorkflowAdapter a = new RecordingAdapter();
    WorkflowAdapter b = new RecordingAdapter();
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService es = Executors.newFixedThreadPool(2);

    try {
      es.submit(
          () -> {
            start.await();
            for (int i = 0; i < 1000; i++) {
              binder.register(ref, a);
            }
            return null;
          });
      es.submit(
          () -> {
            start.await();
            for (int i = 0; i < 1000; i++) {
              binder.register(ref, b);
            }
            return null;
          });
      start.countDown();
      es.shutdown();
      assertThat(es.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
    } finally {
      es.shutdownNow();
    }

    WorkflowAdapter resolved = binder.resolve(ref);
    assertThat(resolved).isIn(a, b);
  }

  /** Bare-bones adapter for binder tests — no signal/recording behaviour needed here. */
  private static final class RecordingAdapter implements WorkflowAdapter {
    @Override
    public void attach(CaseTypeRef caseType, AttachmentScope scope) {}

    @Override
    public void detach(CaseTypeRef caseType) {}

    @Override
    public ExecutionSignalSubscription onExecutionSignal(ExecutionSignalHandler handler) {
      return () -> {};
    }

    @Override
    public String start(CaseInstanceRef caseInstance) {
      return "rec:" + caseInstance.id();
    }

    @Override
    public void cancel(CaseInstanceRef caseInstance) {}
  }
}
