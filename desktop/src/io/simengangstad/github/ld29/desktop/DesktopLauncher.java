package io.simengangstad.github.ld29.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.simengangstad.github.ld29.screens.LD29;

public class DesktopLauncher
{
    public static void main(String[] arg)
    {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

        config.resizable = false;
        config.width = 840;
        config.height = 540;

        new LwjglApplication(new LD29(), config);
    }
}
