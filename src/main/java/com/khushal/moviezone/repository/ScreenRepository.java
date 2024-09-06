package com.khushal.moviezone.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khushal.moviezone.dto.Screen;

public interface ScreenRepository extends JpaRepository<Screen, Integer>{

	boolean existsByName(String name);

}
