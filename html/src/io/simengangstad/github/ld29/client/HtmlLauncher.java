package io.simengangstad.github.ld29.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import io.simengangstad.github.ld29.screens.LD29;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(840, 540);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new LD29();
        }
}