package com.pixl.ld29.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.pixl.ld29.gui.Font;

/**
 * @author simengangstad
 * @since 27.04.14
 */
public class MenuScreen implements Screen
{
    private LD29 game;

    private SpriteBatch spriteBatch;

    private int choiceY = 0;

    public MenuScreen(LD29 game, SpriteBatch spriteBatch)
    {
        this.game = game;
        this.spriteBatch = spriteBatch;
    }

    @Override
    public void show()
    {}

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

        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))
        {
            if (this.choiceY > -1)
            {
                this.choiceY--;
            }
        }
        else if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))
        {
            if (this.choiceY < 0)
            {
                this.choiceY++;
            }
        }

        Font.draw(this.spriteBatch, LD29.TITLE, 8, 8, Gdx.graphics.getWidth() / 2 - LD29.TITLE.length() * (8 * 4) / 2, Gdx.graphics.getHeight() - (8 * 4) * 2, 4);

        Font.draw(this.spriteBatch, ">", 8, 10, 50 - (8 * 3), 350 - 8 * (10 * 3) + this.choiceY * (10 * 3), 3);
        Font.draw(this.spriteBatch, "about", 8, 10, 50, 350 - 9 * (10 * 3), 3);
        Font.draw(this.spriteBatch, "play",  8, 10, 50, 350 - 9 * (10 * 3) + (10 * 3), 3);

        Font.draw(this.spriteBatch, "etno entered the wrong\n" +
                                    "coordinates on his tele-\n" +
                                    "porter on the way to a\n" +
                                    "ludum dare meet-up.\n\n" +
                                    "his teleporter is now \n" +
                                    "out of juice.find the\n" +
                                    "way to the power stones\n" +
                                    "and help etno reach the\n" +
                                    "meet-up!", 8, 10, 260, 350, 3);

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER))
        {
            if (this.choiceY == 0)
            {
                this.game.setScreen(this.game.gameScreen);
            }
            else
            {
                this.game.setScreen(this.game.aboutScreen);
            }
        }

        this.spriteBatch.end();
    }

    @Override
    public void dispose()
    {

    }
}
