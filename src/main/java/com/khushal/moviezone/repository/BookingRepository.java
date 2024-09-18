package com.khushal.moviezone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khushal.moviezone.dto.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer>{

}
