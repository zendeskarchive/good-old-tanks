package com.getbase.hackkrk.tanks.server.callback

import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.SettableListenableFuture
import org.springframework.web.context.request.async.DeferredResult

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Slf4j
@Component
class Callbacks {

    private final ConcurrentMap<Event, SettableListenableFuture> callbacks = new ConcurrentHashMap<>()

    public <T> DeferredResult<T> on(Event<T> event) {
        log.info "Adding callback for {}", event
        def deferred = new DeferredResult<T>()
        callbacks.computeIfAbsent(event, { new SettableListenableFuture() }).addCallback({
            deferred.result = it
        }, {})
        deferred
    }

    public <T> void notify(Event<T> event, T value) {
        log.info "Notifying on event {}", event
        Optional
                .ofNullable(callbacks.remove(event))
                .ifPresent { it.set(value) }
    }

}
