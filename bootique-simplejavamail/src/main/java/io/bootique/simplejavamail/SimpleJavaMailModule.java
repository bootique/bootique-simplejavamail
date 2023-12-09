package io.bootique.simplejavamail;

import io.bootique.BQModule;
import io.bootique.ModuleCrate;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;

import javax.inject.Singleton;

/**
 * @since 2.0
 * @deprecated in favor of the Jakarta flavor
 */
@Deprecated(since = "3.0", forRemoval = true)
public class SimpleJavaMailModule implements BQModule {

    private static final String CONFIG_PREFIX = "simplejavamail";

    @Override
    public ModuleCrate crate() {
        return ModuleCrate.of(this)
                .description("Deprecated, can be replaced with 'bootique-simplejavamail-jakarta'.")
                .config(CONFIG_PREFIX, MailersFactory.class)
                .build();
    }

    @Override
    public void configure(Binder binder) {
    }

    @Singleton
    @Provides
    Mailers provideMailers(ConfigurationFactory configFactory, ShutdownManager shutdownManager) {
        return configFactory.config(MailersFactory.class, CONFIG_PREFIX).createMailers(shutdownManager);
    }
}
