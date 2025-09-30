package com.example.RankCat.dto.ai;

import com.example.RankCat.model.UserHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserHistoryResponse {

    private String nickname;                  // 사용자 닉네임 (최상단에 1번만 표시)
    private List<HistoryDto> histories;       // 히스토리 목록

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class HistoryDto {
        private Long id;
        private String query;
        private String title;
        private Integer lprice;
        private Integer hprice;
        private String mallName;
        private String brand;
        private String maker;
        private String productId;
        private String productType;
        private String category1;
        private String category2;
        private String category3;
        private String category4;
        private Double predRank;
        private Double predRankClipped;
        private LocalDateTime createdAt;

        public static HistoryDto fromEntity(UserHistory entity) {
            return HistoryDto.builder()
                    .id(entity.getId())
                    .query(entity.getQuery())
                    .title(entity.getTitle())
                    .lprice(entity.getLprice())
                    .hprice(entity.getHprice())
                    .mallName(entity.getMallName())
                    .brand(entity.getBrand())
                    .maker(entity.getMaker())
                    .productId(entity.getProductId())
                    .productType(entity.getProductType())
                    .category1(entity.getCategory1())
                    .category2(entity.getCategory2())
                    .category3(entity.getCategory3())
                    .category4(entity.getCategory4())
                    .predRank(entity.getPredRank())
                    .predRankClipped(entity.getPredRankClipped())
                    .createdAt(entity.getCreatedAt())
                    .build();
        }
    }
}
