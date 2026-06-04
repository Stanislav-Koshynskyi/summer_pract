package org.core.collision;

public interface PathBlocker extends Blocker {
    // чи можна прокласти через нього маршрут, тобто якщо передбачається спеціальна поведінка розблокування
    // як для дверей
    boolean isTraversable();
    float getPathCost();
}