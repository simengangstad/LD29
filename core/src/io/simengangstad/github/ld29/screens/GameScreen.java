package io.simengangstad.github.ld29.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import io.simengangstad.github.ld29.game.Level;
import io.simengangstad.github.ld29.gui.Font;

import java.util.Random;

/**
 * @author simengangstad
 * @since 26.04.14
 */
public class GameScreen implements Screen
{
    private LD29 game;

    private Level level;
    private SpriteBatch spriteBatch;

    private boolean freeze = true;

    private Matrix4 matrix4;

    public GameScreen(LD29 game, SpriteBatch spriteBatch)
    {
        this.game = game;
        this.spriteBatch = spriteBatch;
    }

    @Override
    public void show()
    {
        this.initialize();

        this.matrix4 = new Matrix4().setToOrtho2D(0.0f, 0.0f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void initialize()
    {
        if (this.level != null)
        {
            this.level.dispose();
        }

        char[][] map;

        Pixmap pixmap = new Pixmap(Gdx.files.internal("maps/Map_4.png"));

        map = new char[pixmap.getWidth()][pixmap.getHeight()];

        Random random = new Random();

        char currentDirt = Level.BlockType.DIRT_TYPE_ONE.value;
        char currentAir = Level.BlockType.AIR_ONE.value;

        for (int x = 0; x < pixmap.getWidth(); x++)
        {
            for (int y = 0; y < pixmap.getHeight(); y++)
            {
                switch (pixmap.getPixel(x, Math.abs(y - (pixmap.getHeight() - 1))))
                {
                    case 0x000000ff:

                        if (random.nextInt(100) < 40)
                        {
                            currentDirt = Level.BlockType.values()[random.nextInt(3) + 3].value;
                        }

                        map[x][y] = currentDirt;

                        break;

                    case 0xffffffff:

                        if (random.nextInt(100) < 50)
                        {
                            currentAir = Level.BlockType.values()[random.nextInt(3)].value;
                        }

                        map[x][y] = currentAir;

                        break;

                    case 0xff00ffff:

                        map[x][y] = Level.BlockType.SPAWN.value;

                        break;

                    case 0x00ff00ff:

                        map[x][y] = Level.BlockType.MOB.value;

                        break;

                    case 0xffff00ff:

                        map[x][y] = Level.BlockType.POWER.value;

                        break;
                }
            }
        }

        this.level = new Level(map);

        this.level.player.updateViewport();

        Gdx.input.setCursorCatched(true);
    }

    @Override
    public void hide()
    {
    }

    @Override
    public void resume()
    {
    }

    @Override
    public void pause()
    {
    }

    @Override
    public void resize(int width, int height)
    {
        this.level.player.updateViewport();
    }

    @Override
    public void render(float delta)
    {
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
        {
            Gdx.input.setCursorCatched(false);
        }

        if (!Gdx.input.isCursorCatched() && Gdx.input.justTouched())
        {
            Gdx.input.setCursorCatched(true);
        }

        this.level.draw(this.spriteBatch, this.freeze, delta);

        this.spriteBatch.begin();

        this.spriteBatch.setProjectionMatrix(matrix4);

        if (this.freeze && !this.level.player.isDead() && !this.level.player.foundPowerSource())
        {
            Font.draw(this.spriteBatch, LD29.TITLE, 8, 8, Gdx.graphics.getWidth() / 2 - LD29.TITLE.length() * (8 * 4) / 2, Gdx.graphics.getHeight() - (8 * 4) * 2, 4);

            Font.draw(this.spriteBatch, "ad to move",                   8, 12, Gdx.graphics.getWidth() / 2 - 10 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 5, 3);
            Font.draw(this.spriteBatch, "w to jump",                    8, 12, Gdx.graphics.getWidth() / 2 - 9 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 6, 3);
            Font.draw(this.spriteBatch, "w+shift to fly with jet pack", 8, 12, Gdx.graphics.getWidth() / 2 - 28 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 7, 3);
            Font.draw(this.spriteBatch, "left click to shoot",          8, 12, Gdx.graphics.getWidth() / 2 - 19 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 8, 3);
            Font.draw(this.spriteBatch, "click to start",               8, 12,  Gdx.graphics.getWidth() / 2 - 14 * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 13, 3);

            if (Gdx.input.isTouched() && Gdx.input.justTouched())
            {
                this.freeze = false;
            }
        }
        else
        {
            String score = "score: " + this.level.player.score;

            Font.draw(this.spriteBatch, score, 8, 10, Gdx.graphics.getWidth() - score.length() * (8 * 2), Gdx.graphics.getHeight() - (20 * 1), 2);
            Font.draw(this.spriteBatch, "power: " +  (int) this.level.player.power,  8, 10, Gdx.graphics.getWidth() - 10 * (8 * 2), Gdx.graphics.getHeight() - (20 * 3), 2);
            Font.draw(this.spriteBatch, "health: " + (int) this.level.player.health, 8, 10, Gdx.graphics.getWidth() - 11 * (8 * 2), Gdx.graphics.getHeight() - (20 * 4), 2);
        }

        if (this.level.player.isDead())
        {
            this.freeze = true;

            String score = "score: " + this.level.player.score;

            Font.draw(this.spriteBatch, "you died and did",      8, 12, Gdx.graphics.getWidth() / 2 - 16 * (8 * 3) / 2,             Gdx.graphics.getHeight() - (12 * 3) * 5, 3);
            Font.draw(this.spriteBatch, "not reach the meet-up", 8, 12, Gdx.graphics.getWidth() / 2 - 21 * (8 * 3) / 2,             Gdx.graphics.getHeight() - (12 * 3) * 6, 3);
            Font.draw(this.spriteBatch, score,                   8, 12, Gdx.graphics.getWidth() / 2 - score.length() * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 10, 3);
            Font.draw(this.spriteBatch, "click to restart",      8, 12, Gdx.graphics.getWidth() / 2 - 16 * (8 * 3) / 2,             Gdx.graphics.getHeight() - (12 * 3) * 13, 3);

            if (Gdx.input.isTouched() && Gdx.input.justTouched())
            {
                this.freeze = false;

                this.initialize();
            }
        }

        if (this.level.player.foundPowerSource())
        {
            this.freeze = true;

            String score = "score: " + this.level.player.score;

            Font.draw(this.spriteBatch, "you found a power stone",   8, 12, Gdx.graphics.getWidth() / 2 - 26 * (8 * 3) / 2,             Gdx.graphics.getHeight() - (12 * 3) * 5, 3);
            Font.draw(this.spriteBatch, "and etno reached the meet-up", 8, 12, Gdx.graphics.getWidth() / 2 - 28 * (8 * 3) / 2,             Gdx.graphics.getHeight() - (12 * 3) * 6, 3);
            Font.draw(this.spriteBatch, "in time.good job!",           8, 12, Gdx.graphics.getWidth() / 2 - 18 * (8 * 3) / 2,             Gdx.graphics.getHeight() - (12 * 3) * 7, 3);
            Font.draw(this.spriteBatch, score,                          8, 12, Gdx.graphics.getWidth() / 2 - score.length() * (8 * 3) / 2, Gdx.graphics.getHeight() - (12 * 3) * 10, 3);
            Font.draw(this.spriteBatch, "click to restart",             8, 12, Gdx.graphics.getWidth() / 2 - 16 * (8 * 3) / 2,             Gdx.graphics.getHeight() - (12 * 3) * 13, 3);

            if (Gdx.input.isTouched() && Gdx.input.justTouched())
            {
                this.freeze = false;

                this.initialize();
            }
        }

        this.spriteBatch.end();
    }

    @Override
    public void dispose()
    {
        if (this.level != null)
        {
            this.level.dispose();
        }
    }
}
