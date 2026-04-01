package com.example.RankCat.repository;

import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserHistoryRepository extends JpaRepository<UserHistory, Long> {

    @Query(
            """
            select h
            from UserHistory h
            where h.user = :user
              and (
                    :cursorCreatedAt is null
                    or h.createdAt < :cursorCreatedAt
                    or (h.createdAt = :cursorCreatedAt and h.id < :cursorId)
              )
            order by h.createdAt desc, h.id desc
            """)
    List<UserHistory> findHistorySliceByUser(
            @Param("user") User user,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    List<UserHistory> findByUserAndProductIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            User user, String productId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query(
            """
            select h
            from UserHistory h
            where h.user = :user
              and h.query = :query
              and (
                    :cursorCreatedAt is null
                    or h.createdAt < :cursorCreatedAt
                    or (h.createdAt = :cursorCreatedAt and h.id < :cursorId)
              )
            order by h.createdAt desc, h.id desc
            """)
    List<UserHistory> findHistorySliceByUserAndQuery(
            @Param("user") User user,
            @Param("query") String query,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);
}
