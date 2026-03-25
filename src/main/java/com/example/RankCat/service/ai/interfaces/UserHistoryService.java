package com.example.RankCat.service.ai.interfaces;

import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserHistoryService {
    UserHistory save(User user, SaveHistoryRequest req);

    // 성능 테스트: 페이징 조회
    Page<UserHistoryResponse.HistoryDto> getUserHistoriesWithPaging(User user, Pageable pageable);

    // 성능 테스트: 특정 상품 추이 (User와 Pageable 추가)
    List<UserHistoryResponse.HistoryDto> getProductRankHistory(
            User user, String productId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 성능 테스트: 키워드 검색
    Page<UserHistoryResponse.HistoryDto> searchHistoriesByQuery(
            User user, String query, Pageable pageable);
}
