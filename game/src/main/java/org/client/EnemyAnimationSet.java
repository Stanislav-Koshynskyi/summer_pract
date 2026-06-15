package org.client;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.core.enums.AnimationState;

import java.util.HashMap;
import java.util.Map;

public class EnemyAnimationSet{
    private final Map<AnimationState, Animation<TextureRegion>> animations = new HashMap<>();

    public void addAnimation(AnimationState state, Animation<TextureRegion> anim) {
        animations.put(state, anim);
    }

    public TextureRegion getKeyFrame(AnimationState state, float stateTime, boolean loop) {
        Animation<TextureRegion> anim = animations.get(state);
        if (anim == null) {
            anim = animations.get(AnimationState.IDLE);
        }
        return anim != null ? anim.getKeyFrame(stateTime, loop) : null;
    }

    public boolean isAnimationFinished(AnimationState state, float stateTime) {
        Animation<TextureRegion> anim = animations.get(state);
        if (anim == null) return true;
        return anim.isAnimationFinished(stateTime);
    }
}
