package org.client.menu;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import org.core.enums.MenuStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SwitchMenu {
    private final Game game;
    // Реєстр фабрик для створення екранів
    private final Map<MenuStatus, Supplier<Screen>> screenRegistry = new HashMap<>();

    public SwitchMenu(Game game) {
        this.game = game;
    }

    /**
     * Реєструє спосіб створення екрану для певного статусу.
     */
    public void registerScreen(MenuStatus status, Supplier<Screen> screenSupplier) {
        screenRegistry.put(status, screenSupplier);
    }

    /**
     * Перемикає екран відповідно до статусу.
     */
    public void switchMenu(MenuStatus status) {
        Supplier<Screen> supplier = screenRegistry.get(status);

        if (supplier != null) {
            // 1. Спочатку запам'ятовуємо посилання на поточний активний екран
            Screen currentScreen = game.getScreen();

            // 2. Створюємо та встановлюємо НОВИЙ екран (це безпечно перемикає контекст двигуна)
            Screen nextScreen = supplier.get();
            game.setScreen(nextScreen);

            // 3. Тільки ТЕПЕР безпечно очищаємо ресурси старого екрана, який вже не рендериться
            if (currentScreen != null) {
                currentScreen.dispose();
            }
        } else {
            Gdx.app.error("SwitchMenu", "Екран для статусу " + status + " не зареєстрований!");
        }
    }
}