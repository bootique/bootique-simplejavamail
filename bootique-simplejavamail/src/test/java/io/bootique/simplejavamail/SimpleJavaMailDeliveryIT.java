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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleJavaMailDeliveryIT {

    @RegisterExtension
    public static final GreenMailExtension mailboxManager = new GreenMailExtension(new ServerSetup(5025, null, ServerSetup.PROTOCOL_SMTP));

    @RegisterExtension
    public BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    @Test
    @DisplayName("Delivery with (almost) default config")
    public void testSendMail() throws MessagingException {

        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "5025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        MimeMessage received = sendSync(email, mailers.getDefaultMailer());
        assertEquals("y@example.org", received.getFrom()[0].toString());
        assertEquals("x@example.org", received.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("test subject", received.getSubject());
        assertEquals("test body", GreenMailUtil.getBody(received));
    }

    @Test
    @DisplayName("Async delivery")
    public void testSendMail_Async() throws MessagingException {

        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "5025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        MimeMessage received = sendAsync(email, mailers.getDefaultMailer());
        assertEquals("y@example.org", received.getFrom()[0].toString());
        assertEquals("x@example.org", received.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("test subject", received.getSubject());
        assertEquals("test body", GreenMailUtil.getBody(received));
    }

    @Test
    @DisplayName("'validateEmails' on")
    public void testSendMail_validateEmailsOn() throws MessagingException {
        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "5025"))
                // 'true' is the default
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.validateEmails", "true"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("_invalid_")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        assertThrows(MailException.class, () -> mailers.getDefaultMailer().sendMail(email));
    }

    @Test
    @DisplayName("'validateEmails' off")
    public void testSendMail_validateEmailsOff() throws MessagingException {
        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "5025"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.validateEmails", "false"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("_invalid_")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        MimeMessage received = sendSync(email, mailers.getDefaultMailer());

        assertEquals("_invalid_", received.getFrom()[0].toString());
        assertEquals("x@example.org", received.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("test subject", received.getSubject());
        assertEquals("test body", GreenMailUtil.getBody(received));
    }

    @Test
    @DisplayName("'disabled'")
    public void testSendMail_Disabled() throws MessagingException {
        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "true"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "5025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        mailers.getDefaultMailer().sendMail(email, false);
        assertFalse(mailboxManager.waitForIncomingEmail(500, 1), "Email unexpected in 'disabled' mode");
    }

    @Test
    @DisplayName("'disabled' on by default")
    public void testSendMail_Disabled_Default() throws MessagingException {
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

        mailers.getDefaultMailer().sendMail(email, false);
        assertFalse(mailboxManager.waitForIncomingEmail(500, 1), "Email unexpected in 'disabled' mode");
    }


    private MimeMessage sendSync(Email email, Mailer mailer) {
        mailer.sendMail(email, false);

        // TODO: oddly enough with the addition of the batch module, sync delivery still requires waiting on the
        //  Greenmail end.. Is it truly sync?
        assertTrue(mailboxManager.waitForIncomingEmail(500, 1));
        MimeMessage[] received = mailboxManager.getGreenMail().getReceivedMessages();
        assertEquals(1, received.length);
        return received[0];
    }

    private MimeMessage sendAsync(Email email, Mailer mailer) {
        mailer.sendMail(email, true);
        assertTrue(mailboxManager.waitForIncomingEmail(500, 1));
        MimeMessage[] received = mailboxManager.getGreenMail().getReceivedMessages();
        assertEquals(1, received.length);
        return received[0];
    }
}
