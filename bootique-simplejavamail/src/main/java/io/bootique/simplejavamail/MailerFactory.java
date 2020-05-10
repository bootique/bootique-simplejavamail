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

import io.bootique.annotation.BQConfig;
import io.bootique.annotation.BQConfigProperty;
import io.bootique.shutdown.ShutdownManager;
import io.bootique.value.Duration;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.internal.MailerRegularBuilderImpl;

import java.util.Collections;
import java.util.Map;

/**
 * @since 2.0
 */
@BQConfig
public class MailerFactory {

    private String smtpServer;
    private Integer smtpPort;
    private String username;
    private String password;
    private Duration sessionTimeout;

    private Integer connectionPoolCoreSize;
    private Integer connectionPoolMaxSize;
    private Duration connectionPoolClaimTimeout;
    private Duration connectionPoolExpireAfter;

    private Integer threadPoolSize;
    private Duration threadPoolKeepAliveTime;

    private TransportStrategy transportStrategy;
    private Boolean validateEmails;
    private Map<String, String> javamailProperties;

    // TODO: proxy settings, etc.

    public Mailer createMailer(boolean disabled, ShutdownManager shutdownManager) {

        MailerRegularBuilderImpl builder = MailerBuilder
                .withSMTPServer(resolveSmtpServer(), resolveSmtpPort())
                .withSMTPServerUsername(username)
                .withSMTPServerPassword(password)
                .withSessionTimeout(resolveSessionTimeout())
                .withTransportStrategy(resolveTransportStrategy())
                .withProperties(resolveJavamailProperties())

                .withThreadPoolSize(resolveThreadPoolSize())
                .withThreadPoolKeepAliveTime(resolveThreadPoolKeepAliveTimeMs())

                // configure connection pool
                .withConnectionPoolExpireAfterMillis(resolveConnectionPoolExpireAfter())
                .withConnectionPoolClaimTimeoutMillis(resolveConnectionPoolClaimTimeout())
                .withConnectionPoolCoreSize(resolveConnectionPoolCoreSize())
                .withConnectionPoolMaxSize(resolveConnectionPoolMaxSize())

                .withTransportModeLoggingOnly(disabled);

        if (!resolveValidateEmails()) {
            builder.clearEmailAddressCriteria();
        }

        Mailer mailer = builder.buildMailer();
        shutdownManager.addShutdownHook(() -> mailer.shutdownConnectionPool());
        return mailer;
    }

    @BQConfigProperty("SMTP server used for mail delivery. '127.0.0.1' by default")
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    @BQConfigProperty("SMTP port. '25' by default")
    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    @BQConfigProperty
    public void setUsername(String username) {
        this.username = username;
    }

    @BQConfigProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @BQConfigProperty
    public void setSessionTimeout(Duration sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }


    @BQConfigProperty
    public void setThreadPoolSize(Integer threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    @BQConfigProperty
    public void setThreadPoolKeepAliveTime(Duration threadPoolKeepAliveTime) {
        this.threadPoolKeepAliveTime = threadPoolKeepAliveTime;
    }


    @BQConfigProperty
    public void setConnectionPoolCoreSize(Integer connectionPoolCoreSize) {
        this.connectionPoolCoreSize = connectionPoolCoreSize;
    }

    @BQConfigProperty
    public void setConnectionPoolMaxSize(Integer connectionPoolMaxSize) {
        this.connectionPoolMaxSize = connectionPoolMaxSize;
    }

    @BQConfigProperty
    public void setConnectionPoolExpireAfter(Duration connectionPoolExpireAfter) {
        this.connectionPoolExpireAfter = connectionPoolExpireAfter;
    }

    @BQConfigProperty
    public void setConnectionPoolClaimTimeout(Duration connectionPoolClaimTimeout) {
        this.connectionPoolClaimTimeout = connectionPoolClaimTimeout;
    }


    @BQConfigProperty("One of 'SMTP' (default), 'SMTP_TLS', 'SMTPS'")
    public void setTransportStrategy(TransportStrategy transportStrategy) {
        this.transportStrategy = transportStrategy;
    }

    @BQConfigProperty("Should email addresses be validated. 'true' is the default")
    public void setValidateEmails(Boolean validateEmails) {
        this.validateEmails = validateEmails;
    }

    @BQConfigProperty("Optional map of properties passed directly to the underlying JavaMail engine")
    public void setJavamailProperties(Map<String, String> javamailProperties) {
        this.javamailProperties = javamailProperties;
    }

    protected String resolveSmtpServer() {
        return this.smtpServer != null ? this.smtpServer : "127.0.0.1";
    }

    protected int resolveSmtpPort() {
        return this.smtpPort != null ? this.smtpPort : 25;
    }

    protected int resolveSessionTimeout() {
        return sessionTimeout != null ? (int) sessionTimeout.getDuration().toMillis() : 60_000;
    }

    protected TransportStrategy resolveTransportStrategy() {
        return this.transportStrategy != null ? this.transportStrategy : TransportStrategy.SMTP;
    }

    protected boolean resolveValidateEmails() {
        return this.validateEmails != null ? validateEmails : true;
    }

    protected int resolveConnectionPoolExpireAfter() {
        return connectionPoolExpireAfter != null ? (int) connectionPoolExpireAfter.getDuration().toMillis() : 5_000;
    }

    protected int resolveConnectionPoolClaimTimeout() {
        return connectionPoolClaimTimeout != null ? (int) connectionPoolClaimTimeout.getDuration().toMillis() : Integer.MAX_VALUE;
    }

    protected int resolveConnectionPoolCoreSize() {
        return connectionPoolCoreSize != null ? connectionPoolCoreSize : 0;
    }

    protected int resolveConnectionPoolMaxSize() {
        return connectionPoolMaxSize != null ? connectionPoolMaxSize : 4;
    }

    protected Map<String, String> resolveJavamailProperties() {
        return this.javamailProperties != null ? this.javamailProperties : Collections.emptyMap();
    }

    protected int resolveThreadPoolSize() {
        // default value in SJM is 10.. I suppose we don't need such a big pool for most cases
        return threadPoolSize != null ? threadPoolSize : 2;
    }

    protected int resolveThreadPoolKeepAliveTimeMs() {
        return threadPoolKeepAliveTime != null ? (int) threadPoolKeepAliveTime.getDuration().toMillis() : 10;
    }
}
