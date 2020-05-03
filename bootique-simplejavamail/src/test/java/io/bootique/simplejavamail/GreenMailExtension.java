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

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailProxy;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

// Borrowed  under the terms of the Apache license from
// https://github.com/greenmail-mail-test/greenmail/blob/master/greenmail-junit5/src/main/java/com/icegreen/greenmail/junit5/GreenMailExtension.java

// TODO: Once Greenmail 1.6 is released, we can replace this with upstream implementation

public class GreenMailExtension extends GreenMailProxy implements BeforeEachCallback, AfterEachCallback {

    private final ServerSetup[] serverSetups;
    private GreenMail greenMail;

    /**
     * Initialize with single server setups.
     *
     * @param serverSetup Setup to use
     */
    public GreenMailExtension(final ServerSetup serverSetup) {
        this.serverSetups = new ServerSetup[]{serverSetup};
    }

    /**
     * Initialize with all server setups.
     */
    public GreenMailExtension() {
        this(ServerSetupTest.ALL);
    }

    /**
     * Initialize with multiple server setups.
     *
     * @param serverSetups All setups to use
     */
    public GreenMailExtension(final ServerSetup[] serverSetups) {
        this.serverSetups = serverSetups;
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        greenMail = new GreenMail(serverSetups);
        start();
    }

    @Override
    public void afterEach(final ExtensionContext context) {
        stop();
    }

    @Override
    protected GreenMail getGreenMail() {
        return greenMail;
    }

    @Override
    public GreenMailExtension withConfiguration(final GreenMailConfiguration config) {
        super.withConfiguration(config);
        return this;
    }
}
