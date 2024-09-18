package com.khushal.moviezone.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.khushal.moviezone.dto.Booking;
import com.khushal.moviezone.dto.Customer;
import com.khushal.moviezone.dto.Seat;
import com.khushal.moviezone.dto.Show;
import com.khushal.moviezone.helper.AES;
import com.khushal.moviezone.helper.EmailSendingHelper;
import com.khushal.moviezone.repository.BookingRepository;
import com.khushal.moviezone.repository.CustomerRepository;
import com.khushal.moviezone.repository.SeatRepository;
import com.khushal.moviezone.repository.ShowRepository;
import com.khushal.moviezone.repository.TheaterRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/customer")
public class CustomerController {
	
	@Autowired
	Customer customer;
	

	@Value("${razorpay.key}")
	private String key;

	@Value("${razorpay.secret}")
	private String secret;
	
	@Autowired
	EmailSendingHelper emailSendingHelper;
	
	@Autowired
	CustomerRepository customerRepository;
	@Autowired
	TheaterRepository theaterRepository;
	@Autowired
	ShowRepository showRepository;
	@Autowired
	SeatRepository seatRepository;
	@Autowired
	BookingRepository bookingRepository;
	
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
	
	@GetMapping("/book-show/{showId}")
	public String bookShow(@PathVariable int showId, ModelMap map) {
		Show show = showRepository.findById(showId).orElseThrow();
		List<Seat> seats = show.getScreen().getSeats();
		map.put("show", show);
		map.put("seats", seats);
		return "seat-selection";
	}
	
	@PostMapping("/book-seats")
	public String bookSeats(@RequestParam int showId, @RequestParam List<String> selectedSeats, HttpSession session,
			ModelMap map) throws RazorpayException {
		Customer customer = (Customer) session.getAttribute("customer");
		if (customer != null) {
			Show show = showRepository.findById(showId).orElseThrow();
			List<Seat> seats = seatRepository.findBySeatNumberIn(selectedSeats);

			for (Seat seat : seats) {
				if (seat.isOccupied()) {
					session.setAttribute("failure", "Selected seat(s) are already booked");
					return "redirect:/customer/book-show/" + showId;
				}
			}

			for (Seat seat : seats) {
				seat.setOccupied(true);
			}
			seatRepository.saveAll(seats);

			double price = selectedSeats.size() * show.getTicketPrice();

			Booking booking = new Booking();
			booking.setPrice(price);
			booking.setCustomer(customer);
			booking.setShow(show);
			booking.setSeatNumbers(seats);
			booking.setBookingTime(LocalDateTime.now());
			bookingRepository.save(booking);
			System.out.println("===============> "+booking.getId());
			List<Booking> list = customer.getBookingList();
			list.add(booking);
			customer.setBookingList(list);

			session.setAttribute("success", "Confirm Seat Details and do Payment");

			RazorpayClient razorpay = new RazorpayClient(key, secret);
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", price * 100);
			orderRequest.put("currency", "INR");

			Order order = razorpay.orders.create(orderRequest);

			map.put("key", key);
			map.put("selectedSeats", selectedSeats);
			map.put("totalAmount", price);
			map.put("show", show);
			map.put("customer", customer);
			map.put("orderId", order.get("id"));
			map.put("bookingId", booking.getId());
			return "booking-confirmation-page.html";
		} else {
			session.setAttribute("failure", "First Login To Book Tickets");
			return "redirect:/login";
		}
	}
}
