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
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class SimpleJavaMailConfigIT {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

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
        assertThrows(IllegalStateException.class, mailers::getDefaultMailer);
    }

    @Test
    @DisplayName("Explicit values to all supported config properties")
    public void testMailer_FullConfig() {
        BQRuntime runtime = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpServer", "example.org"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "11111"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.username", "un"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.password", "pwd"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.sessionTimeout", "10sec"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.threadPoolSize", "5"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.threadPoolKeepAliveTime", "11 sec"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.connectionPoolExpireAfter", "12 sec"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.connectionPoolClaimTimeout", "1 min"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.connectionPoolCoreSize", "2"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.connectionPoolMaxSize", "6"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.transportStrategy", "SMTPS"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.validateEmails", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.javamailProperties.x", "yz"))

                .createRuntime();

        Mailer mailer = runtime.getInstance(Mailers.class).getMailer("x");
        assertNotNull(mailer);

        assertFalse(mailer.getOperationalConfig().isTransportModeLoggingOnly());

        assertNotNull(mailer.getServerConfig());
        assertEquals("example.org", mailer.getServerConfig().getHost());
        assertEquals(11111, mailer.getServerConfig().getPort());
        assertEquals("un", mailer.getServerConfig().getUsername());
        assertEquals("pwd", mailer.getServerConfig().getPassword());
        assertEquals(10000, mailer.getOperationalConfig().getSessionTimeout());

        assertEquals(5, mailer.getOperationalConfig().getThreadPoolSize());
        assertEquals(11_000, mailer.getOperationalConfig().getThreadPoolKeepAliveTime());
        ThreadPoolExecutor executor = (ThreadPoolExecutor) mailer.getOperationalConfig().getExecutorService();
        assertNotNull(executor);
        assertEquals(5, executor.getMaximumPoolSize());
        assertEquals(11_000, executor.getKeepAliveTime(TimeUnit.MILLISECONDS));

        assertEquals(12_000, mailer.getOperationalConfig().getConnectionPoolExpireAfterMillis());
        assertEquals(60_000, mailer.getOperationalConfig().getConnectionPoolClaimTimeoutMillis());
        assertEquals(2, mailer.getOperationalConfig().getConnectionPoolCoreSize());
        assertEquals(6, mailer.getOperationalConfig().getConnectionPoolMaxSize());

        assertEquals(TransportStrategy.SMTPS, mailer.getTransportStrategy());
        assertTrue(mailer.getEmailGovernance().getEmailAddressCriteria().isEmpty());

        assertEquals("yz", mailer.getSession().getProperty("x"));
    }

}
