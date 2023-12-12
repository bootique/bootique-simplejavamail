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

import jakarta.mail.Session;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.*;
import org.simplejavamail.email.EmailBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * @since 3.0
 */
public class RecipientOverrideMailer implements Mailer {

    private final Mailer delegate;
    private final List<Recipient> recipientOverrides;

    public RecipientOverrideMailer(Mailer delegate, List<Recipient> recipientOverrides) {
        this.delegate = delegate;
        this.recipientOverrides = recipientOverrides;
    }

    private Email resolveEmail(Email email) {
        // if a user passed their own overrides, use them, otherwise force the preconfigured ones
        return email.getOverrideReceivers().isEmpty()
                ? EmailBuilder.copying(email).withOverrideReceivers(recipientOverrides).buildEmail()
                : email;
    }

    @Override
    public @NotNull CompletableFuture<Void> sendMail(Email email) {
        return delegate.sendMail(resolveEmail(email));
    }

    @Override
    public @NotNull CompletableFuture<Void> sendMail(Email email, boolean b) {
        return delegate.sendMail(resolveEmail(email), b);
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }

    @Override
    public Session getSession() {
        return delegate.getSession();
    }

    @Override
    public void testConnection() {
        delegate.testConnection();
    }

    @Override
    public @NotNull CompletableFuture<Void> testConnection(boolean b) {
        return delegate.testConnection(b);
    }

    @Override
    public boolean validate(Email email) throws MailException {
        return delegate.validate(email);
    }

    @Override
    public Future<?> shutdownConnectionPool() {
        return delegate.shutdownConnectionPool();
    }

    @Override
    public @Nullable ServerConfig getServerConfig() {
        return delegate.getServerConfig();
    }

    @Override
    public @Nullable TransportStrategy getTransportStrategy() {
        return delegate.getTransportStrategy();
    }

    @Override
    public @NotNull ProxyConfig getProxyConfig() {
        return delegate.getProxyConfig();
    }

    @Override
    public @NotNull OperationalConfig getOperationalConfig() {
        return delegate.getOperationalConfig();
    }

    @Override
    public @NotNull EmailGovernance getEmailGovernance() {
        return delegate.getEmailGovernance();
    }
}
