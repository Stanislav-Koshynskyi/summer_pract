package org.core.event;

import org.core.enums.LevelOutcome;

public class LevelCompletedEvent extends GameEvent {
    public LevelOutcome outcome;

    public LevelCompletedEvent(LevelOutcome outcome) {
        this.outcome = outcome;
    }

    @Override
    public String getEventType() {
        return "LevelCompleted";
    }
}


