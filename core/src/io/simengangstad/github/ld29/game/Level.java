package io.simengangstad.github.ld29.game;

import java.util.Random;

import box2dLight.PointLight;
import box2dLight.RayHandler;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

/**
 * @author simengangstad
 * @since 26.04.14
 */
public class Level
{
    private final Texture texture = new Texture(Gdx.files.internal("Sheet2.png"));

    public static final float ANIMATION_DURATION = 0.35f;

    private static final int MAX_AMOUNT_OF_MOBS = 1000, GENERATE_AMOUNT = 10, REFILL_TIME_MS = 1000 * 60;

    protected static final float GRAVITY = 50.0f;

    public static enum BlockType
    {
        AIR_ONE('1'), AIR_TWO('2'), AIR_THREE('3'), DIRT_TYPE_ONE('x'), DIRT_TYPE_TWO('y'), DIRT_TYPE_THREE('z'), GRAVEL_TYPE_ONE('c'), GRAVEL_TYPE_TWO('v'), GRAVEL_TYPE_THREE('b'), SPAWN('#'), MOB('m'), POWER('p');

        public final char value;

        BlockType(char value)
        {
            this.value = value;
        }
    }

    protected final char[][] array;

    protected final int scale = 50;

    private final TextureRegion dirtOne, dirtTwo, dirtThree, gravelOne, gravelTwo, gravelThree, power;

    private TextureRegion tmpRegion = new TextureRegion(), pointerRegion;

    public final Player player;

    private Array<Mob> mobs = new Array<>();

    private World world;

    protected RayHandler rayHandler;

    private Array<PointLight> powerLights = new Array<>();

    private Random random = new Random();

    private long lastRefillTime;

    private long refillTime;

    private Vector3 tmpUnproject = new Vector3();

    public Level(char[][] array)
    {
        this.world = new World(new Vector2(), true);

        RayHandler.useDiffuseLight(true);

        this.rayHandler = new RayHandler(this.world);
        this.rayHandler.setAmbientLight(0.15f, 0.15f, 0.15f, 1.0f);

        this.dirtOne = new TextureRegion(texture, 0, 0, 8, 8);
        this.dirtTwo = new TextureRegion(texture, 8, 0, 8, 8);
        this.dirtThree = new TextureRegion(texture, 16, 0, 8, 8);

        this.gravelOne = new TextureRegion(texture, 24, 0, 8, 8);
        this.gravelTwo = new TextureRegion(texture, 32, 0, 8, 8);
        this.gravelThree = new TextureRegion(texture, 40, 0, 8, 8);

        this.power = new TextureRegion(texture, 48, 16, 8, 8);

        this.pointerRegion = new TextureRegion(texture, 56, 0, 8, 8);

        this.array = array;

        Array<Vector2> spawnPositions = new Array<>();

        for (int y = 0; y < this.array[0].length; y++)
        {
            for (int x = 0; x < this.array.length; x++)
            {
                if (this.array[x][y] == BlockType.SPAWN.value)
                {
                    spawnPositions.add(new Vector2(x, y));
                }
                else if (this.array[x][y] == BlockType.MOB.value)
                {
                    this.mobs.add(new Mob(this.texture, this.rayHandler, x * this.scale, y * this.scale, this.scale));
                }
                else if (this.array[x][y] == BlockType.POWER.value)
                {
                    this.powerLights.add(new PointLight(this.rayHandler, 128, new Color(1.0f, 1.0f, 0.0f, 1.0f), 100.0f, x * this.scale + this.scale / this.power.getRegionWidth() * 4, y * this.scale + this.scale / this.power.getRegionHeight() * 4));
                }
                else if (this.isDirt(x, y) && this.isAir(x, y + 1))
                {
                    if (random.nextInt(100) < 60)
                    {
                        this.array[x][y + 1 % this.array[0].length] = BlockType.values()[random.nextInt(3) + 6].value;
                    }
                }
            }
        }

        Vector2 spawnPosition = spawnPositions.random();

        Gdx.app.log("vector", spawnPosition.toString());
        
        this.player = new Player(texture, this.rayHandler, spawnPosition.x * this.scale, spawnPosition.y * this.scale, this.scale, this.scale * 2);

        this.lastRefillTime = System.currentTimeMillis();
    }

    public boolean isDirt(int x, int y)
    {
        char value;

        try
        {
            value = this.array[x][y];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return false;
        }

        return value == BlockType.DIRT_TYPE_ONE.value ||
               value == BlockType.DIRT_TYPE_TWO.value ||
               value == BlockType.POWER.value ||
               value == BlockType.DIRT_TYPE_THREE.value;
    }

    public boolean isPower(int x, int y)
    {
        char value;

        try
        {
            value = this.array[x][y];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return false;
        }

        return value == BlockType.POWER.value;
    }

