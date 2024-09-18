package com.khushal.moviezone.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khushal.moviezone.dto.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer>{

	List<Movie> findByReleaseDate(LocalDate movieDate);


}
