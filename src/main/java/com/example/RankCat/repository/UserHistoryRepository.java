package com.example.RankCat.repository;

import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    List<UserHistory> findAllByUserOrderByCreatedAtDesc(User user);
}
