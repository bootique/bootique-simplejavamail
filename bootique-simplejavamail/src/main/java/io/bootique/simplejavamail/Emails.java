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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Arrays;
import java.util.Objects;

/**
 * A configuration value object that represents a comma-separated list of emails following RFC822 syntax.
 *
 * @since 3.0
 */
public class Emails {

    private final InternetAddress[] emails;

    public Emails(String value) {
        this.emails = parse(value);
    }

    static InternetAddress[] parse(String value) {
        Objects.requireNonNull(value, "Null 'value' argument");

        if (value.isEmpty()) {
            throw new IllegalArgumentException("Empty 'value' argument");
        }

        try {
            return value != null ? InternetAddress.parse(value) : null;
        } catch (AddressException e) {
            throw new IllegalStateException(e.getLocalizedMessage(), e);
        }
    }

    public InternetAddress[] getEmails() {
        return emails;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Emails emails1 = (Emails) o;
        return Arrays.equals(emails, emails1.emails);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(emails);
    }
}
