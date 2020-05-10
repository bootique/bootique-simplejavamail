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
import org.simplejavamail.api.mailer.Mailer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.0
 */
@BQConfig("Configuration of mailers collection")
public class MailersFactory {

    private Map<String, MailerFactory> mailers;
    private Boolean disabled;

    public Mailers createMailers(ShutdownManager shutdownManager) {
        boolean disabled = resolveDisabled();
        Map<String, Mailer> resolvedMailers = resolveMailers(disabled, shutdownManager);
        Mailer defaultMailer = resolveDefaultMailer(resolvedMailers, disabled, shutdownManager);
        return new DefaultMailers(resolvedMailers, defaultMailer);
    }

    @BQConfigProperty("Named mailers configuration")
    public void setMailers(Map<String, MailerFactory> mailers) {
        this.mailers = mailers;
    }

    @BQConfigProperty("Turns mail delivery on or off for all mailers. By default the value is 'true' to prevent " +
            "unintended emails from going out in non-production environments. So it must be set to 'false' explicitly.")
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    protected Mailer resolveDefaultMailer(Map<String, Mailer> resolvedMailers, boolean disabled, ShutdownManager shutdownManager) {
        switch (resolvedMailers.size()) {
            case 0:
                // provide an implicitly-configured default mailer
                return new MailerFactory().createMailer(disabled, shutdownManager);
            case 1:
                return resolvedMailers.values().iterator().next();
            default:
                return null;
        }
    }

    protected Map<String, Mailer> resolveMailers(boolean disabled, ShutdownManager shutdownManager) {

        if (mailers == null || mailers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Mailer> resolved = new HashMap<>();
        mailers.forEach((k, v) -> resolved.put(k, v.createMailer(disabled, shutdownManager)));
        return resolved;
    }

    protected boolean resolveDisabled() {
        // by default all Mailers are disabled to prevent delivery in non-production environments
        return this.disabled != null ? this.disabled : true;
    }
}