    public boolean isAir(int x, int y)
    {
        char value;

        try
        {
            value = this.array[x][y];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            return false;
        }
        return value == BlockType.AIR_ONE.value ||
                value == BlockType.AIR_TWO.value ||
                value == BlockType.AIR_THREE.value ||
                value == BlockType.GRAVEL_TYPE_ONE.value ||
                value == BlockType.GRAVEL_TYPE_TWO.value ||
                value == BlockType.GRAVEL_TYPE_THREE.value;
    }

    private final Color DARK_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.5f);
    private final Color NOT_DARK_COLOR = new Color(0.75f, 0.75f, 0.75f, 0.75f);

    public void draw(SpriteBatch spriteBatch, boolean freeze, float delta)
    {
        this.refillTime += System.currentTimeMillis() - this.lastRefillTime;

        this.lastRefillTime = System.currentTimeMillis();

        if (!freeze && this.refillTime >= Level.REFILL_TIME_MS)
        {
            this.generateMobs();

            this.refillTime = 0;
        }

        spriteBatch.begin();

        this.player.updateCamera(spriteBatch);

        for (int x = 0; x < this.array.length; x++)
        {
            for (int y = 0; y < this.array[0].length; y++)
            {
                switch (this.array[x][y])
                {
                    case '1':

                        this.tmpRegion = this.dirtOne;

                        spriteBatch.setColor(this.DARK_COLOR);

                        break;

                    case '2':

                        this.tmpRegion = this.dirtTwo;

                        spriteBatch.setColor(this.DARK_COLOR);

                        break;

                    case '3':

                        this.tmpRegion = this.dirtThree;

                        spriteBatch.setColor(this.DARK_COLOR);

                        break;

                    case 'x':

                        this.tmpRegion = this.dirtOne;

                        spriteBatch.setColor(this.NOT_DARK_COLOR);

                        break;

                    case 'y':

                        this.tmpRegion = this.dirtTwo;

                        spriteBatch.setColor(this.NOT_DARK_COLOR);

                        break;

                    case 'z':

                        this.tmpRegion = this.dirtThree;

                        spriteBatch.setColor(this.NOT_DARK_COLOR);

                        break;


                    case 'c':

                        this.tmpRegion = this.gravelOne;

                        spriteBatch.setColor(this.DARK_COLOR);
                        spriteBatch.draw(this.dirtThree, x * this.scale, y * this.scale, this.scale, this.scale);
                        spriteBatch.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                        break;

                    case 'v':

                        this.tmpRegion = this.gravelTwo;

                        spriteBatch.setColor(this.DARK_COLOR);
                        spriteBatch.draw(this.dirtTwo, x * this.scale, y * this.scale, this.scale, this.scale);
                        spriteBatch.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                        break;

                    case 'b':

                        this.tmpRegion = this.gravelThree;

                        spriteBatch.setColor(this.DARK_COLOR);
                        spriteBatch.draw(this.dirtOne, x * this.scale, y * this.scale, this.scale, this.scale);
                        spriteBatch.setColor(this.DARK_COLOR);

                        break;

                    case 'p':

                        this.tmpRegion = this.power;

                        spriteBatch.setColor(this.NOT_DARK_COLOR);

                        break;

                    case '#':
                    case 'm':

                        this.tmpRegion = this.dirtOne;

                        spriteBatch.setColor(this.DARK_COLOR);

                        break;
                }

                spriteBatch.draw(this.tmpRegion, x * this.scale, y * this.scale, this.scale, this.scale);
            }
        }


        for (Mob mob : this.mobs)
        {
            mob.draw(spriteBatch, freeze, this, this.player, delta);

            if (mob.health == 0.0f)
            {
                this.player.score++;

                mob.dispose(this.rayHandler);

                this.mobs.removeValue(mob, true);
            }
        }

        this.player.draw(spriteBatch, freeze, this, delta);

        this.tmpUnproject.set(Gdx.input.getX(), Gdx.input.getY(), 0.0f);

        this.player.orthographicCamera.unproject(this.tmpUnproject);

        spriteBatch.draw(this.pointerRegion, this.tmpUnproject.x - this.scale / 4, this.tmpUnproject.y - this.scale / 4, this.scale / 2, this.scale / 2);

        spriteBatch.end();

        this.rayHandler.setCombinedMatrix(this.player.orthographicCamera.combined);

        this.rayHandler.updateAndRender();
    }

    public void generateMobs()
    {
        System.out.println("Refilling with mobs");

        int i = 0;

        for (int y = 0; y < this.array[0].length; y++)
        {
            for (int x = 0; x < this.array.length; x++)
            {
                if (this.isAir(x, y))
                {
                    if (this.mobs.size + 1 <= Level.MAX_AMOUNT_OF_MOBS && i + 1 < Level.GENERATE_AMOUNT)
                    {
                        if (this.random.nextInt(100) < 40)
                        {
                            this.mobs.add(new Mob(this.texture, this.rayHandler, x, y, this.scale));

                            i++;
                        }
                    }
                    else
                    {
                        return;
                    }
                }
            }
        }
    }

    public void dispose()
    {
        this.texture.dispose();

        this.player.dispose(this.rayHandler);

        this.world.dispose();

        this.rayHandler.dispose();
    }
}
