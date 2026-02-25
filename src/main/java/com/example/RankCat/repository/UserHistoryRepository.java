package com.example.RankCat.repository;

import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {
    List<UserHistory> findAllByUserOrderByCreatedAtDesc(User user);
}
