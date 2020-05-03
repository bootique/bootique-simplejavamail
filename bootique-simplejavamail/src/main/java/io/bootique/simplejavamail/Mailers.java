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

/**
 * A holder of one or more named {@link org.simplejavamail.api.mailer.Mailer} mailers.
 *
 * @since 2.0
 */
public interface Mailers {

    Mailer getMailer(String name);

    /**
     * Returns default mailer or throws an exception if it is not configured. If no explicit mailers configuration
     * was provided when this object was created, the default mailer is created implicitly and wil use "localhost:25"
     * for delivery. If a single named mailer was configured, it is also assumed to be the default mailer. If more
     * than one mailer was configured, there's no default mailer, and mailers must be accessed by name using
     * {@link #getMailer(String)}.
     *
     * @return default mailer
     */
    Mailer getDefaultMailer();

    Collection<String> getMailerNames();
}
