package io.bootique.simplejavamail;

import io.bootique.BQModuleProvider;
import io.bootique.ConfigModule;
import io.bootique.bootstrap.BuiltModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;

import javax.inject.Singleton;

/**
 * @since 2.0
 * @deprecated in favor of the Jakarta flavor
 */
@Deprecated(since = "3.0", forRemoval = true)
public class SimpleJavaMailModule extends ConfigModule implements BQModuleProvider {

    @Override
    public BuiltModule buildModule() {
        return BuiltModule.of(this)
                .provider(this)
                .description("Deprecated and can be replaced with 'bootique-simplejavamail-jakarta'.")
                .config(configPrefix, MailersFactory.class)
                .build();
    }

    @Singleton
    @Provides
    Mailers provideMailers(ConfigurationFactory configurationFactory, ShutdownManager shutdownManager) {
        return config(MailersFactory.class, configurationFactory).createMailers(shutdownManager);
    }
}
