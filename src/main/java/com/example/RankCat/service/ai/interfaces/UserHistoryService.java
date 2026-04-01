package com.example.RankCat.service.ai.interfaces;

import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface UserHistoryService {
    UserHistory save(User user, SaveHistoryRequest req);

    UserHistoryResponse.HistorySliceResponse getUserHistoriesWithCursor(
            User user, LocalDateTime cursorCreatedAt, Long cursorId, int size);

    List<UserHistoryResponse.HistoryDto> getProductRankHistory(
            User user, String productId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    UserHistoryResponse.HistorySliceResponse searchHistoriesByQuery(
            User user, String query, LocalDateTime cursorCreatedAt, Long cursorId, int size);
}
