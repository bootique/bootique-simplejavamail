package io.bootique.simplejavamail;

import io.bootique.test.junit.BQModuleProviderChecker;
import org.junit.Test;

public class SimpleJavaMailModuleProviderTest {

    @Test
    public void testAutoLoading() {
        BQModuleProviderChecker.testAutoLoadable(SimpleJavaMailModuleProvider.class);
    }
}
