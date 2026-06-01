package org.core.event;

import org.core.enums.DoorState;

public class DoorStateChangedEvent extends GameEvent {
    public String doorId;
    public DoorState newState;

    public DoorStateChangedEvent(String doorId, DoorState newState) {
        this.doorId = doorId;
        this.newState = newState;
    }

    @Override
    public String getEventType() {
        return "DoorStateChanged";
    }
}


