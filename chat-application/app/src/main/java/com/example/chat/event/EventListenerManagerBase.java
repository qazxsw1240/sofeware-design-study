package com.example.chat.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class EventListenerManagerBase<L extends EventListener>
        implements EventListenerManager<L> {

    protected final List<EventListener> listeners;

    protected EventListenerManagerBase() {
        this.listeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void addListener(EventListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(EventListener listener) {
        this.listeners.remove(listener);
    }

    protected <M extends L> List<M> getListeners(Class<M> listenerClass) {
        return this.listeners.stream()
                .filter(listenerClass::isInstance)
                .map(listenerClass::cast)
                .toList();
    }

}
