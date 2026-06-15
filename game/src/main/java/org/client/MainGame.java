package org.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lombok.Getter;
import lombok.Setter;
import org.client.menu.*;
import org.content.registry.PlayerRegistry;
import org.core.definition.PlayerProfile;
import org.core.enums.LevelOutcome;
import org.core.state.LevelStats;

import java.util.ArrayList;
import java.util.List;

public class MainGame extends Game {
    public SpriteBatch batch;
    private Music menuMusic;

    private SwitchMenu switchMenu;
    // Поле для збереження поточної мови інтерфейсу
    @Setter
    @Getter
    private LanguageUI currentLanguage = LanguageUI.UKRAINIAN;
    @Getter
    private int maxUnlockedLevel;
    @Getter
    @Setter
    private int currentLevel = 1;
    @Getter
    private LevelOutcome lastOutcome;
    @Getter
    private LevelStats lastStats;
    @Getter
    private int nextLevelId;
    @Getter
    @Setter
    private String currentPlayerId = PlayerRegistry.DEFAULT_PLAYER_ID;
    @Getter
    @Setter
    private float musicVolume = 0.02f;
    @Getter
    @Setter
    private float sfxVolume = 0.005f;

    private SaveManager saveManager;
    public void setLevelResult(LevelOutcome o, LevelStats s, int next) {
        lastOutcome = o;
        lastStats = s;
        nextLevelId = next;
    }
    @Override
    public void create() {
        saveManager = new SaveManager();
        int maxLevel = saveManager.getMaxUnlockedLevel();
        maxUnlockedLevel = maxLevel;
        batch = new SpriteBatch();

        PlayerRegistry playerRegistry = new PlayerRegistry();
        playerRegistry.init();

        List<PlayerProfile> allProfiles = new ArrayList<>(playerRegistry.getAll().values());

        // Ініціалізуємо менеджер перемикання екранів
        switchMenu = new SwitchMenu(this);

        // РЕЄСТРАЦІЯ ЕКРАНІВ
        switchMenu.registerScreen(MenuStatus.START_MENU, () -> new StartMenu(this, switchMenu));

        switchMenu.registerScreen(MenuStatus.PLAY_GAME_MENU, () -> new GameLevelScreen(this, switchMenu));

        switchMenu.registerScreen(MenuStatus.SELECT_LEVEL_MENU, () -> new SelectLevelMenu(this, switchMenu));

        switchMenu.registerScreen(MenuStatus.PAUSE_GAME_MENU, () -> new PauseMenu(this, switchMenu));

        switchMenu.registerScreen(MenuStatus.SETTINGS_MENU, () -> new SettingsMenu(this, switchMenu));

        // Передаємо базовий шлях та динамічно актуальну мову через метод getCurrentLanguage()
        switchMenu.registerScreen(MenuStatus.ABOUT_AUTHORS_MENU,
                () -> new MarkdownMenuScreen(this, switchMenu, "markdownForMenu/aboutAuthors", getCurrentLanguage()));

        switchMenu.registerScreen(MenuStatus.RULES_MENU,
                () -> new MarkdownMenuScreen(this, switchMenu, "markdownForMenu/rules", getCurrentLanguage()));
        switchMenu.registerScreen(MenuStatus.WIN_GAME_MENU,
                () -> new LevelCompletedMenu(this, switchMenu, lastOutcome, lastStats, nextLevelId));
        switchMenu.registerScreen(MenuStatus.DEFEAT_GAME_MENU,
                () -> new DefeatMenu(this, switchMenu));
        switchMenu.registerScreen(MenuStatus.SELECT_PLAYER_MENU,
                () -> new PlayerSelectScreen(this, switchMenu, allProfiles));
        switchMenu.registerScreen(MenuStatus.END_TITLE_SCREEN,
                () -> new EndTitleScreen(this, switchMenu));

        // Музика
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/menu.mp3"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(musicVolume);

        // Початковий запуск стартового меню
        switchMenu.switchMenu(MenuStatus.START_MENU);
    }
    @Override
    public void dispose() {
        batch.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
        if (menuMusic != null) {
            menuMusic.dispose();
        }
    }
    public void setMaxUnlockedLevel(int maxUnlockedLevel){
        this.maxUnlockedLevel = Math.max(this.maxUnlockedLevel, maxUnlockedLevel);
        saveManager.setMaxUnlockedLevel(maxUnlockedLevel);
    }

    public void playMenuMusic() {
        if (menuMusic != null && !menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    public void stopMenuMusic() {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.stop();
        }
    }

    public void updateMenuMusicVolume() {
        if (menuMusic != null) {
            menuMusic.setVolume(musicVolume);
        }
    }
}