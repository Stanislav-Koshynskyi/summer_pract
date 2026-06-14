package org.client;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.client.menu.StartMenu;

public class MainGame extends Game {
    public SpriteBatch batch;
    public AssetLoader assetLoader;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetLoader = new AssetLoader();
        assetLoader.load();

        this.setScreen(new StartMenu(this));
    }

    @Override
    public void dispose() {
        batch.dispose();
        assetLoader.dispose();
    }
}
