package com.wks.caseengine.event.adapter.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import com.wks.caseengine.event.WksEvent;
import com.wks.caseengine.event.WksEventEmitter;

@Component
@ConditionalOnProperty(name = "wks.event.provider", havingValue = "spring", matchIfMissing = true)
public class SpringWksEventEmitter implements WksEventEmitter {

    private final ApplicationEventPublisher eventPublisher;

    public SpringWksEventEmitter(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void emit(WksEvent event) {
        eventPublisher.publishEvent(event);
    }
}
