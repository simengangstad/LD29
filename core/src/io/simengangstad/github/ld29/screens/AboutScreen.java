package io.simengangstad.github.ld29.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.simengangstad.github.ld29.gui.Font;

/**
 * @author simengangstad
 * @since 27.04.14
 */
public class AboutScreen implements Screen
{
    private LD29 game;
    private SpriteBatch spriteBatch;

    public AboutScreen(LD29 game, SpriteBatch spriteBatch)
    {
        this.game = game;
        this.spriteBatch = spriteBatch;
    }

    @Override
    public void show()
    {
    }

    @Override
    public void hide()
    {

    }

    @Override
    public void pause()
    {

    }

    @Override
    public void resume()
    {

    }

    @Override
    public void resize(int width, int height)
    {

    }

    @Override
    public void render(float delta)
    {
        this.spriteBatch.begin();

        Font.draw(this.spriteBatch, LD29.TITLE, 8, 8, Gdx.graphics.getWidth() / 2 - LD29.TITLE.length() * (8 * 4) / 2, Gdx.graphics.getHeight() - (8 * 4) * 2, 4);

        Font.draw(this.spriteBatch, "a game by flam",         8, 12, Gdx.graphics.getWidth() / 2 - 14 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 5, 3);
        Font.draw(this.spriteBatch, "made for the 29th",      8, 12, Gdx.graphics.getWidth() / 2 - 17 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 6, 3);
        Font.draw(this.spriteBatch, "ludum dare competition", 8, 12, Gdx.graphics.getWidth() / 2 - 22 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 7, 3);
        Font.draw(this.spriteBatch, "in 48 hours",            8, 12, Gdx.graphics.getWidth() / 2 - 11 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 8, 3);
        Font.draw(this.spriteBatch, "powered by libgdx",      8, 12, Gdx.graphics.getWidth() / 2 - 17 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 11, 3);

        Font.draw(this.spriteBatch, "esc-back", 8, 10, 10, 10, 3);

        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
        {
            this.game.setScreen(this.game.menuScreen);
        }

        this.spriteBatch.end();
    }

    @Override
    public void dispose()
    {

    }
}
