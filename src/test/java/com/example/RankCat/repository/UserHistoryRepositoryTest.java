package com.example.RankCat.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.RankCat.config.jpa.JpaConfig;
import com.example.RankCat.model.Role;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Import(JpaConfig.class)
class UserHistoryRepositoryTest {

    @Autowired private UserHistoryRepository userHistoryRepository;

    @Autowired private TestEntityManager entityManager;

    @Test
    @DisplayName("사용자 이력 슬라이스 조회는 createdAt desc, id desc 순으로 정렬된다")
    void findHistorySliceByUser_ordersByCreatedAtDescThenIdDesc() {
        User user = persistUser("user1@test.com");
        LocalDateTime sameTime = LocalDateTime.of(2026, 4, 5, 10, 0, 0);

        persistHistory(user, "키워드", "old", "p-old", sameTime.minusHours(1));
        persistHistory(user, "키워드", "same-1", "p-1", sameTime);
        persistHistory(user, "키워드", "same-2", "p-2", sameTime);

        entityManager.flush();
        entityManager.clear();

        List<UserHistory> result =
                userHistoryRepository.findHistorySliceByUser(
                        user, null, null, PageRequest.of(0, 10));

        assertThat(result)
                .extracting(UserHistory::getTitle)
                .containsExactly("same-2", "same-1", "old");

        assertThat(result.get(0).getId()).isGreaterThan(result.get(1).getId());
        assertThat(result.get(2).getCreatedAt())
                .isEqualTo(sameTime.minusHours(1).truncatedTo(ChronoUnit.MICROS));
    }

    @Test
    @DisplayName("복합 커서(createdAt, id) 이후의 데이터만 다음 슬라이스로 조회한다")
    void findHistorySliceByUser_appliesCompositeCursor() {
        User user = persistUser("user2@test.com");
        LocalDateTime time = LocalDateTime.of(2026, 4, 5, 12, 0, 0);

        persistHistory(user, "키워드", "newest", "p-new", time.plusMinutes(1));
        persistHistory(user, "키워드", "same-1", "p-1", time);
        persistHistory(user, "키워드", "same-2", "p-2", time);
        persistHistory(user, "키워드", "older", "p-old", time.minusMinutes(1));

        entityManager.flush();
        entityManager.clear();

        List<UserHistory> firstPage =
                userHistoryRepository.findHistorySliceByUser(
                        user, null, null, PageRequest.of(0, 2));

        UserHistory cursor = firstPage.get(1);

        List<UserHistory> secondPage =
                userHistoryRepository.findHistorySliceByUser(
                        user, cursor.getCreatedAt(), cursor.getId(), PageRequest.of(0, 10));

        assertThat(firstPage).extracting(UserHistory::getTitle).containsExactly("newest", "same-2");

        assertThat(secondPage).extracting(UserHistory::getTitle).containsExactly("same-1", "older");
    }

    @Test
    @DisplayName("검색어 조건 슬라이스 조회는 사용자와 query가 모두 일치하는 데이터만 최신순으로 반환한다")
    void findHistorySliceByUserAndQuery_filtersByQuery() {
        User user = persistUser("user3@test.com");
        User otherUser = persistUser("other@test.com");
        LocalDateTime base = LocalDateTime.of(2026, 4, 5, 15, 0, 0);

        persistHistory(user, "노트북", "match-1", "p1", base.plusMinutes(2));
        persistHistory(user, "마우스", "other-query", "p2", base.plusMinutes(1));
        persistHistory(user, "노트북", "match-2", "p3", base);
        persistHistory(otherUser, "노트북", "other-user", "p4", base.plusMinutes(3));

        entityManager.flush();
        entityManager.clear();

        List<UserHistory> result =
                userHistoryRepository.findHistorySliceByUserAndQuery(
                        user, "노트북", null, null, PageRequest.of(0, 10));

        assertThat(result).extracting(UserHistory::getTitle).containsExactly("match-1", "match-2");
    }

    @Test
    @DisplayName("상품별 순위 이력 조회는 기간 내 데이터만 createdAt 오름차순으로 반환한다")
    void findByUserAndProductIdAndCreatedAtBetweenOrderByCreatedAtAsc_filtersRangeAndOrdersAsc() {
        User user = persistUser("user4@test.com");
        LocalDateTime base = LocalDateTime.of(2026, 4, 1, 0, 0, 0);

        persistHistory(user, "키워드", "out-of-range-before", "product-1", base.minusDays(1));
        persistHistory(user, "키워드", "in-range-1", "product-1", base.plusDays(1));
        persistHistory(user, "키워드", "other-product", "product-2", base.plusDays(2));
        persistHistory(user, "키워드", "in-range-2", "product-1", base.plusDays(3));
        persistHistory(user, "키워드", "out-of-range-after", "product-1", base.plusDays(5));

        entityManager.flush();
        entityManager.clear();

        List<UserHistory> result =
                userHistoryRepository.findByUserAndProductIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                        user, "product-1", base, base.plusDays(4), PageRequest.of(0, 10));

        assertThat(result)
                .extracting(UserHistory::getTitle)
                .containsExactly("in-range-1", "in-range-2");
    }

    private User persistUser(String email) {
        User user =
                User.builder()
                        .email(email)
                        .password("encoded-password")
                        .nickname("tester")
                        .role(Role.USER)
                        .build();
        entityManager.persist(user);
        return user;
    }

    private UserHistory persistHistory(
            User user, String query, String title, String productId, LocalDateTime createdAt) {

        UserHistory history =
                UserHistory.builder()
                        .user(user)
                        .query(query)
                        .title(title)
                        .category1("디지털")
                        .category2("노트북")
                        .category3("게이밍")
                        .category4("17인치")
                        .predRank(3.4)
                        .predRankClipped(3.0)
                        .lprice(1000000)
                        .hprice(1200000)
                        .mallName("랭캣스토어")
                        .brand("RankCat")
                        .maker("RankCat")
                        .productId(productId)
                        .productType("MALL")
                        .build();

        entityManager.persistAndFlush(history);

        LocalDateTime dbTime = createdAt.truncatedTo(ChronoUnit.MICROS);

        entityManager
                .getEntityManager()
                .createNativeQuery("update user_history set created_at = ? where id = ?")
                .setParameter(1, Timestamp.valueOf(dbTime))
                .setParameter(2, history.getId())
                .executeUpdate();

        ReflectionTestUtils.setField(history, "createdAt", dbTime);
        return history;
    }
}
