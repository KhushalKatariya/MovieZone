package com.khushal.moviezone.controller;

import java.security.DrbgParameters.NextBytes;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.khushal.moviezone.dto.Customer;
import com.khushal.moviezone.dto.Theater;
import com.khushal.moviezone.helper.AES;
import com.khushal.moviezone.helper.EmailSendingHelper;
import com.khushal.moviezone.repository.CustomerRepository;
import com.khushal.moviezone.repository.TheaterRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class GeneralController {

	@Autowired
	CustomerRepository customerRepository;
	@Autowired
	TheaterRepository theaterRepository;

	@Autowired
	EmailSendingHelper emailSendingHelper;

	@Value("${admin.user}")
	private String adminUser;
	@Value("${admin.password}")
	private String password;

//	HOME PAGE
	@GetMapping("/")
	public String homePage() {
		return "home";
	}

//	LOGIN PAGE
	@GetMapping("/login")
	public String loadLogin() {
		return "login.html";
	}

//	POST LOGIN PAGE
	@PostMapping("/login")
	public String login(@RequestParam String emph, @RequestParam String pass, HttpSession session) {
		try {
			Long mobile = Long.parseLong(emph);
			Customer customer = customerRepository.findByMobile(mobile);
			Theater theater = theaterRepository.findByMobile(mobile);

			if (customer == null && theater == null) {
				session.setAttribute("failure", "Invalid Mobile Number");
				return "redirect:/login";
			} else {
				if (customer != null) {
					if (AES.decrypt(customer.getPass(), "123").equals(pass)) {
						if (customer.isVerified()) {
							session.setAttribute("success", "Login Success as Customer");
							session.setAttribute("customer", "customer");
							return "redirect:/";
						} else {
							customer.setOtp(new Random().nextInt(100000, 1000000));
							emailSendingHelper.sendMailToCustomer(customer);
							customerRepository.save(customer);
							session.setAttribute("success", "OTP sent Seccessfully");
							session.setAttribute("id", customer.getId());
							return "redirect:/customer/enter-otp";
						}
					} else {
						session.setAttribute("failure", "Invalid Password");
						return "redirect:/login";
					}
				} else {
					if (AES.decrypt(theater.getPass(), "123").equals(pass)) {
						if (theater.isVerified()) {
							if (theater.isApproved()) {
								session.setAttribute("success", "Login Success as Theater");
								session.setAttribute("theater", "theater");
								return "redirect:/login";
							} else {
								session.setAttribute("failure",
										"Approval is on Process wait sometime or Contact Admin");
								return "redirect:/login";
							}
						} else {
							theater.setOtp(new Random().nextInt(100000, 1000000));
							emailSendingHelper.sendMailToTheater(theater);
							theaterRepository.save(theater);
							session.setAttribute("success", "OTP Sent Successfully");
							session.setAttribute("id", theater.getId());
							return "redirect:/theater/enter-otp";
						}
					} else {
						session.setAttribute("failure", "Invalid Password");
						return "redirect:/login";
					}
				}
			}
		} catch (NumberFormatException e) {
			String email = emph;

			if (email.equals(adminUser) && password.equals(password)) {
				session.setAttribute("success", "Login Success as Admin");
				session.setAttribute("admin", "admin");
				return "redirect:/";
			} else {
				Customer customer = customerRepository.findByEmail(email);
				Theater theater = theaterRepository.findByEmail(email);

				if (customer == null && theater == null) {
					session.setAttribute("failure", "Invalid Email");
					return "redirect:/login";
				} else {
					if (customer != null) {
						if (AES.decrypt(customer.getPass(), "123").equals(pass)) {
							if (customer.isVerified()) {
								session.setAttribute("success", "Login Success as Customer");
								session.setAttribute("customer", "customer");
								return "redirect:/";
							} else {
								customer.setOtp(new Random().nextInt(100000, 1000000));
								emailSendingHelper.sendMailToCustomer(customer);
								customerRepository.save(customer);
								session.setAttribute("success", "OTP Sent Successfully");
								session.setAttribute("id", customer.getId());
								return "redirect:/customer/enter-otp";
							}
						} else {
							session.setAttribute("failure", "Invalid Password");
							return "redirect:/login";
						}
					} else {
						if (AES.decrypt(theater.getPass(), "123").equals(pass)) {
							if (theater.isVerified()) {
								if (theater.isApproved()) {
									session.setAttribute("success", "Login Successfully as Thater");
									session.setAttribute("theater", "theater");
									return "redirect:/";
								} else {
									session.setAttribute("failure",
											"Approval in under process wait sometime or contact Admin");
									return "redirect:/login";
								}
							} else {
								theater.setOtp(new Random().nextInt(100000, 1000000));
								emailSendingHelper.sendMailToTheater(theater);
								theaterRepository.save(theater);
								session.setAttribute("success", "OTP Sent Succesfully");
								session.setAttribute("id", theater.getId());
								return "redirect:/theater/enter-opt";
							}
						} else {
							session.setAttribute("failure", "Invalid Password");
							return "redirect:/login";
						}
					}
				}
			}
		}
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.removeAttribute("customer");
		session.removeAttribute("admin");
		session.removeAttribute("theater");
		session.setAttribute("susccess", "Logout Succesfull");
		return "redirect:/";
	}

	@GetMapping("/admin/approve-theater")
	public String approveTheater(HttpSession session, ModelMap map) {
		if (session.getAttribute("admin") != null) {
			List<Theater> list = theaterRepository.findByApprovedFalse();
			System.out.println(list);
			if (list.isEmpty()) {
				session.setAttribute("failure", "No Theater Pending with Approval");
				return "redirect:/";
			} else {
				map.put("list", list);
				return "approve-theater";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/admin/approve-theater/{id}")
	public String approveTheater(HttpSession session, ModelMap map, @PathVariable int id) {
		if (session.getAttribute("admin") != null) {
			Theater theater = theaterRepository.findById(id).orElseThrow();
			theater.setApproved(true);
			theaterRepository.save(theater);
			session.setAttribute("success", "Account is Approved");
			return "redirect:/";
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/admin/add-movie")
	public String addMovie(HttpSession session) {
		if (session.getAttribute("admin") != null) {
			return "add-movie";
		} else {
			session.setAttribute("failure", "Invalid Session, Login again");
			return "redirect:/login";
		}
	}
}