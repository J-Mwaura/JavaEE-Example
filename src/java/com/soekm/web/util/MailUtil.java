/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.soekm.web.util;

import java.io.IOException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 *
 * @author atjkm
 */
public class MailUtil {
    public static void sendEmailRegistrationLink(String id, String email, String hash) throws AddressException, MessagingException {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", Setup.MAIL_SMTP_HOST);
		props.put("mail.smtp.port", "10.101.3.229");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
                        @Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Setup.MAIL_USERNAME, Setup.MAIL_PASSWORD);
			}
		  });

		String link = Setup.MAIL_REGISTRATION_SITE_LINK+"?scope=activation&userId="+id+"&hash="+hash;
		
		  StringBuilder bodyText = new StringBuilder(); 
			bodyText.append("<div>")
			     .append("  Dear User,<br/><br/>")
			     .append("  Thank you for registration.")
			     .append("  Please click <a href=\""+link+"\">here</a> to verify your email.")
			     /*.append("  <a href=\""+link+"\">"+link+"</a>")*/
			     .append("  <br/><br/>")
			     .append("  Thank you,<br/>")
			     .append("  Soekm Ltd.")
			     .append("</div>");
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(Setup.MAIL_USERNAME));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(email));
			message.setSubject("Email Registration");
			message.setContent(bodyText.toString(), "text/html; charset=utf-8");
			Transport.send(message);
	}

	public static void sendResetPasswordLink(String id, String email, String hash) throws AddressException, MessagingException, IOException {

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", Setup.MAIL_SMTP_HOST);
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
                        @Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(Setup.MAIL_USERNAME, Setup.MAIL_PASSWORD);
			}
		  });

		String link = Setup.MAIL_NEW_PASSWORD_SITE_LINK+"?scope=resetPassword&userId="+id+"&hash="+hash;
		
		  StringBuilder bodyText = new StringBuilder(); 
			bodyText.append("<div>")
			     .append("  Dear User<br/><br/>")
			     .append("  We received your request:<br/>")
			     .append("  Please click <a href=\""+link+"\">here</a> to reset your password:<br/>")
			     //.append("  <a href=\""+link+"\">"+link+"</a>")
                             .append("  <br/><br/>")
                             .append("  Please note, this reset link will automatically expire within one day <br/>")
			     .append("  <br/><br/>")
			     .append("  Thank you,<br/>")
			     .append("  Soekm Ltd.")
			     .append("</div>");
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(Setup.MAIL_USERNAME));
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(email));
			message.setSubject("Reset Password");
			message.setContent(bodyText.toString(), "text/html; charset=utf-8");
			Transport.send(message);
	}
}
