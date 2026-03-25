package com.example.RankCat.repository;

import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    /**
     * 1. 내 전체 기록 조회 (페이징 & 정렬 적용) SQL(Data): SELECT * FROM user_history WHERE user_id = ? ORDER BY
     * created_at DESC LIMIT ? OFFSET ?; SQL(Count): SELECT COUNT(*) FROM user_history WHERE user_id
     * = ?;
     */
    Page<UserHistory> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 2. 특정 상품 기간별 추이 조회 (페이징 포함) SQL: SELECT * FROM user_history WHERE user_id = ? AND product_id
     * = ? AND created_at BETWEEN ? AND ? ORDER BY created_at ASC;
     */
    List<UserHistory> findByUserAndProductIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            User user, String productId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * 3. 내 기록 키워드 검색 SQL(Data): SELECT * FROM user_history WHERE user_id = ? AND query = ? LIMIT ?
     * OFFSET ?; SQL(Count): SELECT COUNT(*) FROM user_history WHERE user_id = ? AND query = ?;
     */
    Page<UserHistory> findByUserAndQuery(User user, String query, Pageable pageable);
}
