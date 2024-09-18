package com.khushal.moviezone.controller;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.khushal.moviezone.dto.Movie;
import com.khushal.moviezone.dto.Screen;
import com.khushal.moviezone.dto.Seat;
import com.khushal.moviezone.dto.Show;
import com.khushal.moviezone.dto.Theater;
import com.khushal.moviezone.helper.AES;
import com.khushal.moviezone.helper.EmailSendingHelper;
import com.khushal.moviezone.repository.CustomerRepository;
import com.khushal.moviezone.repository.MovieRepository;
import com.khushal.moviezone.repository.ScreenRepository;
import com.khushal.moviezone.repository.ShowRepository;
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
	@Autowired
	ScreenRepository screenRepository;
	@Autowired
	MovieRepository movieRepository;
	@Autowired
	ShowRepository showRepository;

//	FOR CUSTOMER REGISTRATION

	@GetMapping("/signup")
	public String theaterRegistration(ModelMap map) {
		map.put("theater", theater);
		return "theater-signup";
	}

	@PostMapping("/signup")
	public String createRegistration(@Valid Theater theater, BindingResult result, HttpSession session) {
		if (!theater.getPass().equals(theater.getCnfPass())) {
			result.rejectValue("cnfPass", "error.cnfPpass", "* Password Mismatch");
		}

		if (theaterRepository.existsByEmail(theater.getEmail())
				|| theaterRepository.existsByEmail(theater.getEmail())) {
			result.rejectValue("email", "error.email", "* Account is Already Exist by This Email");
		}

		if (theaterRepository.existsByMobile(theater.getMobile())
				|| theaterRepository.existsByMobile(theater.getMobile())) {
			result.rejectValue("mobile", "error.mobile", "* Account Alredy Exist by this Mobile");
		}

		if (result.hasErrors()) {
			return "theater-signup";
		} else {
			theater.setPass(AES.encrypt(theater.getPass(), "123"));
			theater.setOtp(new Random().nextInt(100000, 1000000));
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
		if (theater.getOtp() == otp) {
			theater.setVerified(true);
			theaterRepository.save(theater);
			session.setAttribute("success", "Account Created Successfully");
			return "redirect:/login";
		} else {
			session.setAttribute("failure", "Invalid Otp Try Again..!");
			return "redirect:/theater/enter-otp";
		}
	}

	@GetMapping("/add-screen")
	public String addScreen(HttpSession session) {
		if (session.getAttribute("theater") != null) {
			return "add-screen.html";
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@PostMapping("/add-screen")
	public String addMovie(Screen screen, HttpSession session) {
		Theater theater = (Theater) session.getAttribute("theater");
		if (theater != null) {
			boolean flag = false;
			List<Screen> screens = theater.getScreens();
			for (Screen screen1 : screens) {
				if (screen.getName().equals(screen1.getName())) {
					flag = true;
					break;
				}
			}
			if (flag) {
				session.setAttribute("failure", "Screen Already Exists");
				return "redirect:/";
			} else {
				// Creating all seats
				List<Seat> seats = new ArrayList<>();
				for (char i = 'A'; i < 'A' + screen.getRow(); i++) {
					for (int j = 1; j <= screen.getColumn(); j++) {
						Seat seat = new Seat();
						seat.setSeatNumber(i + "" + j);
						seats.add(seat);
					}
				}
				screen.setSeats(seats);

				screens.add(screen);
				screen.setTheater(theater);

				theaterRepository.save(theater);
				session.setAttribute("theater", theaterRepository.findById(theater.getId()).orElseThrow());

				session.setAttribute("success", "Screen and Seats Added Success");
				return "redirect:/";
			}
		} else {
			session.setAttribute("failure", "Invalid Session,Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/add-show")
	public String addShow(HttpSession session, ModelMap map) {
		Theater theater = (Theater) session.getAttribute("theater");
		if (theater != null) {
			List<Screen> screens = theater.getScreens();
			List<Movie> movies = movieRepository.findAll();

			if (screens.isEmpty()) {
				session.setAttribute("failure", "No Screens Available for Show");
				return "redirect:/";
			}
			if (movies.isEmpty()) {
				session.setAttribute("failure", "No Movies Available for Show");
				return "redirect:/";
			}

			map.put("screens", screens);
			map.put("movies", movies);
			return "add-show";
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@PostMapping("/add-show")
	public String addShow(HttpSession session, Show show) {
		Theater theater = (Theater) session.getAttribute("theater");

		if (theater != null) {
			show.setMovie(movieRepository.findById(show.getMovie().getId()).orElseThrow());
			show.setScreen(screenRepository.findById(show.getScreen().getId()).orElseThrow());

			showRepository.save(show);
			session.setAttribute("success", "Show Added Success");
			return "redirect:/";
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/manage-show")
	public String manageShow(HttpSession session, ModelMap map) {
		Theater theater = (Theater) session.getAttribute("theater");
		if (theater != null) {
			List<Screen> screens = theater.getScreens();
			List<Show> shows = showRepository.findByScreenIn(screens);

			if (shows.isEmpty()) {
				session.setAttribute("failure", "No Show Added Yet");
				return "redirect:/";
			} else {
				map.put("shows", shows);
				return "manage-show";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}
	
	@GetMapping("/open-booking/{id}")
	public String openBooking(HttpSession session, @PathVariable int id) {
		Theater theater = (Theater) session.getAttribute("theater");
		if (theater != null) {
			Show show = showRepository.findById(id).orElseThrow();
			Screen screen = show.getScreen();
			int timing = show.getTiming();
			LocalDate movieDate = show.getMovie().getReleaseDate();
			LocalDate currentDate = LocalDate.now();
			if (Period.between(movieDate, currentDate).getDays() >= 0 && Period.between(movieDate, currentDate)
					.getDays() <= 10) {
				List<Movie> movies = movieRepository.findByReleaseDate(movieDate);
				boolean flag = showRepository.existsByScreenAndTimingAndAvailableTrueAndMovieIn(screen, timing, movies);
				if (flag) {
					session.setAttribute("failure", "Already there is a show running, can not open different booking");
					return "redirect:/theater/manage-show";
				} else {
					show.setAvailable(true);
					showRepository.save(show);
					session.setAttribute("success", "Bookings Open ");
					return "redirect:/theater/manage-show";
				}
			} else {
				session.setAttribute("failure", "Can not open Bookings Now");
				return "redirect:/theater/manage-show";
			}
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}

	@GetMapping("/close-booking/{id}")
	public String closeBooking(HttpSession session, @PathVariable int id) {
		Theater theater = (Theater) session.getAttribute("theater");
		if (theater != null) {
			Show show = showRepository.findById(id).orElseThrow();
			Screen screen = show.getScreen();
			List<Seat> seats = screen.getSeats();
			for (Seat seat : seats) {
				if (seat.isOccupied()) {
					session.setAttribute("failure", "Already tickets are booked You can not cancel");
					return "redirect:/theater/manage-show";
				}
			}
			show.setAvailable(false);
			showRepository.save(show);
			session.setAttribute("success", "Booking Closed");
			return "redirect:/theater/manage-show";
		} else {
			session.setAttribute("failure", "Invalid Session, Login Again");
			return "redirect:/login";
		}
	}
}