package org.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import org.core.enums.AnimationState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssetLoader {
    private final List<Texture> allLoadedTextures = new ArrayList<>();
    private final Map<String, EnemyAnimationSet> enemyRegistry = new HashMap<>();
    private final Map<String, EnemyAnimationSet> playerRegistry = new HashMap<>();

    public void load() {
        registerEnemy(
                "Colombian",
                "Knife",
                "sprites/enemies/sprColombian/sprColombianWalkKnife/sprColombianWalkKnife_", 8, 0.1f,
                "sprites/enemies/sprColombian/sprColombianWalkKnife/sprColombianWalkKnife_", 1, 0.1f,
                "sprites/enemies/sprColombian/sprColombianDie9mm/sprColombianDie9mm_", 14, 0.1f,
                "sprites/enemies/sprColombian/sprColombianAttackKnife/sprColombianAttackKnife_", 9, 0.05f
        );

        registerEnemy(
                "Colombian",
                "9mm",
                "sprites/enemies/sprColombian/sprColombianWalkSilencer/sprColombianWalkSilencer_", 8, 0.1f,
                "sprites/enemies/sprColombian/sprColombianWalkSilencer/sprColombianWalkSilencer_", 1, 0.1f,
                "sprites/enemies/sprColombian/sprColombianDie9mm/sprColombianDie9mm_", 14, 0.1f,
                "sprites/enemies/sprColombian/sprColombianWalkSilencer/sprColombianWalkSilencer_", 2, 0.05f
        );

        registerEnemy(
                "Colombian",
                "Shotgun",
                "sprites/enemies/sprColombian/sprColombianWalkShotgun/sprColombianWalkShotgun_", 8, 0.1f,
                "sprites/enemies/sprColombian/sprColombianWalkShotgun/sprColombianWalkShotgun_", 1, 0.1f,
                "sprites/enemies/sprColombian/sprColombianDie9mm/sprColombianDie9mm_", 14, 0.1f,
                "sprites/enemies/sprColombian/sprColombianAttackShotgun/sprColombianAttackShotgun_", 5, 0.05f
        );

        registerEnemy(
                "Colombian",
                "Uzi",
                "sprites/enemies/sprColombian/sprColombianWalkMendoza/sprColombianWalkMendoza_", 8, 0.1f,
                "sprites/enemies/sprColombian/sprColombianWalkMendoza/sprColombianWalkMendoza_", 1, 0.1f,
                "sprites/enemies/sprColombian/sprColombianDie9mm/sprColombianDie9mm_", 14, 0.1f,
                "sprites/enemies/sprColombian/sprColombianAttackMendoza/sprColombianAttackMendoza_", 2, 0.05f
        );

        registerEnemy(
                "Colombian",
                "Famae",
                "sprites/enemies/sprColombian/sprColombianWalkFamae/sprColombianWalkFamae_", 8, 0.1f,
                "sprites/enemies/sprColombian/sprColombianWalkFamae/sprColombianWalkFamae_", 1, 0.1f,
                "sprites/enemies/sprColombian/sprColombianDie9mm/sprColombianDie9mm_", 14, 0.1f,
                "sprites/enemies/sprColombian/sprColombianAttackFamae/sprColombianAttackFamae_", 2, 0.05f
        );

        registerEnemy(
                "EGang",
                "Knife",
                "sprites/enemies/sprEGang/sprEGangWalkKnife/sprEGangWalkKnife_", 8, 0.1f,
                "sprites/enemies/sprEGang/sprEGangWalkKnife/sprEGangWalkKnife_", 1, 0.1f,
                "sprites/enemies/sprEGang/sprEGangDie9mm/sprEGangDie9mm_", 5, 0.1f,
                "sprites/enemies/sprEGang/sprEGangAttackKnife/sprEGangAttackKnife_", 9, 0.05f
        );

        registerEnemy(
                "EGang",
                "9mm",
                "sprites/enemies/sprEGang/sprEGangWalk9mm/sprEGangWalk9mm_", 8, 0.1f,
                "sprites/enemies/sprEGang/sprEGangWalk9mm/sprEGangWalk9mm_", 1, 0.1f,
                "sprites/enemies/sprEGang/sprEGangDie9mm/sprEGangDie9mm_", 5, 0.1f,
                "sprites/enemies/sprEGang/sprEGangAttack9mm/sprEGangAttack9mm_", 2, 0.05f
        );

        registerEnemy(
                "EGang",
                "Shotgun",
                "sprites/enemies/sprEGang/sprEGangWalkShotgun/sprEGangWalkShotgun_", 8, 0.1f,
                "sprites/enemies/sprEGang/sprEGangWalkShotgun/sprEGangWalkShotgun_", 1, 0.1f,
                "sprites/enemies/sprEGang/sprEGangDie9mm/sprEGangDie9mm_", 5, 0.1f,
                "sprites/enemies/sprEGang/sprEGangAttackShotgun/sprEGangAttackShotgun_", 7, 0.05f
        );

        registerEnemy(
                "EGang",
                "Uzi",
                "sprites/enemies/sprEGang/sprEGangWalkUzi/sprEGangWalkUzi_", 8, 0.1f,
                "sprites/enemies/sprEGang/sprEGangWalkUzi/sprEGangWalkUzi_", 1, 0.1f,
                "sprites/enemies/sprEGang/sprEGangDie9mm/sprEGangDie9mm_", 5, 0.1f,
                "sprites/enemies/sprEGang/sprEGangAttackUzi/sprEGangAttackUzi_", 2, 0.05f
        );

        // GangLeader
        registerEnemy(
                "GangLeader",
                "Machete",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerEnemy(
                "GangLeader",
                "Pipe",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerEnemy(
                "GangLeader",
                "Knife",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerEnemy(
                "GangLeader",
                "Famae",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerEnemy(
                "GangLeader",
                "9mm",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerEnemy(
                "GangLeader",
                "Shotgun",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerEnemy(
                "GangLeader",
                "Silencer",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerEnemy(
                "GangLeader",
                "Silencer",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerEnemy(
                "GangLeader",
                "Bat",
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 8, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderWalk/sprGangLeaderWalk_", 1, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderDie/sprGangLeaderDie_", 5, 0.1f,
                "sprites/enemies/sprGangLeader/sprGangLeaderBang/sprGangLeaderBang_", 7, 0.05f
        );

        registerPlayer(
                "1",
                "sprites/players/sprJacket/sprJacketWalkKalashnikov/sprJacketWalkKalashnikov_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalkKalashnikov/sprJacketWalkKalashnikov_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttackKalashnikov/sprJacketAttackKalashnikov_", 2, 0.05f
        );

        registerPlayer(
                "Knife",
                "sprites/players/sprJacket/sprJacketWalkKnife/sprJacketWalkKnife_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalkKnife/sprJacketWalkKnife_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttackKnife/sprJacketAttackKnife_", 9, 0.05f
        );

        registerPlayer(
                "9mm",
                "sprites/players/sprJacket/sprJacketWalk9mm/sprJacketWalk9mm_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalk9mm/sprJacketWalk9mm_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttack9mm/sprJacketAttack9mm_", 2, 0.05f
        );

        registerPlayer(
                "Shotgun",
                "sprites/players/sprJacket/sprJacketWalkShotgun/sprJacketWalkShotgun_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalkShotgun/sprJacketWalkShotgun_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttackShotgun/sprJacketAttackShotgun_", 6, 0.05f
        );

        registerPlayer(
                "Uzi",
                "sprites/players/sprJacket/sprJacketWalkUzi/sprJacketWalkUzi_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalkUzi/sprJacketWalkUzi_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttackUzi/sprJacketAttackUzi_", 2, 0.05f
        );

        registerPlayer(
                "Famae",
                "sprites/players/sprJacket/sprJacketWalkKalashnikov/sprJacketWalkKalashnikov_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalkKalashnikov/sprJacketWalkKalashnikov_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttackKalashnikov/sprJacketAttackKalashnikov_", 2, 0.05f
        );

        registerPlayer(
                "Pipe",
                "sprites/players/sprJacket/sprJacketWalkPipe/sprJacketWalkPipe_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalkPipe/sprJacketWalkPipe_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttackPipe/sprJacketAttackPipe_", 9, 0.05f
        );

        registerPlayer(
                "Bat",
                "sprites/players/sprJacket/sprJacketWalkBat/sprJacketWalkBat_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalkBat/sprJacketWalkBat_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttackBat/sprJacketAttackBat_", 9, 0.05f
        );

        registerPlayer(
                "Silencer",
                "sprites/players/sprJacket/sprJacketWalkSilencer/sprJacketWalkSilencer_", 8, 0.1f,
                "sprites/players/sprJacket/sprJacketWalkSilencer/sprJacketWalkSilencer_", 1, 0.1f,
                "sprites/players/sprJacket/sprJacketDeadCut/sprJacketDeadCut_", 3, 0.1f,
                "sprites/players/sprJacket/sprJacketAttackSilencer/sprJacketAttackSilencer_", 2, 0.05f
        );
    }

    private void registerEnemy(String typeName, String weaponId,
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

        String combinedKey = typeName + "_" + weaponId;
        enemyRegistry.put(combinedKey, animSet);
    }

    private void registerPlayer(String weaponId,
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

        // АТАКА
        TextureRegion[] attackArray = new TextureRegion[attackFrames];
        for (int i = 0; i < attackFrames; i++) {
            Texture tex = new Texture(Gdx.files.internal(attackPath + (i + 1) + ".png"));
            allLoadedTextures.add(tex);
            attackArray[i] = new TextureRegion(tex);
        }
        animSet.addAnimation(AnimationState.ATTACK, new Animation<>(attackSpeed, attackArray));

        playerRegistry.put(weaponId, animSet);
    }

    public EnemyAnimationSet getAnimationSet(String typeName, String weaponId) {
        return enemyRegistry.get(typeName + "_" + weaponId);
    }

    public EnemyAnimationSet getAnimationSet(String combinedKey) {
        return enemyRegistry.get(combinedKey);
    }

    public EnemyAnimationSet getPlayerAnimationSet(String weaponId) {
        return playerRegistry.get(weaponId);
    }

    public void dispose() {
        for (Texture tex : allLoadedTextures) {
            if (tex != null) tex.dispose();
        }
        allLoadedTextures.clear();
        enemyRegistry.clear();
    }
}
