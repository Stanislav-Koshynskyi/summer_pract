package org.core.event;

public abstract class GameEvent {
    public final long timestamp = System.currentTimeMillis();

    public abstract String getEventType();
}

