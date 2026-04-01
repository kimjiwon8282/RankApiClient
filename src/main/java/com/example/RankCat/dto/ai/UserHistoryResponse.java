package com.example.RankCat.dto.ai;

import com.example.RankCat.model.UserHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "사용자 분석 이력 응답 객체")
public class UserHistoryResponse {
    @Schema(description = "사용자 닉네임 (화면 상단 노출용)", example = "지원개발자")
    private String nickname;

    @Schema(description = "히스토리 목록")
    private List<HistoryDto> histories;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "개별 히스토리 상세 정보")
    public static class HistoryDto {
        @Schema(description = "히스토리 DB ID", example = "101")
        private Long id;

        @Schema(description = "검색 쿼리", example = "가구바퀴")
        private String query;

        @Schema(description = "상품 제목", example = "무소음 우레탄 바퀴 4개입")
        private String title;

        @Schema(description = "최저가", example = "12000")
        private Integer lprice;

        @Schema(description = "최고가", example = "15000")
        private Integer hprice;

        @Schema(description = "쇼핑몰 이름", example = "네이버 스마트스토어")
        private String mallName;

        @Schema(description = "브랜드", example = "지완퍼니처")
        private String brand;

        @Schema(description = "제조사", example = "지완공업")
        private String maker;

        @Schema(description = "네이버 상품 고유 ID", example = "123456789")
        private String productId;

        @Schema(description = "상품 타입 (FastAPI 수신값)", example = "2")
        private String productType;

        @Schema(description = "카테고리 1(대)", example = "가구/인테리어")
        private String category1;

        @Schema(description = "카테고리 2(중)", example = "DIY자재/용품")
        private String category2;

        @Schema(description = "카테고리 3(소)", example = "가구부속품")
        private String category3;

        @Schema(description = "카테고리 4(세)", example = "가구바퀴")
        private String category4;

        @Schema(description = "예측 랭킹 점수", example = "15.42")
        private Double predRank;

        @Schema(description = "보정된 예측 랭킹 점수", example = "15.0")
        private Double predRankClipped;

        @Schema(description = "기록 생성 일시", example = "2026-03-02T17:00:00")
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

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Schema(description = "키셋 페이지네이션 기반 분석 이력 응답")
    public static class HistorySliceResponse {
        @Schema(description = "현재 응답에 포함된 히스토리 목록")
        private List<HistoryDto> histories;

        @Schema(description = "다음 묶음 존재 여부", example = "true")
        private boolean hasNext;

        @Schema(
                description = "다음 요청에 넘길 createdAt 커서. hasNext=false면 null입니다.",
                example = "2026-03-30T12:34:56")
        private LocalDateTime nextCursorCreatedAt;

        @Schema(description = "다음 요청에 넘길 ID 커서. hasNext=false면 null입니다.", example = "120")
        private Long nextCursorId;

        @Schema(description = "요청 size", example = "20")
        private int size;
    }
}
