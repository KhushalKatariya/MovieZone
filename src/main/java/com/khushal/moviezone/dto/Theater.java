package com.khushal.moviezone.dto;

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
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Component
@Entity
public class Theater {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Size(min = 3, max = 20, message = "Ente name Between 3 - 20 Character")
	private String name;
	@NotEmpty(message = "* It is Mandatory Feld")
	private String address;
	@DecimalMax(value = "9999999999", message = "Enter Proper Number")
	@DecimalMin(value = "6000000000", message = "Enter Proper Number")
	private long mobile;
	@NotEmpty(message = "* It is Mandatory Field")
	@Email(message = "* Enter Proper Email")
	private String email;
//	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", message = "* Enter 8 charecters with one lowercase, one uppercase, one number and one special charecter")
	private String pass;
//	@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", message = "* Enter 8 charecters with one lowercase, one uppercase, one number and one special charecter")
	@Transient
	private String cnfPass;
	@NotEmpty(message = "* It is Mandatory field")
	@Size(min = 5, max = 10, message = "* Enter no. between 5 - 10")
	private String licenceno;
	
	private int otp;
	private boolean verified;
	private boolean approved;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private List<Screen> screens = new ArrayList<>();

}