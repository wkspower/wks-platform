package com.wks.caseengine.command;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.wks.caseengine.event.WksEvent;
import com.wks.caseengine.event.WksEventEmitter;

import lombok.extern.slf4j.Slf4j;

/**
 * @author victor.franca
 *
 */
@Component
@Slf4j
public class CommandExecutorImpl implements CommandExecutor {

	@Autowired
	private CommandContext commandContext;

	@Autowired
	private WksEventEmitter eventEmitter;

	public <T> T execute(final Command<T> command) {
		T t = command.execute(commandContext);

		if (command instanceof AuditableCommand) {
			try {
				@SuppressWarnings("unchecked")
				AuditableCommand<Object> auditableCommand = (AuditableCommand<Object>) command;
				
				WksEvent wksEvent = WksEvent.builder()
						.id(java.util.UUID.randomUUID().toString())
						.tenantId(commandContext.getSecurityContextTenantHolder().getTenantId().orElse("default"))
						.userId(commandContext.getSecurityContextTenantHolder().getUserId().orElse("system"))
						.eventType(auditableCommand.getAuditEventType().name())
						.entityId(auditableCommand.getEntityId(commandContext))
						.entityType(auditableCommand.getEntityType())
						.payload(auditableCommand.getAuditPayload(commandContext, t))
						.timestamp(java.time.Instant.now().toString())
						.build();
				
				eventEmitter.emit(wksEvent);
			} catch (Exception e) {
				log.error("Failed to emit event for command {}", command.getClass().getSimpleName(), e);
			}
		}

		if (commandContext.getSecurityContextTenantHolder().getUserId().isPresent()) {
			log.debug("Command {} executed by user {}", command.getClass().getSimpleName(),
					commandContext.getSecurityContextTenantHolder().getUserId().get());
		} else {
			log.debug("Command {} executed", command.getClass().getSimpleName());
		}

		return t;
	}
}