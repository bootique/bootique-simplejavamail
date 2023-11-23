package io.bootique.simplejavamail;

import io.bootique.junit5.BQModuleProviderChecker;
import org.junit.jupiter.api.Test;

public class SimpleJavaMailModuleTest {

    @Test
    public void autoLoading() {
        BQModuleProviderChecker.testAutoLoadable(SimpleJavaMailModule.class);
    }
}
