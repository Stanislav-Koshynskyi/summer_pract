package org.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.client.menu.*;
import org.core.enums.MenuStatus;
import org.core.enums.LanguageUI;

public class MainGame extends Game {
    public SpriteBatch batch;

    private SwitchMenu switchMenu;
    // Поле для збереження поточної мови інтерфейсу
    private LanguageUI currentLanguage = LanguageUI.UKRAINIAN;

    private int maxUnlockedLevel = 1;


    public LanguageUI getCurrentLanguage() {
        return currentLanguage;
    }

    public void setCurrentLanguage(LanguageUI currentLanguage) {
        this.currentLanguage = currentLanguage;
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

        // Передаємо базовий шлях та динамічно актуальну мову через метод getCurrentLanguage()
        switchMenu.registerScreen(MenuStatus.ABOUT_AUTHORS_MENU,
                () -> new MarkdownMenuScreen(this, switchMenu, "markdownForMenu/aboutAuthors", getCurrentLanguage()));

        switchMenu.registerScreen(MenuStatus.SETTINGS_MENU,
                () -> new MarkdownMenuScreen(this, switchMenu, "markdownForMenu/settings", getCurrentLanguage()));

        switchMenu.registerScreen(MenuStatus.RULES_MENU,
                () -> new MarkdownMenuScreen(this, switchMenu, "markdownForMenu/rules", getCurrentLanguage()));

        // Початковий запуск стартового меню
        switchMenu.switchMenu(MenuStatus.START_MENU);
    }

    public int getMaxUnlockedLevel() {
        return maxUnlockedLevel;
    }

    public void setMaxUnlockedLevel(int maxUnlockedLevel) {
        this.maxUnlockedLevel = maxUnlockedLevel;
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}