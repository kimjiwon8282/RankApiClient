package com.example.RankCat.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "user_history",
        indexes = {
            @Index(name = "idx_user_query_created", columnList = "user_id, query, created_at, id"),
            @Index(name = "idx_user_created", columnList = "user_id, created_at, id"),
            @Index(
                    name = "idx_user_product_created",
                    columnList = "user_id, product_id, created_at")
        })
public class UserHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String query;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(nullable = false)
    private String category1;

    @Column(nullable = false)
    private String category2;

    @Column(nullable = false)
    private String category3;

    @Column(nullable = false)
    private String category4;

    @Column(nullable = false)
    private Double predRank;

    @Column(nullable = false)
    private Double predRankClipped;

    @Column private Integer lprice;

    @Column private Integer hprice;

    @Column private String mallName;

    @Column private String brand;

    @Column private String maker;

    @Column private String productId;

    @Column private String productType;

    @Builder
    public UserHistory(
            User user,
            String query,
            String title,
            Integer lprice,
            Integer hprice,
            String mallName,
            String brand,
            String maker,
            String productId,
            String productType,
            String category1,
            String category2,
            String category3,
            String category4,
            Double predRank,
            Double predRankClipped) {
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
