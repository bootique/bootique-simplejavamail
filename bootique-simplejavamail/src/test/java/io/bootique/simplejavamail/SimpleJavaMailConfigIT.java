/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.bootique.simplejavamail;

import io.bootique.BQCoreModule;
import io.bootique.BQRuntime;
import io.bootique.test.junit5.BQTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Collections;
import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleJavaMailConfigIT {

    @RegisterExtension
    public BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    @Test
    public void testMailer_NoConfig() {
        BQRuntime runtime = testFactory.app().createRuntime();
        Mailers mailers = runtime.getInstance(Mailers.class);

        assertTrue(mailers.getMailerNames().isEmpty());
        assertNotNull(mailers.getDefaultMailer());
    }

    @Test
    public void testMailer_SingleConfig() {
        BQRuntime runtime = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpServer", "example.org"))
                .createRuntime();

        Mailers mailers = runtime.getInstance(Mailers.class);

        assertEquals(Collections.singleton("x"), new HashSet<>(mailers.getMailerNames()));
        assertNotNull(mailers.getDefaultMailer());
        assertNotNull(mailers.getMailer("x"));
        assertSame(mailers.getDefaultMailer(), mailers.getMailer("x"));
    }

    @Test
    public void testMailer_MultiConfig() {
        BQRuntime runtime = testFactory.app()
                .module(b -> BQCoreModule.extend(b)
                        .setProperty("bq.simplejavamail.mailers.x.smtpServer", "example.org")
                        .setProperty("bq.simplejavamail.mailers.y.smtpServer", "example.org"))
                .createRuntime();

        Mailers mailers = runtime.getInstance(Mailers.class);

        assertEquals(new HashSet<>(asList("x", "y")), new HashSet<>(mailers.getMailerNames()));
        assertNotNull(mailers.getMailer("x"));
        assertNotNull(mailers.getMailer("y"));
        assertThrows(IllegalStateException.class, () -> mailers.getDefaultMailer());
    }
}
