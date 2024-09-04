package com.khushal.moviezone.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.khushal.moviezone.dto.Customer;
import com.khushal.moviezone.dto.Theater;

import jakarta.mail.internet.MimeMessage;

@Component
public class EmailSendingHelper {

	@Autowired
	JavaMailSender mailSender;
	
	@Autowired
	TemplateEngine templateEngine;
	
	public void sendMailToCustomer(Customer customer) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		try {
			helper.setFrom("khushalahir743344@gmail.com", "From-MovieZone-Site");
			helper.setTo(customer.getEmail());
			helper.setSubject("Email Verification OTP");
			Context context = new Context();
			context.setVariable("customer",	customer);
			String body = templateEngine.process("my-email-template.html",context);
			helper.setText(body,true);
			mailSender.send(message);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendMailToTheater(Theater theater) {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		
		try {
			helper.setFrom("khushalahir743344@gmail.com", "From-MovieZone-Site");
			helper.setTo(theater.getEmail());
			helper.setSubject("Email Verification OTP");
			Context context = new Context();
			context.setVariable("theater", theater);
			String body = templateEngine.process("my-email-template.html",context);
			helper.setText(body, true);
			mailSender.send(message);			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
