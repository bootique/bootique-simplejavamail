package io.bootique.simplejavamail;

import io.bootique.junit5.BQModuleProviderChecker;
import org.junit.jupiter.api.Test;

public class SimpleJavaMailModuleTest {

    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testAutoLoadable(SimpleJavaMailModule.class);
    }
}
