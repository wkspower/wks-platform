package com.wkspower.platform.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.wkspower.platform.domain.port.AttachmentScope;
import com.wkspower.platform.domain.port.ExecutionSignalSubscription;
import com.wkspower.platform.domain.port.CaseInstanceRef;
import com.wkspower.platform.domain.port.CaseTypeRef;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Story 4.1 Task 4 — direct unit coverage of {@link NullAdapter} contract per AC3. */
class NullAdapterTest {

  private final NullAdapter adapter = new NullAdapter();

  @Test
  void attach_isNoOp() {
    assertThatCode(() -> adapter.attach(new CaseTypeRef("ct-1", "1.0.0"), AttachmentScope.ofCase()))
        .doesNotThrowAnyException();
  }

  @Test
  void detach_isNoOp() {
    assertThatCode(() -> adapter.detach(new CaseTypeRef("ct-1", "1.0.0")))
        .doesNotThrowAnyException();
  }

  @Test
  void start_returnsDeterministicSyntheticId() {
    UUID caseId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    CaseInstanceRef ref = new CaseInstanceRef(caseId, new CaseTypeRef("ct-1", "1.0.0"));

    String id = adapter.start(ref);

    assertThat(id).isEqualTo("null:11111111-1111-1111-1111-111111111111");
  }

  @Test
  void start_isStableAcrossInvocations() {
    CaseInstanceRef ref = new CaseInstanceRef(UUID.randomUUID(), new CaseTypeRef("ct-1", "1.0.0"));
    assertThat(adapter.start(ref)).isEqualTo(adapter.start(ref));
  }

  @Test
  void cancel_isNoOp() {
    CaseInstanceRef ref = new CaseInstanceRef(UUID.randomUUID(), new CaseTypeRef("ct-1", "1.0.0"));
    assertThatCode(() -> adapter.cancel(ref)).doesNotThrowAnyException();
  }

  @Test
  void onExecutionSignal_neverInvokesHandler() {
    AtomicInteger invocations = new AtomicInteger(0);

    ExecutionSignalSubscription sub =
        adapter.onExecutionSignal(signal -> invocations.incrementAndGet());

    // No way to push a signal through NullAdapter — verify the handler was registered without
    // invocation, then close. The adapter emits no signals, ever.
    assertThat(invocations.get()).isZero();
    assertThatCode(sub::close).doesNotThrowAnyException();
    assertThat(invocations.get()).isZero();
  }

  @Test
  void closingSubscription_isIdempotent() {
    ExecutionSignalSubscription sub = adapter.onExecutionSignal(s -> {});
    sub.close();
    assertThatCode(sub::close).doesNotThrowAnyException();
  }
}
