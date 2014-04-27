package com.pixl.ld29.game;

import box2dLight.ConeLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * @author simengangstad
 * @since 26.04.14
 */
public class Player
{
    public class Projectile
    {
        public Rectangle rectangle;
        public Vector2 direction;
        public PointLight pointLight;

        public Projectile(Rectangle rectangle, Vector2 direction, PointLight pointLight)
        {
            this.rectangle = rectangle;
            this.direction = direction;
            this.pointLight = pointLight;
        }
    }

    private final float SPEED = 300.0f;

    private final int MS_BETWEEN_PER_BLAST = 120;

    private long blastInterval;

    private long lastTime;

    protected OrthographicCamera orthographicCamera;

    protected float x, y;

    protected float angle;

    protected final int xScale, yScale;

    private final TextureRegion blasterTextureRegion, blastTextureRegion;

    private boolean jumping = false, facingRight = false, falling = true;

    private float yVelocity;

    private Vector3 tmpVector = new Vector3();

    protected Array<Projectile> projectiles = new Array<Projectile>();

    private Rectangle tmpRectangle = new Rectangle();

    protected final Rectangle rectangle;

    public float health = 100.0f;

    public float power = 100.0f;

    public int score;

    private ConeLight coneLight, jetPackLight;

    private PointLight eye, blaster;

    protected Sound blast, explosion, jetPack;

    private Animation standingAnimation, walkingAnimation, hoveringAnimation;

    private TextureRegion currentFrame;

    private Animation currentAnimation;

    private TextureRegion jumpingTextureRegion;

    private float stateTime;

    private boolean foundPowerSource = false;

    private long lastJetPackTime, jetPackTime;

    public Player(Texture texture, RayHandler rayHandler, float x, float y, int xScale, int yScale)
    {
        this.blasterTextureRegion = new TextureRegion(texture, 32, 8, 16, 8);
        this.blastTextureRegion = new TextureRegion(texture, 48, 8, 8, 8);

        this.orthographicCamera = new OrthographicCamera();

        this.x = x;
        this.y = y;

        this.orthographicCamera.position.y = this.y;

        this.xScale = xScale;
        this.yScale = yScale;

        this.rectangle = new Rectangle(0.0f, 0.0f, this.xScale, this.yScale);

        this.coneLight = new ConeLight(rayHandler, 128, new Color(1.0f, 1.0f, 1.0f, 1.0f), 150.0f, 0.0f, 0.0f, this.angle, 20.0f);
        this.jetPackLight = new ConeLight(rayHandler, 128, new Color(0.0f, 1.0f, 1.0f, 1.0f), 40.0f, 0.0f, 0.0f, 270.0f, 15.0f);

        this.eye = new PointLight(rayHandler, 128, new Color(0.0f, 0.0f, 1.0f, 1.0f), 10.0f, 0.0f, 0.0f);

        this.blaster = new PointLight(rayHandler, 128, new Color(1.0f, 0.0f, 0.0f, 1.0f), 10.0f, 0.0f, 0.0f);

        this.explosion = Gdx.audio.newSound(Gdx.files.internal("sfx/Explosion.wav"));
        this.blast = Gdx.audio.newSound(Gdx.files.internal("sfx/Shot.wav"));
        this.jetPack = Gdx.audio.newSound(Gdx.files.internal("sfx/jetpack.wav"));

        TextureRegion[][] textureRegions = TextureRegion.split(texture, texture.getWidth() / 8, texture.getHeight() / 4);

        TextureRegion[] standingAnimation = new TextureRegion[4];

        standingAnimation[0] = textureRegions[1][0];
        standingAnimation[1] = textureRegions[1][1];
        standingAnimation[2] = textureRegions[1][2];
        standingAnimation[3] = textureRegions[1][3];

        this.standingAnimation = new Animation(Level.ANIMATION_DURATION, standingAnimation);

        TextureRegion[] hoveringAnimation = new TextureRegion[4];

        hoveringAnimation[0] = textureRegions[2][1];
        hoveringAnimation[1] = textureRegions[2][2];
        hoveringAnimation[2] = textureRegions[2][3];
        hoveringAnimation[3] = textureRegions[2][4];

        this.hoveringAnimation = new Animation(Level.ANIMATION_DURATION, hoveringAnimation);

        TextureRegion[] walkingAnimation = new TextureRegion[4];

        walkingAnimation[0] = textureRegions[3][0];
        walkingAnimation[1] = textureRegions[3][1];
        walkingAnimation[2] = textureRegions[3][2];
        walkingAnimation[3] = textureRegions[3][3];

        this.walkingAnimation = new Animation(Level.ANIMATION_DURATION, walkingAnimation);

        this.jumpingTextureRegion = textureRegions[2][0];

        this.currentAnimation = this.standingAnimation;

        this.currentFrame = this.currentAnimation.getKeyFrame(this.stateTime);
    }

