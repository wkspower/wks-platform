package com.wks.caseengine.event.adapter.noop;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import com.wks.caseengine.event.WksEvent;
import com.wks.caseengine.event.WksEventEmitter;

@Component
@ConditionalOnMissingBean(WksEventEmitter.class)
public class NoOpWksEventEmitter implements WksEventEmitter {
    @Override
    public void emit(WksEvent event) {
        // No-Op
    }
}
