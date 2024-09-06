package com.khushal.moviezone.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.khushal.moviezone.dto.Screen;
import com.khushal.moviezone.dto.Show;

public interface ShowRepository extends JpaRepository<Show, Integer>{

	List<Show> findByScreenIn(List<Screen> screens);

}
