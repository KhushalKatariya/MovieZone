package com.khushal.moviezone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khushal.moviezone.dto.Theater;

public interface TheaterRepository extends JpaRepository<Theater, Integer>{

	boolean existsByEmail(String email);

	boolean existsByMobile(long mobile);

	Theater findByEmail(String email);

	Theater findByMobile(Long mobile);

	List<Theater> findByApprovedFalse();

}
