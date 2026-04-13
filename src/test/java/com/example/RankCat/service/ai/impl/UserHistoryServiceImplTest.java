package com.example.RankCat.service.ai.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.Role;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import com.example.RankCat.repository.UserHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserHistoryServiceImplTest {

    @Mock private UserHistoryRepository userHistoryRepository;

    @InjectMocks private UserHistoryServiceImpl userHistoryService;

    @Test
    @DisplayName("이력 저장 시 요청값을 UserHistory 엔티티로 매핑해 저장한다")
    void save_mapsRequestToEntityAndPersists() {
        // given
        User user = createUser();
        SaveHistoryRequest request = createSaveHistoryRequest();
        given(userHistoryRepository.save(any(UserHistory.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        UserHistory saved = userHistoryService.save(user, request);

        // then
        ArgumentCaptor<UserHistory> captor = ArgumentCaptor.forClass(UserHistory.class);
        verify(userHistoryRepository).save(captor.capture());

        UserHistory entity = captor.getValue();
        assertThat(saved).isSameAs(entity);
        assertThat(entity.getUser()).isEqualTo(user);
        assertThat(entity.getQuery()).isEqualTo("노트북");
        assertThat(entity.getTitle()).isEqualTo("가성비 노트북");
        assertThat(entity.getLprice()).isEqualTo(1000000);
        assertThat(entity.getHprice()).isEqualTo(1200000);
        assertThat(entity.getMallName()).isEqualTo("랭캣스토어");
        assertThat(entity.getBrand()).isEqualTo("RankCat");
        assertThat(entity.getMaker()).isEqualTo("RankCat");
        assertThat(entity.getProductId()).isEqualTo("12345");
        assertThat(entity.getProductType()).isEqualTo("GENERAL");
        assertThat(entity.getCategory1()).isEqualTo("디지털/가전");
        assertThat(entity.getCategory2()).isEqualTo("노트북");
        assertThat(entity.getCategory3()).isEqualTo("노트북");
        assertThat(entity.getCategory4()).isEqualTo("노트북");
        assertThat(entity.getPredRank()).isEqualTo(3.2);
        assertThat(entity.getPredRankClipped()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("복합 커서는 createdAt과 id를 함께 보내지 않으면 INVALID_INPUT_VALUE를 던진다")
    void getUserHistoriesWithCursor_invalidCursor_throwsInvalidInputValue() {
        // given
        User user = createUser();

        // when & then
        assertThatThrownBy(
                        () ->
                                userHistoryService.getUserHistoriesWithCursor(
                                        user, LocalDateTime.now(), null, 20))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("이력 조회 결과가 size를 초과하면 hasNext=true와 다음 커서를 반환한다")
    void getUserHistoriesWithCursor_hasNext_setsNextCursor() {
        // given
        User user = createUser();
        LocalDateTime now = LocalDateTime.of(2026, 4, 4, 20, 0);
        UserHistory first = createHistory(30L, now.minusMinutes(1), "첫 번째");
        UserHistory second = createHistory(20L, now.minusMinutes(2), "두 번째");
        UserHistory third = createHistory(10L, now.minusMinutes(3), "세 번째");

        given(
                        userHistoryRepository.findHistorySliceByUser(
                                eq(user), eq(null), eq(null), eq(PageRequest.of(0, 3))))
                .willReturn(List.of(first, second, third));

        // when
        UserHistoryResponse.HistorySliceResponse response =
                userHistoryService.getUserHistoriesWithCursor(user, null, null, 2);

        // then
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getHistories()).hasSize(2);
        assertThat(response.getHistories())
                .extracting(UserHistoryResponse.HistoryDto::getTitle)
                .containsExactly("첫 번째", "두 번째");
        assertThat(response.getNextCursorId()).isEqualTo(20L);
        assertThat(response.getNextCursorCreatedAt()).isEqualTo(now.minusMinutes(2));
        assertThat(response.getSize()).isEqualTo(2);
    }

    @Test
    @DisplayName("검색어 기준 이력 조회 결과가 size 이하면 hasNext=false와 null 커서를 반환한다")
    void searchHistoriesByQuery_withoutNextPage_returnsNullCursor() {
        // given
        User user = createUser();
        LocalDateTime now = LocalDateTime.of(2026, 4, 4, 20, 0);
        UserHistory first = createHistory(11L, now.minusMinutes(1), "노트북 A");
        UserHistory second = createHistory(10L, now.minusMinutes(2), "노트북 B");

        given(
                        userHistoryRepository.findHistorySliceByUserAndQuery(
                                eq(user), eq("노트북"), eq(null), eq(null), eq(PageRequest.of(0, 3))))
                .willReturn(List.of(first, second));

        // when
        UserHistoryResponse.HistorySliceResponse response =
                userHistoryService.searchHistoriesByQuery(user, "노트북", null, null, 2);

        // then
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getHistories()).hasSize(2);
        assertThat(response.getNextCursorId()).isNull();
        assertThat(response.getNextCursorCreatedAt()).isNull();
    }

    @Test
    @DisplayName("상품별 순위 이력 조회 시 엔티티를 응답 DTO로 변환한다")
    void getProductRankHistory_mapsEntitiesToDtos() {
        // given
        User user = createUser();
        LocalDateTime start = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 30, 23, 59);
        UserHistory first = createHistory(1L, LocalDateTime.of(2026, 4, 10, 10, 0), "A 상품");
        UserHistory second = createHistory(2L, LocalDateTime.of(2026, 4, 11, 10, 0), "B 상품");

        given(
                        userHistoryRepository
                                .findByUserAndProductIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                                        eq(user),
                                        eq("12345"),
                                        eq(start),
                                        eq(end),
                                        eq(PageRequest.of(0, 10))))
                .willReturn(List.of(first, second));

        // when
        List<UserHistoryResponse.HistoryDto> result =
                userHistoryService.getProductRankHistory(
                        user, "12345", start, end, PageRequest.of(0, 10));

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(UserHistoryResponse.HistoryDto::getId)
                .containsExactly(1L, 2L);
        assertThat(result)
                .extracting(UserHistoryResponse.HistoryDto::getTitle)
                .containsExactly("A 상품", "B 상품");
        assertThat(result)
                .extracting(UserHistoryResponse.HistoryDto::getCreatedAt)
                .containsExactly(
                        LocalDateTime.of(2026, 4, 10, 10, 0), LocalDateTime.of(2026, 4, 11, 10, 0));
    }

    private User createUser() {
        return User.builder()
                .email("tester@example.com")
                .password("encoded-password")
                .nickname("tester")
                .role(Role.USER)
                .build();
    }

    private SaveHistoryRequest createSaveHistoryRequest() {
        SaveHistoryRequest request = new SaveHistoryRequest();
        ReflectionTestUtils.setField(request, "query", "노트북");
        ReflectionTestUtils.setField(request, "title", "가성비 노트북");
        ReflectionTestUtils.setField(request, "lprice", 1000000);
        ReflectionTestUtils.setField(request, "hprice", 1200000);
        ReflectionTestUtils.setField(request, "mallName", "랭캣스토어");
        ReflectionTestUtils.setField(request, "brand", "RankCat");
        ReflectionTestUtils.setField(request, "maker", "RankCat");
        ReflectionTestUtils.setField(request, "productId", "12345");
        ReflectionTestUtils.setField(request, "productType", "GENERAL");
        ReflectionTestUtils.setField(request, "category1", "디지털/가전");
        ReflectionTestUtils.setField(request, "category2", "노트북");
        ReflectionTestUtils.setField(request, "category3", "노트북");
        ReflectionTestUtils.setField(request, "category4", "노트북");
        ReflectionTestUtils.setField(request, "predRank", 3.2);
        ReflectionTestUtils.setField(request, "predRankClipped", 3.0);
        return request;
    }

    private UserHistory createHistory(Long id, LocalDateTime createdAt, String title) {
        UserHistory history =
                UserHistory.builder()
                        .user(createUser())
                        .query("노트북")
                        .title(title)
                        .lprice(1000000)
                        .hprice(1200000)
                        .mallName("랭캣스토어")
                        .brand("RankCat")
                        .maker("RankCat")
                        .productId("12345")
                        .productType("GENERAL")
                        .category1("디지털/가전")
                        .category2("노트북")
                        .category3("노트북")
                        .category4("노트북")
                        .predRank(3.2)
                        .predRankClipped(3.0)
                        .build();
        ReflectionTestUtils.setField(history, "id", id);
        ReflectionTestUtils.setField(history, "createdAt", createdAt);
        return history;
    }
}
