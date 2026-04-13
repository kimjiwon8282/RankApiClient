package com.example.RankCat.service.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.RankCat.client.naver.NaverShopSearchClient;
import com.example.RankCat.client.naver.NaverShoppingInsightClient;
import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.dto.api.KeywordTrendResponseDto;
import com.example.RankCat.dto.api.ShopSearchTrendResponseDto;
import com.example.RankCat.model.ShopSearchTrendItem;
import com.example.RankCat.model.ShopSearchTrendResult;
import com.example.RankCat.model.ShoppingInsightCategoryResult;
import com.example.RankCat.model.ShoppingInsightKeywordResult;
import com.example.RankCat.model.ShoppingInsightTrendResponse;
import com.example.RankCat.repository.ShopSearchTrendResultRepository;
import com.example.RankCat.repository.ShoppingInsightCategoryRepository;
import com.example.RankCat.repository.ShoppingInsightKeywordRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShoppingInsightServiceImplTest {

    @Mock private NaverShoppingInsightClient shoppingInsightClient;
    @Mock private NaverShopSearchClient shopSearchClient;
    @Mock private ShoppingInsightCategoryRepository categoryRepository;
    @Mock private ShoppingInsightKeywordRepository keywordRepository;
    @Mock private ShopSearchTrendResultRepository trendRepository;

    @InjectMocks private ShoppingInsightServiceImpl shoppingInsightService;

    @Captor private ArgumentCaptor<ShopSearchTrendResult> trendResultCaptor;
    @Captor private ArgumentCaptor<ShoppingInsightCategoryResult> categoryResultCaptor;

    @Test
    @DisplayName("쇼핑 검색 수집 시 2페이지까지 병합하고 중복 제거 후 rank를 다시 매긴다")
    void collectShopSearchTrend_mergesSecondPageDedupesAndReranks() {
        // given
        ShopSearchTrendItem item1 = item("p1", "A", "MallA", "link-a");
        ShopSearchTrendItem item2 = item("p2", "B", "MallB", "link-b");
        ShopSearchTrendItem duplicated = item("p2", "B-dup", "MallB", "link-b");
        ShopSearchTrendItem item3 = item("p3", "C", "MallC", "link-c");

        NaverShopSearchClient.NaverShopSearchResponse page1 =
                response(150, 1, 100, List.of(item1, item2));
        NaverShopSearchClient.NaverShopSearchResponse page2 =
                response(150, 101, 100, List.of(duplicated, item3));

        given(shopSearchClient.search("노트북", 1, 100)).willReturn(page1);
        given(shopSearchClient.search("노트북", 101, 100)).willReturn(page2);

        // when
        shoppingInsightService.collectShopSearchTrend("노트북");

        // then
        verify(shopSearchClient).search("노트북", 1, 100);
        verify(shopSearchClient).search("노트북", 101, 100);
        verify(trendRepository).save(trendResultCaptor.capture());

        ShopSearchTrendResult saved = trendResultCaptor.getValue();
        assertThat(saved.getId()).isEqualTo("노트북");
        assertThat(saved.getItems()).hasSize(3);
        assertThat(saved.getItems())
                .extracting(ShopSearchTrendItem::getProductId)
                .containsExactly("p1", "p2", "p3");
        assertThat(saved.getItems())
                .extracting(ShopSearchTrendItem::getRank)
                .containsExactly(1, 2, 3);
    }

    @Test
    @DisplayName("productId가 없으면 title, mallName, link 조합으로 중복을 제거한다")
    void collectShopSearchTrend_dedupesByFallbackKeyWhenProductIdIsBlank() {
        // given
        ShopSearchTrendItem item1 = item("   ", "같은상품", "같은몰", "same-link");
        ShopSearchTrendItem item2 = item(null, "같은상품", "같은몰", "same-link");
        ShopSearchTrendItem item3 = item(null, "다른상품", "다른몰", "other-link");

        given(shopSearchClient.search("키워드", 1, 100))
                .willReturn(response(3, 1, 100, List.of(item1, item2, item3)));

        // when
        shoppingInsightService.collectShopSearchTrend("키워드");

        // then
        verify(trendRepository).save(trendResultCaptor.capture());
        ShopSearchTrendResult saved = trendResultCaptor.getValue();
        assertThat(saved.getItems()).hasSize(2);
        assertThat(saved.getItems())
                .extracting(ShopSearchTrendItem::getTitle)
                .containsExactly("같은상품", "다른상품");
        assertThat(saved.getItems()).extracting(ShopSearchTrendItem::getRank).containsExactly(1, 2);
    }

    @Test
    @DisplayName("월간 카테고리 트렌드 수집 시 기존 문서에 월간 필드만 갱신한다")
    void collectCategoryTrend_month_updatesMonthlyFieldsOnExistingDocument() {
        // given
        ShoppingInsightTrendResponse response = new ShoppingInsightTrendResponse();
        response.setStartDate("2025-01-01");
        response.setEndDate("2025-12-31");
        response.setTimeUnit("month");

        ShoppingInsightCategoryResult existing = new ShoppingInsightCategoryResult();
        existing.setId("50003308");
        existing.setCategoryName("가구바퀴");
        ShoppingInsightTrendResponse weeklyResponse = new ShoppingInsightTrendResponse();
        weeklyResponse.setTimeUnit("week");
        existing.setWeeklyResponse(weeklyResponse);
        existing.setWeeklyCallAt(12345L);
        existing.setStartDate_w("2025-03-01");
        existing.setEndDate_w("2025-03-31");

        given(
                        shoppingInsightClient.fetchCategoryTrend(
                                "2025-01-01", "2025-12-31", "month", "가구바퀴", "50003308"))
                .willReturn(response);
        given(categoryRepository.findById("50003308")).willReturn(Optional.of(existing));

        // when
        shoppingInsightService.collectCategoryTrend(
                "2025-01-01", "2025-12-31", "month", "가구바퀴", "50003308");

        // then
        verify(categoryRepository).save(categoryResultCaptor.capture());
        ShoppingInsightCategoryResult saved = categoryResultCaptor.getValue();

        assertThat(saved.getId()).isEqualTo("50003308");
        assertThat(saved.getCategoryName()).isEqualTo("가구바퀴");
        assertThat(saved.getMonthlyResponse()).isSameAs(response);
        assertThat(saved.getStartDate_m()).isEqualTo("2025-01-01");
        assertThat(saved.getEndDate_m()).isEqualTo("2025-12-31");

        assertThat(saved.getWeeklyResponse()).isSameAs(weeklyResponse);
        assertThat(saved.getWeeklyCallAt()).isEqualTo(12345L);
        assertThat(saved.getStartDate_w()).isEqualTo("2025-03-01");
        assertThat(saved.getEndDate_w()).isEqualTo("2025-03-31");
    }

    @Test
    @DisplayName("키워드 트렌드 스냅샷 조회 시 키워드를 정렬한 ID로 조회한다")
    void getKeywordTrendSnapshot_sortsKeywordsBeforeLookup() {
        // given
        ShoppingInsightKeywordResult entity = new ShoppingInsightKeywordResult();
        entity.setId("50003308_노트북_맥북");
        entity.setCategoryCode("50003308");
        entity.setKeywords(List.of("맥북", "노트북"));
        entity.setTimeUnit("week");
        entity.setStartDate("2025-01-01");
        entity.setEndDate("2025-01-31");

        given(keywordRepository.findById("50003308_노트북_맥북")).willReturn(Optional.of(entity));

        // when
        KeywordTrendResponseDto result =
                shoppingInsightService.getKeywordTrendSnapshot("50003308", List.of("맥북", "노트북"));

        // then
        assertThat(result.getId()).isEqualTo("50003308_노트북_맥북");
        assertThat(result.getCategoryCode()).isEqualTo("50003308");
        verify(keywordRepository).findById("50003308_노트북_맥북");
    }

    @Test
    @DisplayName("쇼핑 검색 스냅샷이 없으면 DATA_NOT_FOUND를 던진다")
    void getShopSearchTrendSnapshot_notFound_throwsDataNotFound() {
        // given
        given(trendRepository.findById("없는검색어")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> shoppingInsightService.getShopSearchTrendSnapshot("없는검색어"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DATA_NOT_FOUND);
    }

    @Test
    @DisplayName("쇼핑 검색 스냅샷 조회 시 엔티티를 DTO로 변환한다")
    void getShopSearchTrendSnapshot_success_mapsToDto() {
        // given
        ShopSearchTrendResult entity = new ShopSearchTrendResult();
        entity.setId("가구바퀴");
        entity.setItems(List.of(item("p1", "상품1", "몰1", "l1")));
        entity.setCallAt(1000L);
        entity.setExpiresAt(0L);
        entity.setSource("NAVER_SHOPPING_SEARCH");

        given(trendRepository.findById("가구바퀴")).willReturn(Optional.of(entity));

        // when
        ShopSearchTrendResponseDto result =
                shoppingInsightService.getShopSearchTrendSnapshot("가구바퀴");

        // then
        assertThat(result.getQuery()).isEqualTo("가구바퀴");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getCallAt()).isEqualTo(1000L);
        assertThat(result.getSource()).isEqualTo("NAVER_SHOPPING_SEARCH");
        assertThat(result.isFresh()).isFalse();
    }

    private NaverShopSearchClient.NaverShopSearchResponse response(
            int total, int start, int display, List<ShopSearchTrendItem> items) {
        NaverShopSearchClient.NaverShopSearchResponse response =
                new NaverShopSearchClient.NaverShopSearchResponse();
        response.setTotal(total);
        response.setStart(start);
        response.setDisplay(display);
        response.setItems(items);
        return response;
    }

    private ShopSearchTrendItem item(String productId, String title, String mallName, String link) {
        ShopSearchTrendItem item = new ShopSearchTrendItem();
        item.setProductId(productId);
        item.setTitle(title);
        item.setMallName(mallName);
        item.setLink(link);
        return item;
    }
}
