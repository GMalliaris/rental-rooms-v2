package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.springframework.data.util.Pair;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public MailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    protected Pair<MimeMessage, MimeMessageHelper> createMimeClasses() throws MessagingException {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name());

        return Pair.of(message, helper);
    }

    public void sendRegistrationConfirmationEmail(ConfirmationToken token) throws MessagingException {

        var registrationConfirmationTemplate = "registration-confirmation";
        var registrationConfirmationSubject = "Registration Confirmation";

        var mimePair = createMimeClasses();
        var message = mimePair.getFirst();
        var helper = mimePair.getSecond();

        var props = new HashMap<String, Object>();
        props.put("firstName", token.getAccountUser().getFirstName());
        props.put("lastName", token.getAccountUser().getLastName());
        props.put("confirmationToken", token.getId());
        var defaultZoneId = ZoneId.systemDefault();
        var expirationInstant = token.getExpirationDate().atStartOfDay(defaultZoneId).toInstant();
        props.put("confirmationTokenExpirationDate", Date.from(expirationInstant));

        var context = new Context();
        context.setVariables(props);
        var html = templateEngine.process(registrationConfirmationTemplate, context);

        helper.setSubject(registrationConfirmationSubject);
        helper.setTo(token.getAccountUser().getEmail());
        helper.setText(html, true);

        mailSender.send(message);
    }
}
