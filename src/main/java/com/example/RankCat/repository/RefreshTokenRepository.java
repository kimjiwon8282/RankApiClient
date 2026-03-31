package com.example.RankCat.repository;

import com.example.RankCat.model.RefreshToken;
import com.example.RankCat.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // [실행 쿼리]
    // SELECT * FROM refresh_token WHERE refresh_token = ?
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    // [실행 쿼리]
    // SELECT * FROM refresh_token WHERE user_id = ?
    // 파라미터로 넘긴 User 객체의 PK(id)를 추출하여, 해당 유저의 토큰을 찾습니다.
    Optional<RefreshToken> findByUser(User user);

    // [토큰 재발급용] 새 액세스 토큰을 만들기 위해 User 정보가 무조건 필요할 때 (FETCH JOIN으로 한 방 쿼리)
    @Query(
            "SELECT r FROM RefreshToken r JOIN FETCH r.user WHERE r.refreshToken = :refreshTokenHash")
    Optional<RefreshToken> findWithUserByRefreshToken(String refreshTokenHash);
}
