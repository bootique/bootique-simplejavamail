package io.bootique.simplejavamail;

import io.bootique.BaseModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;

import javax.inject.Singleton;

/**
 * @since 2.0
 */
public class SimpleJavaMailModule extends BaseModule {

    @Override
    public void configure(Binder binder) {
        // TODO: configure services
    }

    @Singleton
    @Provides
    Mailers provideMailers(ConfigurationFactory configurationFactory) {
        return config(MailersFactory.class, configurationFactory).createMailers();
    }
}
