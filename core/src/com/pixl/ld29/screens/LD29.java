package com.pixl.ld29.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LD29 extends Game
{
    public static String TITLE = "return to the surface";

    protected Screen menuScreen, aboutScreen, gameScreen;

    private SpriteBatch spriteBatch;

    @Override
    public void create()
    {
        this.spriteBatch = new SpriteBatch();

        this.menuScreen = new MenuScreen(this, spriteBatch);
        this.aboutScreen = new AboutScreen(this, spriteBatch);
        this.gameScreen = new GameScreen(this, spriteBatch);

        this.setScreen(this.menuScreen);
    }

    @Override
    public void resume()
    {
        super.resume();
    }

    @Override
    public void pause()
    {
        super.pause();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
    }

    @Override
    public void render()
    {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        super.render();
    }

    @Override
    public void dispose()
    {
        super.dispose();

        this.spriteBatch.dispose();

        this.menuScreen.dispose();
        this.aboutScreen.dispose();
        this.gameScreen.dispose();
    }
}
