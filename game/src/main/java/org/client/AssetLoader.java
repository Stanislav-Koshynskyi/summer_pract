package org.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.core.definition.EnemyProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetLoader {
    private final List<Texture> allLoadedTextures = new ArrayList<>();
    private final Map<String, EnemyAnimationSet> enemyRegistry = new HashMap<>();

    public void load() {
        registerEnemy(
                "1",
                "sprites/enemies/sprColombian/sprColombianWalkMendoza/sprColombianWalkMendoza_", 8, 0.1f,
                "sprites/enemies/sprColombian/sprColombianWalkMendoza/sprColombianWalkMendoza_", 1, 0.1f,
                "sprites/enemies/sprColombian/sprColombianDie9mm/sprColombianDie9mm_", 14, 0.1f,
                "sprites/enemies/sprColombian/sprColombianAttackMendoza/sprColombianAttackMendoza_", 2, 0.05f
        );

        registerEnemy(
                "2",
                "sprites/enemies/sprEGang/sprEGangWalkShotgun/sprEGangWalkShotgun_", 8, 0.1f,
                "sprites/enemies/sprEGang/sprEGangWalkShotgun/sprEGangWalkShotgun_", 1, 0.1f,
                "sprites/enemies/sprEGang/sprEGangDie9mm/sprEGangDie9mm_", 5, 0.1f,
                "sprites/enemies/sprEGang/sprEGangAttackShotgun/sprEGangAttackShotgun_", 7, 0.05f
        );
    }

    private void registerEnemy(String typeName,
                               String walkPath, int walkFrames, float walkSpeed,
                               String idlePath, int idleFrames, float idleSpeed,
                               String diePath, int dieFrames, float dieSpeed,
                               String attackPath, int attackFrames, float attackSpeed) {

        EnemyAnimationSet animSet = new EnemyAnimationSet();

        // ХОДЬБА
        TextureRegion[] walkArray = new TextureRegion[walkFrames];
        for (int i = 0; i < walkFrames; i++) {
            Texture tex = new Texture(Gdx.files.internal(walkPath + (i + 1) + ".png"));
            allLoadedTextures.add(tex);
            walkArray[i] = new TextureRegion(tex);
        }
        animSet.addAnimation(AnimationState.WALK, new Animation<>(walkSpeed, walkArray));

        // СПОКІЙ
        TextureRegion[] idleArray = new TextureRegion[idleFrames];
        for (int i = 0; i < idleFrames; i++) {
            Texture tex = new Texture(Gdx.files.internal(idlePath + (i + 1) + ".png"));
            allLoadedTextures.add(tex);
            idleArray[i] = new TextureRegion(tex);
        }
        animSet.addAnimation(AnimationState.IDLE, new Animation<>(idleSpeed, idleArray));

        // ТРУП
        TextureRegion[] dieArray = new TextureRegion[1];
        for (int i = 0; i < dieFrames; i++) {
            Texture tex = new Texture(Gdx.files.internal(diePath + (i + 1) + ".png"));
            allLoadedTextures.add(tex);
            dieArray[0] = new TextureRegion(tex);
        }
        animSet.addAnimation(AnimationState.DYING, new Animation<>(dieSpeed, dieArray));

        // Атака
        TextureRegion[] attackArray = new TextureRegion[attackFrames];
        for (int i = 0; i < attackFrames; i++) {
            Texture tex = new Texture(Gdx.files.internal(attackPath + (i + 1) + ".png"));
            allLoadedTextures.add(tex);
            attackArray[i] = new TextureRegion(tex);
        }
        animSet.addAnimation(AnimationState.ATTACK, new Animation<>(attackSpeed, attackArray));

        enemyRegistry.put(typeName, animSet);
    }

    public EnemyAnimationSet getAnimationSet(String enemyProfile) {
        return enemyRegistry.get(enemyProfile);
    }

    public void dispose() {
        for (Texture tex : allLoadedTextures) {
            if (tex != null) tex.dispose();
        }
        allLoadedTextures.clear();
        enemyRegistry.clear();
    }
}
