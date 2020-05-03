package io.bootique.simplejavamail;

import io.bootique.BaseModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Provides;

import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;

/**
 * @since 2.0
 */
public class SimpleJavaMailModule extends BaseModule {

    @Singleton
    @Provides
    Mailers provideMailers(ConfigurationFactory configurationFactory) {
        return config(MailersFactory.class, configurationFactory).createMailers();
    }

    @Override
    public Map<String, Type> configs() {
        return Collections.singletonMap(configPrefix, MailersFactory.class);
    }
}
