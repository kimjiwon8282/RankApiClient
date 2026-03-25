package com.example.RankCat.repository;

import com.example.RankCat.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // [실행 쿼리]
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // [실행 쿼리]
    // SELECT 1 FROM users WHERE email = ? LIMIT 1
    boolean existsByEmail(String email);
}
