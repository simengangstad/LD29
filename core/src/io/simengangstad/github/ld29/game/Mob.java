package io.simengangstad.github.ld29.game;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

import java.util.Random;

/**
 * @author simengangstad
 * @since 26.04.14
 */
public class Mob
{
    private static final Random random = new Random();

    private final long TIME_TO_DAMAGE = 50;

    private final float RADIUS = 500.0f;

    private TextureRegion textureRegion;

    private float x, y;

    private final int scale;

    private boolean jumping = false, shot = false, shotToRight = false;

    private float yVelocity, xVelocity;

    protected float health = 1.0f;

    private final Rectangle tmpRectangle;

    private long damageTime = 0, lastDamageTime = 0;

    private float direction;

    private int directionSteps = 0;

    private float lastPosition;

    private PointLight leftEye, rightEye;

    private Animation animation;

    private float stateTime;

    public Mob(Texture texture, RayHandler rayHandler, float x, float y, int scale)
    {
        this.textureRegion = new TextureRegion(texture, 0, 8, 8, 8);

        this.x = x;
        this.y = y;

        this.scale = scale;

        this.tmpRectangle = new Rectangle(0.0f, 0.0f, scale, scale);

        this.leftEye = new PointLight(rayHandler, 128, new Color(1.0f, 0.0f, 0.0f, 1.0f), 10.0f, 0.0f, 0.0f);
        this.rightEye = new PointLight(rayHandler, 128, new Color(1.0f, 0.0f, 0.0f, 1.0f), 10.0f, 0.0f, 0.0f);

        TextureRegion[][] textureRegions = TextureRegion.split(texture, 8, 8);

        TextureRegion[] ani = new TextureRegion[4];

        ani[0] = textureRegions[1][0];
        ani[1] = textureRegions[1][1];
        ani[2] = textureRegions[1][2];
        ani[3] = textureRegions[1][3];

        this.animation = new Animation(Level.ANIMATION_DURATION, ani);
    }

    public void draw(SpriteBatch spriteBatch, boolean freeze, Level level, Player player, float delta)
    {
        this.tmpRectangle.setX(this.x);
        this.tmpRectangle.setY(this.y);

        float moveX = direction * delta;
        float moveY = 0.0f;

        for (Player.Projectile projectile : player.projectiles)
        {
            if (projectile.rectangle.overlaps(this.tmpRectangle))
            {
                level.rayHandler.lightList.removeValue(projectile.pointLight, true);

                player.projectiles.removeValue(projectile, true);

                this.health -= 0.5f;

                if (player.x - this.x < 0 == this.direction > 0)
                {
                    this.direction = -this.direction;
                }

                this.directionSteps += 20;

                this.direction *= 1.5f;

                this.shotToRight = player.x < this.x;

                this.xVelocity = (this.shotToRight ? 4.1f : -4.1f);
                this.yVelocity = 5.0f;

                this.shot = true;
                this.jumping = true;

                player.explosion.play();

                break;
            }
        }

        long now = System.currentTimeMillis();

        if (player.rectangle.overlaps(this.tmpRectangle))
        {
            if (this.lastDamageTime == 0)
            {
                this.lastDamageTime = System.currentTimeMillis();
            }

            this.damageTime += now - this.lastDamageTime;

            this.lastDamageTime = now;

            if (this.damageTime >= this.TIME_TO_DAMAGE)
            {
                player.damage(0.5f);

                this.damageTime = 0;
                this.lastDamageTime = 0;
            }
        }

        if (this.directionSteps == 0)
        {
            this.directionSteps = Mob.random.nextInt(5) + 35;

            this.direction = 125;
        }

        if (Math.abs(this.x - player.x) < this.RADIUS && Math.abs(this.y - player.y) < this.RADIUS)
        {
            if (player.x - this.x < 0 == this.direction > 0)
            {
                this.direction = -this.direction;
            }
        }

        if (Math.abs(this.lastPosition - this.x) >= this.scale)
        {
            this.lastPosition = x;

            this.directionSteps--;
        }

        if (this.jumping || level.isAir(Math.round((this.x + moveX) / (float) level.scale), (int) ((this.y - this.direction * delta) / level.scale)))
        {
            if (this.shot)
            {
                if (this.shotToRight)
                {
                    this.xVelocity += Level.GRAVITY * delta;
                }
                else
                {
                    this.xVelocity -= Level.GRAVITY * delta;
                }

                moveX = this.xVelocity;
            }

            this.yVelocity -= Level.GRAVITY * delta;

            moveY = this.yVelocity;
        }

        float posx = this.x + moveX;
        float posy = this.y + moveY;

        posx /= level.scale;
        posy /= level.scale;

        try
        {
            if (this.direction > 0)
            {
                if (level.isDirt((int) Math.floor(posx + 1), (int) Math.floor(posy)))
                {
                    if (level.isAir((int) Math.floor(posx + (this.direction < 0 ? -1 : 1)), (int) Math.floor(posy + 1)))
                    {
                        jumping = true;

                        this.yVelocity = 12.5f;
                    }
                    else
                    {
                        this.direction = -direction;
                    }
                }
            }
            else
            {
                if (level.isDirt((int) Math.ceil(posx - 1), (int) Math.ceil(posy)))
                {
                    if (level.isAir((int) Math.ceil(posx - 1), (int) Math.ceil(posy + 1)))
                    {
                        jumping = true;

                        this.yVelocity = 12.5f;
                    }
                    else
                    {
                        this.direction = -direction;
                    }
                }
            }


            if (moveY < 0.0f)
            {
                if (level.isDirt(Math.round(posx), (int) posy))
                {
                    this.yVelocity = 0.0f;

                    this.y = (int) (posy + 1) * level.scale;

                    moveY = 0.0f;

                    this.jumping = false;
                    this.shot = false;
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException exception)
        {
            Gdx.app.error("Exception", exception.getMessage());
        }

        if (!freeze)
        {
            this.x += moveX;
            this.y += moveY;
        }

        this.textureRegion = this.animation.getKeyFrame(this.stateTime, true);

        spriteBatch.setColor(1.0f, health, health, 1.0f);
        spriteBatch.draw(this.textureRegion, this.x, this.y, this.scale, this.scale);

        this.leftEye.setPosition(this.x + this.scale / 2.0f / this.textureRegion.getRegionWidth() * 4, this.y + this.scale / 2.0f / this.textureRegion.getRegionHeight() * 11);
        this.rightEye.setPosition(this.x + this.scale / 2.0f / this.textureRegion.getRegionWidth() * 12, this.y + this.scale / 2.0f / this.textureRegion.getRegionHeight() * 11);

        if (!freeze)
        {
            this.stateTime += delta;
        }


        if (this.stateTime >= this.animation.animationDuration)
        {
            this.stateTime = 0.0f;
        }
    }

    public void dispose(RayHandler rayHandler)
    {
        rayHandler.lightList.removeValue(this.leftEye, true);
        rayHandler.lightList.removeValue(this.rightEye, true);
    }
}