    public void updateViewport()
    {
        this.orthographicCamera.setToOrtho(false);
    }

    public void updateCamera(SpriteBatch spriteBatch)
    {
        this.orthographicCamera.update();

        spriteBatch.setProjectionMatrix(this.orthographicCamera.combined);
    }

    public void damage(float amount)
    {
        if (this.health - amount <= 0.0f)
        {
            this.health = 0.0f;

            return;
        }

        this.health -= amount;
    }

    public void draw(SpriteBatch spriteBatch, boolean freeze, Level level, float delta)
    {
        this.orthographicCamera.position.y = this.y;

        if (Gdx.input.isKeyPressed(Input.Keys.W) && !freeze)
        {
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT))
            {
                this.jumping = true;

                if (this.power > 0.2f)
                {
                    long now = System.currentTimeMillis();

                    if (this.lastJetPackTime == 0)
                    {
                        this.lastJetPackTime = now;

                        this.jetPack.play(2.0f);
                    }

                    this.jetPackTime = now - this.lastJetPackTime;

                    this.lastJetPackTime = now;

                    if (this.currentAnimation != this.hoveringAnimation)
                    {
                        this.currentAnimation = this.hoveringAnimation;

                        this.stateTime = 0;
                    }

                    this.jetPackLight.setActive(true);

                    this.yVelocity += 1.0f;

                    if (this.yVelocity > 10.0f)
                    {
                        this.yVelocity = 10.0f;
                    }

                    this.power -= 0.2f;

                    if (this.jetPackTime >= 2000)
                    {
                        this.lastJetPackTime = 0;
                        this.jetPackTime = 0;
                    }
                }
                else
                {
                    this.jetPackLight.setActive(false);

                    this.jetPackTime = 0;
                    this.lastJetPackTime = 0;
                }
            }
            else
            {
                this.jetPackLight.setActive(false);

                this.jetPack.stop();

                this.jetPackTime = 0;
                this.lastJetPackTime = 0;

                if (!this.jumping && !this.falling)
                {
                    this.jumping = true;

                    this.yVelocity = 12.5f;
                }
            }
        }
        else
        {
            this.jetPackLight.setActive(false);

            this.jetPack.stop();

            this.jetPackTime = 0;
            this.lastJetPackTime = 0;
        }

        float moveX = 0.0f;
        float moveY = 0.0f;

        if (Gdx.input.isKeyPressed(Input.Keys.D) && !freeze)
        {
            if (!this.jumping)
            {
                if (this.currentAnimation != this.walkingAnimation)
                {
                    this.currentAnimation = this.walkingAnimation;

                    this.stateTime = 0;
                }
            }

            moveX += SPEED * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.A) && !freeze)
        {
            if (!this.jumping)
            {
                if (this.currentAnimation != this.walkingAnimation)
                {
                    this.currentAnimation = this.walkingAnimation;

                    this.stateTime = 0;
                }
            }

            moveX -= SPEED * delta;
        }

        if (this.jumping)
        {
            this.currentFrame = this.jumpingTextureRegion;

            this.yVelocity -= Level.GRAVITY * delta;

            moveY = this.yVelocity;

            this.falling = true;
        }
        else if (level.isAir(Math.round((this.x + moveX) / (float) level.scale), (int) ((this.y - SPEED * delta) / level.scale)) && !freeze)
        {
            this.yVelocity -= Level.GRAVITY * delta;

            moveY = this.yVelocity;

            this.falling = true;
        }

        float posx = this.x + moveX;
        float posy = this.y + moveY;

        posx /= level.scale;
        posy /= level.scale;

        if (moveX < 0.0f)
        {
            this.facingRight = false;

            if (level.isDirt((int) Math.floor(posx), (int) posy) ||
                level.isDirt((int) Math.floor(posx), (int) posy + 1))
            {
                moveX = 0.0f;
            }
        }
        else if (moveX > 0.0f)
        {
            this.facingRight = true;

            if (level.isDirt((int) Math.ceil(posx), (int) posy) ||
                level.isDirt((int) Math.ceil(posx), (int) posy + 1))
            {
                moveX = 0.0f;
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
                this.falling = false;
            }
        }
        else if (moveY > 0.0f)
        {
            if (level.isDirt(Math.round(posx), (int) posy + 2))
            {
                this.yVelocity = 0.0f;

                this.y = (int) (posy) * level.scale;

                moveY = 0.0f;
            }
        }

        if (level.isPower(Math.round(posx), (int) posy - 1))
        {
            this.foundPowerSource = true;
        }

        if (moveX == 0.0f &&
            moveY == 0.0f)
        {
            if (this.currentAnimation != this.standingAnimation)
            {
                this.currentAnimation = this.standingAnimation;

                this.stateTime = 0.0f;
            }
        }

        if (!freeze)
        {
            this.x += moveX;
            this.y += moveY;
        }

        this.orthographicCamera.position.x = this.x + this.xScale / 2.0f;

        this.tmpVector.set(Gdx.input.getX(), Gdx.input.getY(), 0.0f);

        this.orthographicCamera.unproject(this.tmpVector);

        this.rectangle.setX(this.x);
        this.rectangle.setY(this.y);

        if (!freeze)
        {
            this.angle = (float) -Math.toDegrees(Math.atan2(this.tmpVector.x - (this.x + this.xScale / 2.0f), this.tmpVector.y - (this.y + this.yScale / 2.0f))) + 90.0f;
        }


        final float max = 20.0f;

        if (!this.jumping || this.hoveringAnimation == this.currentAnimation)
        {
            this.currentFrame = this.currentAnimation.getKeyFrame(this.stateTime, true);
        }
        else
        {
            this.currentFrame = this.jumpingTextureRegion;
        }

        spriteBatch.draw(this.currentFrame.getTexture(), this.x, this.y, 0.0f, 0.0f, this.xScale, this.yScale, 1.0f, 1.0f, 0.0f, this.currentFrame.getRegionX(), this.currentFrame.getRegionY(), this.currentFrame.getRegionWidth(), this.currentFrame.getRegionHeight(), !this.facingRight, false);

        if (!freeze)
        {
            if (this.tmpVector.x > this.x + this.xScale / 2.0f)
            {
                if (this.angle > max)
                {
                    this.angle = max;
                }
                else if (this.angle < -max)
                {
                    this.angle = -max;
                }

                spriteBatch.draw(this.blasterTextureRegion.getTexture(), this.x + this.xScale / 2.0f - this.xScale / 3.0f, this.y + 30.0f, 0.0f, this.yScale / 4.0f, this.xScale, this.yScale / 2.0f, 1.0f, 1.0f, this.angle, this.blasterTextureRegion.getRegionX(), this.blastTextureRegion.getRegionY(), this.blasterTextureRegion.getRegionWidth(), this.blasterTextureRegion.getRegionHeight(), false, false);
            }
            else
            {
                if (this.angle > max + 180)
                {
                    this.angle = max + 180;
                }
                else if (this.angle < 180 - max)
                {
                    this.angle = 180 - max;
                }

                spriteBatch.draw(this.blasterTextureRegion.getTexture(), this.x + this.xScale / 2.0f + this.xScale / 3.0f, this.y + 30.0f, 0.0f, this.yScale / 4.0f, this.xScale, this.yScale / 2.0f, 1.0f, 1.0f, this.angle, this.blasterTextureRegion.getRegionX(), this.blastTextureRegion.getRegionY(), this.blasterTextureRegion.getRegionWidth(), this.blasterTextureRegion.getRegionHeight(), false, true);
            }
        }

        float x = (float) Math.cos(Math.toRadians(this.angle)) * this.xScale * 0.75f;
        float y = (float) Math.sin(Math.toRadians(this.angle)) * this.yScale * 0.75f;

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && !freeze)
        {
            long now = System.currentTimeMillis();

            if (this.lastTime == 0)
            {
                this.lastTime = now;
            }

            this.blastInterval += now - this.lastTime;

            this.lastTime = now;

            if (this.blastInterval >= this.MS_BETWEEN_PER_BLAST)
            {
                if (this.power > 4.0f)
                {
                    this.projectiles.add(new Projectile(new Rectangle(x + this.x + this.xScale / 2.0f - this.xScale / 4.0f / 2.0f, y + this.y + this.yScale / 4.0f - this.yScale / 8.0f / 2.0f + 35.0f - this.yScale / this.currentFrame.getRegionHeight() * 1, this.xScale / 8.0f, this.yScale / 8.0f / 2.0f),
                                                        new Vector2(x, y),
                                                        new PointLight(level.rayHandler, 128, new Color(1.0f, 0.0f, 0.0f, 1.0f), 40, x + this.x + this.xScale / 2.0f - this.xScale / 8.0f / 2.0f, y + this.y + this.yScale / 4.0f - this.yScale / 8.0f / 2.0f)));

                    this.blast.play();

                    this.power -= 4.0f;

                    this.blastInterval = 0;
                    this.lastTime = 0;
                }
            }
        }

        for (Projectile projectile : this.projectiles)
        {
            boolean collided = false;

            projectile.rectangle.x += projectile.direction.x * delta * 20;
            projectile.rectangle.y += projectile.direction.y * delta * 20;

            projectile.pointLight.setPosition(projectile.rectangle.x, projectile.rectangle.y);

            spriteBatch.draw(this.blastTextureRegion, projectile.rectangle.x, projectile.rectangle.y, projectile.rectangle.width, projectile.rectangle.height);

            for (int xs = 0; xs < level.array.length; xs++)
            {
                if (collided)
                {
                    break;
                }

                for (int ys = 0; ys < level.array[0].length; ys++)
                {
                    if (level.isDirt(xs, ys))
                    {
                        if (projectile.rectangle.overlaps(this.tmpRectangle.set(xs * level.scale, ys * level.scale, level.scale, level.scale)))
                        {
                            level.rayHandler.lightList.removeValue(projectile.pointLight, true);

                            this.blast.play();

                            this.projectiles.removeValue(projectile, true);

                            collided = true;

                            break;
                        }
                    }
                }
            }
        }

        this.coneLight.setPosition(x + this.x + this.xScale / 2.0f - this.xScale / 4.0f / 2.0f, y + this.y + this.yScale / 4.0f - this.yScale / 8.0f / 2.0f + 35.0f);
        this.coneLight.setDirection(this.angle);

        this.jetPackLight.setPosition(this.x + this.xScale / 2.0f + (this.facingRight ? -this.xScale / 3.0f : this.xScale / 3.0f), this.y + this.yScale / 4.0f + 20.0f);

        this.eye.setPosition(this.x + this.xScale / 2.0f, this.y + this.yScale / this.currentFrame.getRegionHeight() * 15);

        this.blaster.setPosition(x + this.x + this.xScale / 2.0f - this.xScale / 4.0f / 2.0f, y + this.y + this.yScale / 4.0f - this.yScale / 8.0f / 2.0f + 35.0f);

        if (!freeze)
        {
            this.power += 0.1f;
            this.stateTime += delta;
        }

        if (this.power > 100.0f)
        {
            this.power = 100.0f;
        }

        if (this.stateTime >= this.currentAnimation.animationDuration)
        {
            this.stateTime = 0.0f;
        }
    }

    public boolean isDead()
    {
        return this.health == 0.0f;
    }

    public boolean foundPowerSource()
    {
        return this.foundPowerSource;
    }

    public void dispose(RayHandler rayHandler)
    {
        rayHandler.lightList.removeValue(this.eye, true);
        rayHandler.lightList.removeValue(this.blaster, true);

        this.blast.stop();
        this.explosion.stop();
        this.jetPack.stop();

        this.jetPack.dispose();
        this.blast.dispose();
        this.explosion.dispose();
    }
}
