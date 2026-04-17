package com.mani.resumeanalyzer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mani.resumeanalyzer.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	public Optional<User>  findByEmail(String email);

}
