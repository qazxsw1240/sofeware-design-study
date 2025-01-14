package com.example.chat.event;

public interface EventListenerManager<L extends EventListener> {

    public void addListener(L listener);

    public void removeListener(L listener);

}
