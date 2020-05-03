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

import org.simplejavamail.api.mailer.Mailer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @since 2.0
 */
public class DefaultMailers implements Mailers {

    private Map<String, Mailer> mailers;
    private Mailer defaultMailer;

    public DefaultMailers(Map<String, Mailer> mailers, Mailer defaultMailer) {
        this.mailers = mailers;
        this.defaultMailer = defaultMailer;
    }

    @Override
    public Collection<String> getMailerNames() {
        return Collections.unmodifiableCollection(mailers.keySet());
    }

    @Override
    public Mailer getMailer(String name) {
        Mailer mailer = mailers.get(name);
        if (mailer == null) {
            throw new IllegalArgumentException("Unknown mailer: " + name);
        }

        return mailer;
    }

    @Override
    public Mailer getDefaultMailer() {

        if (defaultMailer == null) {

            if (mailers.size() > 1) {
                throw new IllegalStateException("Can't determine default mailer, as more than one mailer exists. " +
                        "Use 'getMailer(String)' to obtain a named mailer instead");
            } else {
                // factory shouldn't let this happen
                throw new IllegalStateException("No default mailer configured");
            }
        }

        return defaultMailer;
    }
}
