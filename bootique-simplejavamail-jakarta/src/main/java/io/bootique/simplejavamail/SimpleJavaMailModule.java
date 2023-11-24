package io.bootique.simplejavamail;

import io.bootique.BQModuleProvider;
import io.bootique.ConfigModule;
import io.bootique.bootstrap.BuiltModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.shutdown.ShutdownManager;

import javax.inject.Singleton;

/**
 * @since 2.0
 */
public class SimpleJavaMailModule extends ConfigModule implements BQModuleProvider {

    @Override
    public BuiltModule buildModule() {
        return BuiltModule.of(this).provider(this).config(configPrefix, MailersFactory.class).build();
    }

    @Override
    public void configure(Binder binder) {
    }

    @Singleton
    @Provides
    Mailers provideMailers(ConfigurationFactory configurationFactory, ShutdownManager shutdownManager) {
        return config(MailersFactory.class, configurationFactory).createMailers(shutdownManager);
    }
}
