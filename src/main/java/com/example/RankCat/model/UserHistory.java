package com.example.RankCat.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Entity
public class UserHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    //소유 사용자
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id",nullable = false)
    private User user;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 요청 필드
    private String query;
    private String title;
    private Integer lprice;
    private Integer hprice;
    private String mallName;
    private String brand;
    private String maker;
    private String productId;
    /** FastAPI 요청에서 문자열이므로 String 으로 저장 */
    private String productType;
    private String category1;
    private String category2;
    private String category3;
    private String category4;

    // 응답 필드
    private Double predRank;
    private Double predRankClipped;

    @Builder
    public UserHistory(User user,
                       String query, String title,
                       Integer lprice, Integer hprice,
                       String mallName, String brand, String maker,
                       String productId, String productType,
                       String category1, String category2, String category3, String category4,
                       Double predRank, Double predRankClipped) {
        this.user = user;
        this.query = query;
        this.title = title;
        this.lprice = lprice;
        this.hprice = hprice;
        this.mallName = mallName;
        this.brand = brand;
        this.maker = maker;
        this.productId = productId;
        this.productType = productType;
        this.category1 = category1;
        this.category2 = category2;
        this.category3 = category3;
        this.category4 = category4;
        this.predRank = predRank;
        this.predRankClipped = predRankClipped;
    }
}
