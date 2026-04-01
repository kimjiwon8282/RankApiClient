package com.example.RankCat.service.ai.impl;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import com.example.RankCat.repository.UserHistoryRepository;
import com.example.RankCat.service.ai.interfaces.UserHistoryService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserHistoryServiceImpl implements UserHistoryService {

    private final UserHistoryRepository userHistoryRepository;

    @Transactional
    @Override
    public UserHistory save(User user, SaveHistoryRequest r) {
        UserHistory history =
                UserHistory.builder()
                        .user(user)
                        .query(r.getQuery())
                        .title(r.getTitle())
                        .lprice(r.getLprice())
                        .hprice(r.getHprice())
                        .mallName(r.getMallName())
                        .brand(r.getBrand())
                        .maker(r.getMaker())
                        .productId(r.getProductId())
                        .productType(r.getProductType())
                        .category1(r.getCategory1())
                        .category2(r.getCategory2())
                        .category3(r.getCategory3())
                        .category4(r.getCategory4())
                        .predRank(r.getPredRank())
                        .predRankClipped(r.getPredRankClipped())
                        .build();

        return userHistoryRepository.save(history);
    }

    @Transactional(readOnly = true)
    @Override
    public UserHistoryResponse.HistorySliceResponse getUserHistoriesWithCursor(
            User user, LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        validateCursor(cursorCreatedAt, cursorId);

        List<UserHistory> histories =
                userHistoryRepository.findHistorySliceByUser(
                        user, cursorCreatedAt, cursorId, PageRequest.of(0, size + 1));

        return toSliceResponse(histories, size);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserHistoryResponse.HistoryDto> getProductRankHistory(
            User user,
            String productId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {
        return userHistoryRepository
                .findByUserAndProductIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                        user, productId, start, end, pageable)
                .stream()
                .map(UserHistoryResponse.HistoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public UserHistoryResponse.HistorySliceResponse searchHistoriesByQuery(
            User user, String query, LocalDateTime cursorCreatedAt, Long cursorId, int size) {
        validateCursor(cursorCreatedAt, cursorId);

        List<UserHistory> histories =
                userHistoryRepository.findHistorySliceByUserAndQuery(
                        user, query, cursorCreatedAt, cursorId, PageRequest.of(0, size + 1));

        return toSliceResponse(histories, size);
    }

    private void validateCursor(LocalDateTime cursorCreatedAt, Long cursorId) {
        boolean hasCreatedAt = cursorCreatedAt != null;
        boolean hasId = cursorId != null;

        if (hasCreatedAt != hasId) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private UserHistoryResponse.HistorySliceResponse toSliceResponse(
            List<UserHistory> histories, int size) {
        boolean hasNext = histories.size() > size;
        List<UserHistory> currentSlice = hasNext ? histories.subList(0, size) : histories;

        LocalDateTime nextCursorCreatedAt = null;
        Long nextCursorId = null;

        if (hasNext && !currentSlice.isEmpty()) {
            UserHistory lastHistory = currentSlice.get(currentSlice.size() - 1);
            nextCursorCreatedAt = lastHistory.getCreatedAt();
            nextCursorId = lastHistory.getId();
        }

        return UserHistoryResponse.HistorySliceResponse.builder()
                .histories(
                        currentSlice.stream()
                                .map(UserHistoryResponse.HistoryDto::fromEntity)
                                .toList())
                .hasNext(hasNext)
                .nextCursorCreatedAt(nextCursorCreatedAt)
                .nextCursorId(nextCursorId)
                .size(size)
                .build();
    }
}
