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

import com.khushal.moviezone.dto.Customer;
import com.khushal.moviezone.dto.Theater;
import com.khushal.moviezone.helper.AES;
import com.khushal.moviezone.helper.EmailSendingHelper;
import com.khushal.moviezone.repository.CustomerRepository;
import com.khushal.moviezone.repository.TheaterRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerController {
	
	@Autowired
	Customer customer;
	
	@Autowired
	EmailSendingHelper emailSendingHelper;
	
	@Autowired
	CustomerRepository customerRepository;
	@Autowired
	TheaterRepository theaterRepository;
	
//	FOR CUSTOMER REGISTRATION
	
	@GetMapping("/signup")
	public String customerRegistration(ModelMap map) {
		map.put("customer", customer);
		return "customer-signup";
	}
	
	@PostMapping("/signup")
	public String createRegistration(@Valid Customer customer , BindingResult result, HttpSession session ) {
		if(!customer.getPass().equals(customer.getCnf_pass())) {
			System.out.println(customer.getPass()+" , cnfpass"+customer.getCnf_pass());
			result.rejectValue("cnf_pass", "error.cnf_pass", "* Password Mismatch");
		}
		
		if(customerRepository.existsByEmail(customer.getEmail()) || theaterRepository.existsByEmail(customer.getEmail())) {
			result.rejectValue("email", "error.email", "* Account is Already Exist by This Email");
		}
		
		if(customerRepository.existsByMobile(customer.getMobile()) || theaterRepository.existsByMobile(customer.getMobile())) {
			result.rejectValue("mobile", "error.mobile", "* Account Alredy Exist by this Mobile");
		}
		
		if(result.hasErrors()) {
			return "customer-signup";
		}
		else {
			customer.setPass(AES.encrypt(customer.getPass(), "123"));
			customer.setOtp(new Random().nextInt(100000,1000000));
			emailSendingHelper.sendMailToCustomer(customer);
			customerRepository.save(customer);
			session.setAttribute("success", "OTP Sent Successfully");
			session.setAttribute("id", customer.getId());
			return "redirect:/customer/enter-otp";
		}
	}
	
	@GetMapping("/enter-otp")
	public String enterOTP(ModelMap map) {
		map.put("user", "customer");
		return "enter-otp";
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam int id, @RequestParam int otp, HttpSession session) {
		Customer customer = customerRepository.findById(id).orElseThrow();
		if(customer.getOtp() == otp) {
			customer.setVerified(true);
			customerRepository.save(customer);
			session.setAttribute("success", "Account Created Successfully");
			return "redirect:/login";
		}else {
			session.setAttribute("failure", "Invalid Otp Try Again..!");
			return "redirect:/customer/enter-otp";
		}
	}
	
//	FOR CUSTOMER LOGIN
	
	@GetMapping("/signin")
	public String customerLogin() {
		return "customer-signin";
	}
	
}
