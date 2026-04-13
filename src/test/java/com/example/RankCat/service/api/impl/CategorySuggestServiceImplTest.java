package com.example.RankCat.service.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.example.RankCat.dto.api.CategorySuggestResponse;
import com.example.RankCat.model.ShopSearchTrendItem;
import com.example.RankCat.model.ShopSearchTrendResult;
import com.example.RankCat.repository.ShopSearchTrendResultRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategorySuggestServiceImplTest {

    @Mock private ShopSearchTrendResultRepository repo;

    @InjectMocks private CategorySuggestServiceImpl categorySuggestService;

    @Test
    @DisplayName("수집 문서가 없으면 source=none과 null 추천값을 반환한다")
    void suggestByQuery_notFound_returnsNone() {
        // given
        given(repo.findById("노트북")).willReturn(Optional.empty());

        // when
        CategorySuggestResponse response = categorySuggestService.suggestByQuery("노트북", 5);

        // then
        assertThat(response.getSource()).isEqualTo("none");
        assertThat(response.getRecommended()).isNull();
        assertThat(response.getCallAt()).isNull();
    }

    @Test
    @DisplayName("1위 상품의 카테고리 경로가 완전하면 rank1 추천을 반환한다")
    void suggestByQuery_rank1Complete_returnsRank1() {
        // given
        ShopSearchTrendResult doc = new ShopSearchTrendResult();
        doc.setId("노트북");
        doc.setCallAt(1712000000000L);
        doc.setItems(List.of(item(1, "디지털/가전", "노트북", "노트북", "노트북")));
        given(repo.findById("노트북")).willReturn(Optional.of(doc));

        // when
        CategorySuggestResponse response = categorySuggestService.suggestByQuery("노트북", 5);

        // then
        assertThat(response.getSource()).isEqualTo("rank1");
        assertThat(response.getCallAt()).isEqualTo(1712000000000L);
        assertThat(response.getRecommended().getCategory1()).isEqualTo("디지털/가전");
        assertThat(response.getRecommended().getCategory2()).isEqualTo("노트북");
        assertThat(response.getRecommended().getCategory3()).isEqualTo("노트북");
        assertThat(response.getRecommended().getCategory4()).isEqualTo("노트북");
    }

    @Test
    @DisplayName("1위 카테고리가 불완전하면 topN 내 완전한 카테고리로 majority 추천을 반환한다")
    void suggestByQuery_rank1Incomplete_returnsMajority() {
        // given
        ShopSearchTrendResult doc = new ShopSearchTrendResult();
        doc.setId("노트북");
        doc.setCallAt(1712000000000L);
        doc.setItems(
                List.of(
                        item(1, "디지털/가전", "노트북", "노트북", null),
                        item(2, "디지털/가전", "노트북", "노트북", "게이밍노트북"),
                        item(3, null, null, null, null)));
        given(repo.findById("노트북")).willReturn(Optional.of(doc));

        // when
        CategorySuggestResponse response = categorySuggestService.suggestByQuery("노트북", 3);

        // then
        assertThat(response.getSource()).isEqualTo("majority");
        assertThat(response.getRecommended().getCategory1()).isEqualTo("디지털/가전");
        assertThat(response.getRecommended().getCategory2()).isEqualTo("노트북");
        assertThat(response.getRecommended().getCategory3()).isEqualTo("노트북");
        assertThat(response.getRecommended().getCategory4()).isEqualTo("게이밍노트북");
    }

    @Test
    @DisplayName("완전한 카테고리 경로가 하나도 없으면 source=none을 반환한다")
    void suggestByQuery_noCompleteCategory_returnsNone() {
        // given
        ShopSearchTrendResult doc = new ShopSearchTrendResult();
        doc.setId("노트북");
        doc.setCallAt(1712000000000L);
        doc.setItems(
                List.of(item(1, "디지털/가전", "노트북", null, null), item(2, null, null, null, null)));
        given(repo.findById("노트북")).willReturn(Optional.of(doc));

        // when
        CategorySuggestResponse response = categorySuggestService.suggestByQuery("노트북", 3);

        // then
        assertThat(response.getSource()).isEqualTo("none");
        assertThat(response.getCallAt()).isEqualTo(1712000000000L);
        assertThat(response.getRecommended()).isNull();
    }

    private ShopSearchTrendItem item(
            Integer rank, String category1, String category2, String category3, String category4) {
        ShopSearchTrendItem item = new ShopSearchTrendItem();
        item.setRank(rank);
        item.setCategory1(category1);
        item.setCategory2(category2);
        item.setCategory3(category3);
        item.setCategory4(category4);
        return item;
    }
}
