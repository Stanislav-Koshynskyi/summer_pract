package org.core.event;

import org.core.enums.LevelOutcome;
import org.core.state.LevelStats;

public class LevelCompletedEvent extends GameEvent {
    public LevelOutcome outcome;
    public LevelStats levelStats;

    public LevelCompletedEvent(LevelOutcome outcome, LevelStats levelStats) {
        this.outcome = outcome;
        this.levelStats = levelStats;
    }

    @Override
    public String getEventType() {
        return "LevelCompleted";
    }
}


