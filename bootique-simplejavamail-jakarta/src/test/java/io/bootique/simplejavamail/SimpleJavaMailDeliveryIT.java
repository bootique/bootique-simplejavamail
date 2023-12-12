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

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import io.bootique.BQCoreModule;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.simplejavamail.MailException;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.email.EmailBuilder;

import static org.junit.jupiter.api.Assertions.*;

@BQTest
public class SimpleJavaMailDeliveryIT {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP_IMAP);

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    @Test
    @DisplayName("Delivery with (almost) default config")
    public void sendMail() throws MessagingException {

        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
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
    @Disabled("Temporary disabled as it fails in some environments")
    @DisplayName("Async delivery")
    public void sendMail_Async() throws MessagingException {

        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
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
    public void sendMail_validateEmailsOn() {
        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
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
    public void sendMail_validateEmailsOff() throws MessagingException {
        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
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
    public void sendMail_Disabled() {
        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "true"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        mailers.getDefaultMailer().sendMail(email, false);
        assertFalse(greenMail.waitForIncomingEmail(500, 1), "Email unexpected in 'disabled' mode");
    }

    @Test
    @DisplayName("'disabled' on by default")
    public void sendMail_Disabled_Default() {
        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        mailers.getDefaultMailer().sendMail(email, false);
        assertFalse(greenMail.waitForIncomingEmail(500, 1), "Email unexpected in 'disabled' mode");
    }

    @Test
    @DisplayName("'to' recipient override")
    public void sendMail_RecipientOverride_To() throws MessagingException {

        GreenMailUser x = greenMail.setUser("x@example.org", "x", "secret");
        GreenMailUser z = greenMail.setUser("z@example.org", "z", "secret");

        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.recipientOverrides", "z@example.org"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .to("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        sendSyncNoRetrieve(email, mailers.getDefaultMailer());

        // must be delivered to the override address, but "To" address must be preserved..

        assertNull(readFirst(x), "Was still delivered to the original address");
        MimeMessage message = readFirst(z);
        assertNotNull(message, "Was not delivered to the override address");

        assertEquals("y@example.org", message.getFrom()[0].toString());
        assertEquals("x@example.org", message.getRecipients(Message.RecipientType.TO)[0].toString());
        assertNull(message.getRecipients(Message.RecipientType.CC));
        assertEquals("test subject", message.getSubject());
        assertEquals("test body", GreenMailUtil.getBody(message));
    }

    @Test
    @DisplayName("'cc' recipient override")
    public void sendMail_RecipientOverride_Cc() throws MessagingException {

        GreenMailUser x = greenMail.setUser("x@example.org", "x", "secret");
        GreenMailUser z = greenMail.setUser("z@example.org", "z", "secret");

        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.recipientOverrides", "z@example.org"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .cc("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        sendSyncNoRetrieve(email, mailers.getDefaultMailer());

        // must be delivered to the override address, but "To" address must be preserved..

        assertNull(readFirst(x), "Was still delivered to the original address");
        MimeMessage message = readFirst(z);
        assertNotNull(message, "Was not delivered to the override address");

        assertEquals("y@example.org", message.getFrom()[0].toString());
        assertNull(message.getRecipients(Message.RecipientType.TO));
        assertEquals("x@example.org", message.getRecipients(Message.RecipientType.CC)[0].toString());
        assertEquals("test subject", message.getSubject());
        assertEquals("test body", GreenMailUtil.getBody(message));
    }

    @Test
    @DisplayName("'bcc' recipient override")
    public void sendMail_RecipientOverride_Bcc() throws MessagingException {

        GreenMailUser x = greenMail.setUser("x@example.org", "x", "secret");
        GreenMailUser z = greenMail.setUser("z@example.org", "z", "secret");

        Mailers mailers = testFactory.app()
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.recipientOverrides", "z@example.org"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.disabled", "false"))
                .module(b -> BQCoreModule.extend(b).setProperty("bq.simplejavamail.mailers.x.smtpPort", "3025"))
                .createRuntime()
                .getInstance(Mailers.class);

        Email email = EmailBuilder.startingBlank()
                .from("y@example.org")
                .bcc("x@example.org")
                .withSubject("test subject")
                .withPlainText("test body")
                .buildEmail();

        sendSyncNoRetrieve(email, mailers.getDefaultMailer());

        // must be delivered to the override address, but "To" address must be preserved..

        assertNull(readFirst(x), "Was still delivered to the original address");
        MimeMessage message = readFirst(z);
        assertNotNull(message, "Was not delivered to the override address");

        assertEquals("y@example.org", message.getFrom()[0].toString());
        assertNull(message.getRecipients(Message.RecipientType.TO));
        assertNull(message.getRecipients(Message.RecipientType.CC));
        assertEquals("test subject", message.getSubject());
        assertEquals("test body", GreenMailUtil.getBody(message));
    }

    private MimeMessage readFirst(GreenMailUser user) throws MessagingException {
        Session imapSession = greenMail.getImap().createSession();
        Store store = imapSession.getStore("imap");
        store.connect(user.getLogin(), user.getPassword());
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        return inbox.getMessageCount() == 0 ? null : (MimeMessage) inbox.getMessage(1);
    }

    private MimeMessage sendSync(Email email, Mailer mailer) {
        mailer.sendMail(email, false);

        // TODO: oddly enough with the addition of the batch module, sync delivery still requires waiting on the
        //  Greenmail end.. Is it truly sync?
        assertTrue(greenMail.waitForIncomingEmail(500, 1));
        MimeMessage[] received = greenMail.getReceivedMessages();
        assertEquals(1, received.length);
        return received[0];
    }

    private void sendSyncNoRetrieve(Email email, Mailer mailer) {
        mailer.sendMail(email, false);

        // TODO: oddly enough with the addition of the batch module, sync delivery still requires waiting on the
        //  Greenmail end.. Is it truly sync?
        assertTrue(greenMail.waitForIncomingEmail(500, 1));
    }

    private MimeMessage sendAsync(Email email, Mailer mailer) {
        mailer.sendMail(email, true);
        assertTrue(greenMail.waitForIncomingEmail(500, 1));
        MimeMessage[] received = greenMail.getReceivedMessages();
        assertEquals(1, received.length);
        return received[0];
    }
}
