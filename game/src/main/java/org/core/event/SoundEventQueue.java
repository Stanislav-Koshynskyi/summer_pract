package org.core.event;

import java.util.ArrayList;
import java.util.List;

public class SoundEventQueue {
    private final List<SoundEvent> events = new ArrayList<>();

    public void add(SoundEvent event) {
        events.add(event);
    }

    public List<SoundEvent> drain() {
        List<SoundEvent> result = new ArrayList<>(events);
        events.clear();
        return result;
    }

    public void clear() {
        events.clear();
    }

    public boolean isEmpty() {
        return events.isEmpty();
    }

    public int size() {
        return events.size();
    }
}

