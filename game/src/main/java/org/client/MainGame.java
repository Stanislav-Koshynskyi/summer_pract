package org.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lombok.Getter;
import lombok.Setter;
import org.client.menu.*;
import org.core.enums.LevelOutcome;
import org.core.enums.MenuStatus;
import org.core.enums.LanguageUI;
import org.core.state.LevelStats;

public class MainGame extends Game {
    public SpriteBatch batch;

    private SwitchMenu switchMenu;
    // Поле для збереження поточної мови інтерфейсу
    @Setter
    @Getter
    private LanguageUI currentLanguage = LanguageUI.UKRAINIAN;
    @Getter
    private int maxUnlockedLevel = 1;
    @Getter
    @Setter
    private int currentLevel = 1;
    @Getter
    private LevelOutcome lastOutcome;
    @Getter
    private LevelStats lastStats;
    @Getter
    private int nextLevelId;

    public void setLevelResult(LevelOutcome o, LevelStats s, int next) {
        lastOutcome = o;
        lastStats = s;
        nextLevelId = next;
    }
    @Override
    public void create() {
        batch = new SpriteBatch();

        // Ініціалізуємо менеджер перемикання екранів
        switchMenu = new SwitchMenu(this);

        // РЕЄСТРАЦІЯ ЕКРАНІВ
        switchMenu.registerScreen(MenuStatus.START_MENU, () -> new StartMenu(this, switchMenu));

        switchMenu.registerScreen(MenuStatus.INTRO_MENU, () -> new IntroMenu(this, switchMenu));

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
        // Початковий запуск стартового меню
        switchMenu.switchMenu(MenuStatus.START_MENU);
    }
    @Override
    public void dispose() {
        batch.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
    public void setMaxUnlockedLevel(int maxUnlockedLevel){
        this.maxUnlockedLevel = Math.max(this.maxUnlockedLevel, maxUnlockedLevel);
    }
}