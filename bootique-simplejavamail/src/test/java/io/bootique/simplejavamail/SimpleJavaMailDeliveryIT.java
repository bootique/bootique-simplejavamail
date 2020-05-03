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

import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import io.bootique.BQCoreModule;
import io.bootique.test.junit5.BQTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleJavaMailDeliveryIT {

    @RegisterExtension
    public static final GreenMailExtension mailboxManager = new GreenMailExtension(new ServerSetup(5025, null, ServerSetup.PROTOCOL_SMTP));

    @RegisterExtension
    public BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    @Test
    public void testSendMail() throws MessagingException {

        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "5025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        mailers.getDefaultMailer().sendMail(email);

        MimeMessage[] received = mailboxManager.getGreenMail().getReceivedMessages();
        assertEquals(1, received.length);
        assertEquals("y@example.org", received[0].getFrom()[0].toString());
        assertEquals("x@example.org", received[0].getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("test subject", received[0].getSubject());
        assertEquals("test body", GreenMailUtil.getBody(received[0]));
    }
}
