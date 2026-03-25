package com.example.RankCat.service.ai.impl;

import com.example.RankCat.dto.ai.SaveHistoryRequest;
import com.example.RankCat.dto.ai.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import com.example.RankCat.repository.UserHistoryRepository;
import com.example.RankCat.service.ai.interfaces.UserHistoryService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Page<UserHistoryResponse.HistoryDto> getUserHistoriesWithPaging(
            User user, Pageable pageable) {
        // [성능 테스트 포인트] 인덱스가 없을 때 데이터가 많아지면 Page 객체를 만들기 위한
        // 내부적 'Count 쿼리'가 전체 테이블을 스캔하며 매우 느려집니다.
        Page<UserHistory> histories =
                userHistoryRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        return histories.map(UserHistoryResponse.HistoryDto::fromEntity);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserHistoryResponse.HistoryDto> getProductRankHistory(
            User user,
            String productId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable) {
        // 인덱스/파티셔닝 미적용 시: 테이블 전체를 풀스캔하며 user_id, productId와 기간을 대조합니다.
        // List로 반환하지만, 내부적으로는 페이징 처리를 통해 힙 메모리 폭주를 방지합니다.
        return userHistoryRepository
                .findByUserAndProductIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                        user, productId, start, end, pageable)
                .stream()
                .map(UserHistoryResponse.HistoryDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserHistoryResponse.HistoryDto> searchHistoriesByQuery(
            User user, String query, Pageable pageable) {
        // 복합 인덱스 미적용 시: user_id와 query를 모두 비교하기 위해 상당한 I/O가 발생합니다.
        return userHistoryRepository
                .findByUserAndQuery(user, query, pageable)
                .map(UserHistoryResponse.HistoryDto::fromEntity);
    }
}
