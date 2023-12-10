package io.bootique.simplejavamail;

import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;

import javax.inject.Singleton;

/**
 * @since 2.0
 */
public class SimpleJavaMailModule implements BQModule {

    private static final String CONFIG_PREFIX = "simplejavamail";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this).config(CONFIG_PREFIX, MailersFactory.class).build();
    }

    @Override
    public void configure(Binder binder) {
    }

    @Singleton
    @Provides
    Mailers provideMailers(ConfigurationFactory configurationFactory) {
        return configurationFactory.config(MailersFactory.class, CONFIG_PREFIX).create();
    }
}
