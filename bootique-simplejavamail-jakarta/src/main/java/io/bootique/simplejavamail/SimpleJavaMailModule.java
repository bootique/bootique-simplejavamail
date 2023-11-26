package io.bootique.simplejavamail;

import io.bootique.BQModuleProvider;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.BQModule;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;

import javax.inject.Singleton;

/**
 * @since 2.0
 */
public class SimpleJavaMailModule implements BQModule, BQModuleProvider {

    private static final String CONFIG_PREFIX = "simplejavamail";

    @Override
    public ModuleCrate moduleCrate() {
        return ModuleCrate.of(this).provider(this).config(CONFIG_PREFIX, MailersFactory.class).build();
    }

    @Override
    public void configure(Binder binder) {
    }

    @Singleton
    @Provides
    Mailers provideMailers(ConfigurationFactory configurationFactory, ShutdownManager shutdownManager) {
        return configurationFactory.config(MailersFactory.class, CONFIG_PREFIX).createMailers(shutdownManager);
    }
}
