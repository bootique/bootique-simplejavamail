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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 2.0
 */
@BQConfig("Configuration of mailers collection")
public class MailersFactory {

    private Map<String, MailerFactory> mailers;

    public Mailers createMailers() {
        Map<String, Mailer> resolvedMailers = resolveMailers();
        Mailer defaultMailer = resolveDefaultMailer(resolvedMailers);
        return new DefaultMailers(resolvedMailers, defaultMailer);
    }

    @BQConfigProperty("Named mailers configuration")
    public void setMailers(Map<String, MailerFactory> mailers) {
        this.mailers = mailers;
    }

    protected Mailer resolveDefaultMailer(Map<String, Mailer> resolvedMailers) {
        switch (resolvedMailers.size()) {
            case 0:
                // provide an implicitly-configured default mailer
                return new MailerFactory().createMailer();
            case 1:
                return resolvedMailers.values().iterator().next();
            default:
                return null;
        }
    }

    protected Map<String, Mailer> resolveMailers() {

        if (mailers == null || mailers.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Mailer> resolved = new HashMap<>();
        mailers.forEach((k, v) -> resolved.put(k, v.createMailer()));
        return resolved;
    }
}
