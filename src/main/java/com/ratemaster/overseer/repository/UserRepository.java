package com.ratemaster.overseer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ratemaster.overseer.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

	Boolean existsByEmailId(final String emailId);

	Optional<User> findByEmailId(final String emailId);

}