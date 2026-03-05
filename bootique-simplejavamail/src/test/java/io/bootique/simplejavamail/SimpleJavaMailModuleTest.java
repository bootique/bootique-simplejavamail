package io.bootique.simplejavamail;

import io.bootique.junit.BQModuleTester;
import org.junit.jupiter.api.Test;

public class SimpleJavaMailModuleTest {

    @Test
    public void check() {
        BQModuleTester.of(SimpleJavaMailModule.class).testAutoLoadable().testConfig();
    }
}
