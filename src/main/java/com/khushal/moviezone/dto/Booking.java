package com.khushal.moviezone.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private double price;
	
	@ManyToOne
	private Customer customer;
	
	@ManyToOne
	private Show show;
	
	@ElementCollection
	private List<Seat> seatNumbers;	
	private LocalDateTime bookingTime;
	private boolean booked;
	private String pymentId;
}
