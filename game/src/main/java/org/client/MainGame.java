package org.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lombok.Getter;
import lombok.Setter;
import org.client.menu.*;
import org.core.enums.MenuStatus;
import org.core.enums.LanguageUI;

public class MainGame extends Game {
    public SpriteBatch batch;

    private SwitchMenu switchMenu;
    // Поле для збереження поточної мови інтерфейсу
    @Setter
    @Getter
    private LanguageUI currentLanguage = LanguageUI.UKRAINIAN;
    @Setter
    @Getter
    private int maxUnlockedLevel = 2;
    @Getter
    @Setter
    private int currentLevel = 1;

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