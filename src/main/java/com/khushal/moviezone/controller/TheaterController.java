package com.khushal.moviezone.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.khushal.moviezone.dto.Theater;
import com.khushal.moviezone.helper.AES;
import com.khushal.moviezone.helper.EmailSendingHelper;
import com.khushal.moviezone.repository.CustomerRepository;
import com.khushal.moviezone.repository.TheaterRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/theater")
public class TheaterController {

	@Autowired
	Theater theater;
	
	@Autowired
	EmailSendingHelper emailSendingHelper;
	
	@Autowired
	CustomerRepository customerRepository;
	@Autowired
	TheaterRepository theaterRepository;
	
//	FOR CUSTOMER REGISTRATION
	
	@GetMapping("/signup")
	public String theaterRegistration(ModelMap map) {
		map.put("theater", theater);
		return "theater-signup";
	}
	
	@PostMapping("/signup")
	public String createRegistration(@Valid Theater theater, BindingResult result, HttpSession session ) {
		if(!theater.getPass().equals(theater.getCnfPass())) {
			result.rejectValue("cnfPass", "error.cnfPpass", "* Password Mismatch");
		}
		
		if(theaterRepository.existsByEmail(theater.getEmail()) || theaterRepository.existsByEmail(theater.getEmail())) {
			result.rejectValue("email", "error.email", "* Account is Already Exist by This Email");
		}
		
		if(theaterRepository.existsByMobile(theater.getMobile()) || theaterRepository.existsByMobile(theater.getMobile())) {
			result.rejectValue("mobile", "error.mobile", "* Account Alredy Exist by this Mobile");
		}
		
		if(result.hasErrors()) {
			return "theater-signup";
		}
		else {
			theater.setPass(AES.encrypt(theater.getPass(), "123"));
			theater.setOtp(new Random().nextInt(100000,1000000));
			emailSendingHelper.sendMailToTheater(theater);
			theaterRepository.save(theater);
			session.setAttribute("success", "OTP Sent Successfully");
			session.setAttribute("id", theater.getId());
			return "redirect:/theater/enter-otp";
		}
	}
	
	@GetMapping("/enter-otp")
	public String enterOTP(ModelMap map) {
		map.put("user", "theater");
		return "enter-otp";
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam int id, @RequestParam int otp, HttpSession session) {
		Theater theater = theaterRepository.findById(id).orElseThrow();
		if(theater.getOtp() == otp) {
			theater.setVerified(true);
			theaterRepository.save(theater);
			session.setAttribute("success", "Account Created Successfully");
			return "redirect:/login";
		}else {
			session.setAttribute("failure", "Invalid Otp Try Again..!");
			return "redirect:/theater/enter-otp";
		}
	}
}
