package com.khushal.moviezone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khushal.moviezone.dto.Seat;

public interface SeatRepository extends JpaRepository<Seat, Integer>{

	List<Seat> findBySeatNumberIn(List<String> selectedSeats);

}
