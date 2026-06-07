package org.core.state;

import org.core.entity.Door;
import org.core.entity.Enemy;
import org.core.entity.Player;
import org.core.entity.WeaponPickup;
import org.core.enums.DoorState;
import org.core.enums.GamePhase;
import org.core.math.Vec2;

public class GameStateView {

    private final LevelState state;

    public GameStateView(LevelState state) {
        this.state = state;
    }

    public GamePhase getPhase() {
        return state.getPhase();
    }

    public Vec2 getPlayerPosition() {
        Player p = state.getPlayer();
        return new Vec2(p.getX(), p.getY());
    }

    public float getPlayerFacingAngle() {
        // Player.facingAngle буде додано коли А реалізує aim
        return state.getPlayer().getFacingAngle();
    }
    public int getEnemyCount() {
        return state.getEnemies().size();
    }

    public EnemyView getEnemy(int index) {
        Enemy e = state.getEnemies().get(index);
        return new EnemyView(e);
    }

    public int getCorpseCount() {
        return state.getCorpses().size();
    }

    public CorpseView getCorpse(int index) {
        return new CorpseView(state.getCorpses().get(index));
    }

    public boolean isPlayerAtExit() {
        return state.isPlayerAtExit();
    }

    public boolean areAllEnemiesDead() {
        return state.areAllEnemiesDead();
    }

    public boolean isEnemyAlive(String id) {
        return state.isEnemyAlive(id);
    }

    public StatsView getStats() {
        return new StatsView(state.getStats());
    }

    public int getDoorCount() {
        return state.getDoors().size();
    }

    public DoorView getDoor(int i) {
        return new DoorView(state.getDoors().get(i));
    }

    public int getPickupCount() {
        return state.getPickups().size();
    }

    public PickupView getPickup(int i) {
        return new PickupView(state.getPickups().get(i));
    }


    public static class EnemyView {
        private final Enemy e;

        EnemyView(Enemy e) {
            this.e = e;
        }

        public float getX() {
            return e.getX();
        }

        public float getY() {
            return e.getY();
        }

        public Vec2 getPosition() {
            return new Vec2(e.getX(), e.getY());
        }

        public float getFacingAngle() {
            return e.getFacingAngle();
        }

        public String getAiState() {
            return e.getCurrentState().name();
        }

        public String getEnemyId() {
            return e.getEnemyId();
        }

        public String getEnemyTypeId() {
            return e.getProfile().getEnemyTypeId();
        }

        public float getVisionRange() {
            return e.getProfile().getVisionRange();
        }

        public float getLastKnownPlayerX() {
            return e.getLastKnownPlayerX();
        }

        public float getLastKnownPlayerY() {
            return e.getLastKnownPlayerY();
        }
    }

    public static class CorpseView {
        private final Enemy e;

        CorpseView(Enemy e) {
            this.e = e;
        }

        public float getX() {
            return e.getX();
        }

        public float getY() {
            return e.getY();
        }

        public Vec2 getPosition() {
            return new Vec2(e.getX(), e.getY());
        }

        public float getFacingAngle() {
            return e.getFacingAngle();
        }

        public String getEnemyTypeId() {
            return e.getProfile().getEnemyTypeId();
        }
    }
    public static class DoorView {
        private final Door d;

        DoorView(Door d) {
            this.d = d;
        }

        public String getDoorId() {
            return d.getDoorId();
        }

        public DoorState getState() {
            return d.getState();
        }

        public float getX() {
            return d.getX();
        }

        public float getY() {
            return d.getY();
        }

        public float getWidth() {
            return d.getWidth();
        }

        public float getHeight() {
            return d.getHeight();
        }

        public boolean isOpen() {
            return d.getState() == DoorState.OPEN;
        }

        public boolean isClosed() {
            return d.getState() == DoorState.CLOSED;
        }

        public boolean isOpening() {
            return d.getState() == DoorState.OPENING;
        }

        public float getOpenProgress() {
            if (d.getState() != DoorState.OPENING) return 0f;
            return 1f - (d.getOpeningTimer() / d.getOpeningDuration());
        }
    }

    public static class PickupView {
        private final WeaponPickup p;

        PickupView(WeaponPickup p) {
            this.p = p;
        }

        public String getPickupId() {
            return p.getPickupId();
        }

        public String getWeaponId() {
            return p.getWeaponId();
        }

        public float getX() {
            return p.getX();
        }

        public float getY() {
            return p.getY();
        }
        // порожню треба рендерити інакше, щоб було видно що не вийде підібрати
        public boolean isEmpty() {
            return p.isEmpty();
        }
    }
    public boolean isCurrentWeaponAutomatic() {
        return state.getPlayer().getCurrentWeapon().getDefinition().isAutomatic();
    }

    public static class StatsView {
        private final LevelStats s;

        StatsView(LevelStats s) {
            this.s = s;
        }

        public int getUniqueEnemiesAlerted() {
            return s.getUniqueEnemiesAlerted();
        }

        public int getUniqueEnemiesEnteredAttack() {
            return s.getUniqueEnemiesEnteredAttack();
        }

        public int getUniqueEnemiesEnteredSearchOrInvestigate() {
            return s.getUniqueEnemiesEnteredSearchOrInvestigate();
        }

        public int getAlertCount() {
            return s.getAlertCount();
        }

        public boolean isGunshotFired() {
            return s.isGunshotFired();
        }

        public boolean isSuppressedShotFired() {
            return s.isSuppressedShotFired();
        }

        public int getKillCount() {
            return s.getKillCount();
        }

        public int getSilentKillCount() {
            return s.getSilentKillCount();
        }

        public float getElapsedTime() {
            return s.getElapsedTime();
        }
    }
}