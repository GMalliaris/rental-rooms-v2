package org.gmalliaris.rental.rooms.service;

import org.gmalliaris.rental.rooms.entity.AccountUser;
import org.gmalliaris.rental.rooms.entity.ConfirmationToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @InjectMocks
    private MailService mailService;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Test
    void createMimeClassesTest() throws MessagingException {
        var mockMimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage())
                .thenReturn(mockMimeMessage);
        var result = mailService.createMimeClasses();
        assertNotNull(result);
        assertEquals(mockMimeMessage, result.getFirst());
        assertNotNull(result.getSecond());
        assertEquals(mockMimeMessage, result.getSecond().getMimeMessage());
    }

    @Test
    void sendRegistrationConfirmationEmailTest() throws MessagingException {
        var spyService = spy(mailService);

        var mockMimeMessage = mock(MimeMessage.class);
        var mockMimeMessageHelper = mock(MimeMessageHelper.class);
        doReturn(Pair.of(mockMimeMessage, mockMimeMessageHelper))
                .when(spyService).createMimeClasses();

        var htmlText = "<h1>TEST</h1>";
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn(htmlText);
        doNothing().when(mailSender)
                .send(any(MimeMessage.class));

        var user = new AccountUser();
        user.setFirstName("random-first");
        user.setLastName("random-last");
        user.setEmail("random@example.eg");

        var now = LocalDate.now();
        var token = new ConfirmationToken();
        token.setId(UUID.randomUUID());
        token.setExpirationDate(now);
        token.setAccountUser(user);

        spyService.sendRegistrationConfirmationEmail(token);

        var contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("registration-confirmation"), contextCaptor.capture());
        var context = contextCaptor.getValue();
        assertNotNull(context);
        var firstName = context.getVariable("firstName");
        assertNotNull(firstName);
        assertEquals(user.getFirstName(), firstName);
        var lastName = context.getVariable("lastName");
        assertNotNull(lastName);
        assertEquals(lastName, user.getLastName());
        var tokenValue = context.getVariable("confirmationToken");
        assertNotNull(tokenValue);
        assertEquals(token.getId(), tokenValue);
        var expDate = context.getVariable("confirmationTokenExpirationDate");
        assertNotNull(expDate);
        var tokenExpDateInstant = token.getExpirationDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        assertEquals(Date.from(tokenExpDateInstant).toString(), expDate.toString());

        verify(mockMimeMessageHelper).setSubject("Registration Confirmation");
        verify(mockMimeMessageHelper).setTo(user.getEmail());
        verify(mockMimeMessageHelper).setText(htmlText, true);
        verify(mailSender).send(mockMimeMessage);
    }
}