package com.khushal.moviezone.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Component
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Size(min = 3, max = 20, message = " * Enter Your Name Between 3 to 20 character")
	private String name;
	@DecimalMin(value = "6000000000", message = " * Enter Proper Number")
	@DecimalMax(value = "9999999999", message = " * Enter Proper Number")
	private long mobile;
	@NotEmpty(message = "* It is Mandatory Field")
	@Email(message = "* Enter Proper Email")
	private String email;
	@NotNull(message = "It is Mandatory Field")
	private String gender;
//	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\\\d)(?=.*[@$!%*#?&])[A-Za-z\\\\d@$!%*#?&]{8,}$\", message = \"* Enter 8 charecters with one lowercase, one uppercase, one number and one special charecter")
	private String pass;
//	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\\\d)(?=.*[@$!%*#?&])[A-Za-z\\\\d@$!%*#?&]{8,}$\", message = \"* Enter 8 charecters with one lowercase, one uppercase, one number and one special charecter")
	@Transient
	private String cnf_pass;
	@Past(message = "* Enter proper DOB")
	@NotNull(message = "* It is Mandatory field")
	private LocalDate dob;
	private int otp;
	private boolean verified;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	List<Booking> bookingList = new ArrayList<Booking>();
}
