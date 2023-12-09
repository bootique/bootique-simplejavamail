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

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.internal.batchsupport.LifecycleDelegatingTransport;
import org.simplejavamail.api.mailer.CustomMailer;
import org.simplejavamail.api.mailer.config.OperationalConfig;
import org.simplejavamail.internal.moduleloader.ModuleLoader;
import org.simplejavamail.internal.modules.BatchModule;
import org.simplejavamail.mailer.internal.util.TransportConnectionHelper;
import org.simplejavamail.mailer.internal.util.TransportRunner;
import org.slf4j.Logger;

import java.util.Objects;
import java.util.UUID;

import static org.slf4j.LoggerFactory.getLogger;

public class MailerWithOverriddenRecipients implements CustomMailer {

    static final Logger LOGGER = getLogger(MailerWithOverriddenRecipients.class);

    private final Address[] overriddenRecipients;

    public MailerWithOverriddenRecipients(Address[] overriddenRecipients) {
        this.overriddenRecipients = Objects.requireNonNull(overriddenRecipients);
    }

    @Override
    public void testConnection(OperationalConfig operationalConfig, Session session) {
        try {
            // doing the same as TestConnectionClosure
            TransportRunner.connect(operationalConfig.getClusterKey(), session);
        } catch (MessagingException e) {
            throw new MailerWithOverriddenRecipientsException("Was unable to connect to SMTP server", e);
        }
    }

    @Override
    public void sendMessage(OperationalConfig operationalConfig, Session session, Email email, MimeMessage message) {

        try {
            // doing something similar to what SendMailClosure does, except override the recipients
            sendMessage(operationalConfig.getClusterKey(), session, message);
        } catch (MessagingException e) {
            LOGGER.error("Failed to send email:\n{}", email.getId());
            LOGGER.trace("{}", email);
            throw new MailerWithOverriddenRecipientsException("Third party error", e);
        }
    }

    // loosely copied from SJM TransportRunner
    private void sendMessage(UUID clusterKey, Session session, MimeMessage message) throws MessagingException {
        TransportRunner.TransportRunnable runnable = (transport, actualSessionUsed) -> {
            transport.sendMessage(message, overriddenRecipients);
            LOGGER.trace("...email sent");
        };

        if (ModuleLoader.batchModuleAvailable()) {
            sendUsingConnectionPool(ModuleLoader.loadBatchModule(), clusterKey, session, false, runnable);
        } else {
            try (Transport transport = session.getTransport()) {
                TransportConnectionHelper.connectTransport(transport, session);
                runnable.run(transport, session);
            } finally {
                LOGGER.trace("closing transport");
            }
        }
    }

    // copied from SJM TransportRunner
    private static void sendUsingConnectionPool(
            BatchModule batchModule,
            UUID clusterKey,
            Session session,
            boolean stickySession,
            TransportRunner.TransportRunnable runnable) throws MessagingException {

        LifecycleDelegatingTransport delegatingTransport = batchModule.acquireTransport(clusterKey, session, stickySession);
        try {
            runnable.run(delegatingTransport.getTransport(), delegatingTransport.getSessionUsedToObtainTransport());
        } catch (Throwable t) {
            // always make sure claimed resources are released
            delegatingTransport.signalTransportFailed();
            throw t;
        }
        delegatingTransport.signalTransportUsed();
    }

    // SJM throws MailerException that is not public, so the closest things we can do is our own subclass of MailException
    static class MailerWithOverriddenRecipientsException extends MailException {
        public MailerWithOverriddenRecipientsException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
