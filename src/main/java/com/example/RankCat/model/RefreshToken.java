package com.example.RankCat.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id",updatable = false)
    private Long id;

    @Column(name="user_id",nullable = false,updatable = true)
    private Long userId;

    @Column(name="refresh_token",nullable = false)
    private String refreshToken;

    public RefreshToken(Long userId, String refreshToken) {
        this.userId = userId;
        this.refreshToken = refreshToken;
    }
    public RefreshToken update(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}
