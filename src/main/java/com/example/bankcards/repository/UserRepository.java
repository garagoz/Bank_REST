package com.example.bankcards.repository;

import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    Page<User> findByUsernameContaining(String username, Pageable pageable);
    Page<User> findByFirstNameContaining(String firstName, Pageable pageable);
    Page<User> findByLastNameContaining(String lastName, Pageable pageable);
    Page<User> findByFirstNameContainingAndLastNameContaining(String firstName, String lastName, Pageable pageable);
}

