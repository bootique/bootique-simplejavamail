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
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.internal.MailerRegularBuilderImpl;

/**
 * @since 2.0
 */
@BQConfig
public class MailerFactory {

    private String smtpServer;
    private Integer smtpPort;

    public Mailer createMailer() {
        MailerRegularBuilderImpl builder = MailerBuilder.withSMTPServer(resolveSmtpServer(), resolveSmtpPort());

        return builder.buildMailer();
    }

    @BQConfigProperty("SMTP server used for mail delivery. '127.0.0.1' by default")
    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    @BQConfigProperty("SMTP port. '25' by default")
    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    protected String resolveSmtpServer() {
        return this.smtpServer != null ? this.smtpServer : "127.0.0.1";
    }

    protected int resolveSmtpPort() {
        return this.smtpPort != null ? this.smtpPort : 25;
    }
}
