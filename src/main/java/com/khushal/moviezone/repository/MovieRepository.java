package com.khushal.moviezone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khushal.moviezone.dto.Movie;

public interface MovieRepository extends JpaRepository<Movie, Integer>{

}
