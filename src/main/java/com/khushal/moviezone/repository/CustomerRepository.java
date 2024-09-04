package com.khushal.moviezone.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.khushal.moviezone.dto.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer>{

	boolean existsByEmail(String email);

	boolean existsByMobile(long mobile);

	Customer findByEmail(String email);

	Customer findByMobile(Long mobile);

}
