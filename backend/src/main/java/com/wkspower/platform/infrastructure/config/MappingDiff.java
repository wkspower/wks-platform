package com.wkspower.platform.infrastructure.config;

import com.wkspower.platform.domain.config.model.AttachmentDefinition;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.PropertyEmissionRule;
import com.wkspower.platform.domain.config.model.AttachmentDefinition.UserTaskMapping;
import com.wkspower.platform.domain.config.model.MappingChangeClass;
import com.wkspower.platform.domain.config.model.MappingDefinition;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Story 4.2 AC8 — pure-function blast-radius classifier for the mapping layer (D20). Given the
 * previous and next {@link MappingDefinition} of a CaseType, returns either {@link
 * MappingChangeClass#APPEND_CLASS} (only additive changes — safe to redeploy without a CaseType
 * version bump) or {@link MappingChangeClass#MUTATE_CLASS} (something existing was modified or
 * removed — version bump required per D20).
 *
 * <p><b>NOT WIRED INTO DEPLOY GATING IN THIS STORY.</b> Story 3.8's blast-radius validator imports
 * this helper and emits {@link com.wkspower.platform.domain.exception.ErrorCode#WKS_CFG_029} when
 * {@link #classify} returns {@code MUTATE_CLASS} and the deployer did not supply {@code --bump}.
 * 4.2 ships unit tests only; 3.8 wires the call site.
 *
 * <h2>Classification rules</h2>
 *
 * <ul>
 *   <li>{@code APPEND_CLASS} — a new {@code attachments[]} entry was added; or an existing
 *       attachment gained a new {@code map.userTasks.<id>}; or an existing attachment gained a new
 *       {@code map.properties[]} rule; or a new signal mapping was added under existing events
 *       block.
 *   <li>{@code MUTATE_CLASS} — any existing {@code AttachmentDefinition.scope} / {@code .file} /
 *       {@code .type} changed; an attachment was removed; an existing {@code wksTask} value
 *       changed; an existing {@code stageTransition} payload (endEvent or signal) changed; an
 *       existing property emission rule changed; any {@code map.userTasks} / {@code
 *       map.events.signal} key was removed.
 * </ul>
 *
 * <p>Attachment identity is anchored by {@code (type, scope)} — pre/post pairs that share the same
 * {@code (type, scope)} are considered the same attachment for diff purposes (changing {@code file}
 * on the same {@code (type, scope)} is treated as a mutation of the existing attachment, not a
 * delete-plus-add).
 */
public final class MappingDiff {

  private MappingDiff() {}

  /**
   * Classify the diff between {@code prev} and {@code next}. Both arguments may be {@link
   * MappingDefinition#empty()} — empty-to-empty is {@code APPEND_CLASS} (no change is the
   * non-blocking case).
   */
  public static MappingChangeClass classify(MappingDefinition prev, MappingDefinition next) {
    Objects.requireNonNull(prev, "prev");
    Objects.requireNonNull(next, "next");

    Map<String, AttachmentDefinition> prevByKey = indexByKey(prev);
    Map<String, AttachmentDefinition> nextByKey = indexByKey(next);

    // Removal of any attachment is mutate-class.
    for (String key : prevByKey.keySet()) {
      if (!nextByKey.containsKey(key)) {
        return MappingChangeClass.MUTATE_CLASS;
      }
    }

    // For every shared (type, scope) attachment, compare the contents.
    for (var entry : prevByKey.entrySet()) {
      AttachmentDefinition before = entry.getValue();
      AttachmentDefinition after = nextByKey.get(entry.getKey());
      if (after == null) {
        return MappingChangeClass.MUTATE_CLASS;
      }
      // file change is mutate-class (same scope, but the file ref moved)
      if (!before.file().equals(after.file())) {
        return MappingChangeClass.MUTATE_CLASS;
      }

      // userTasks: removed key OR changed value is mutate; new key is append
      for (var u : before.userTaskMappings().entrySet()) {
        UserTaskMapping postValue = after.userTaskMappings().get(u.getKey());
        if (postValue == null) {
          return MappingChangeClass.MUTATE_CLASS;
        }
        if (!Objects.equals(postValue, u.getValue())) {
          return MappingChangeClass.MUTATE_CLASS;
        }
      }

      // signals: removed key OR changed stageTransition is mutate
      for (var s : before.signalMappings().entrySet()) {
        var postValue = after.signalMappings().get(s.getKey());
        if (postValue == null) {
          return MappingChangeClass.MUTATE_CLASS;
        }
        if (!Objects.equals(postValue, s.getValue())) {
          return MappingChangeClass.MUTATE_CLASS;
        }
      }

      // endEvent: presence/value change is mutate (removing or changing the rule)
      if (before.endEventMapping().isPresent()) {
        if (!after.endEventMapping().equals(before.endEventMapping())) {
          return MappingChangeClass.MUTATE_CLASS;
        }
      }

      // properties: every prev rule must be present byte-equal in next; new rules at the tail are
      // append. Use a set of equals-based contains rather than positional indexing.
      Set<PropertyEmissionRule> nextRules = new HashSet<>(after.propertyEmissionRules());
      for (PropertyEmissionRule r : before.propertyEmissionRules()) {
        if (!nextRules.contains(r)) {
          return MappingChangeClass.MUTATE_CLASS;
        }
      }
    }

    // Anything left in next but not prev (new attachment, new userTask, new signal, new property
    // rule) is append-class. We've already verified every existing entry survived unchanged.
    return MappingChangeClass.APPEND_CLASS;
  }

  private static Map<String, AttachmentDefinition> indexByKey(MappingDefinition def) {
    Map<String, AttachmentDefinition> out = new HashMap<>();
    for (AttachmentDefinition a : def.attachments()) {
      out.put(key(a), a);
    }
    return out;
  }

  /** Attachment identity for diff: {@code (type, scope)} pair. */
  private static String key(AttachmentDefinition a) {
    return a.type() + "|" + a.scope();
  }
}
