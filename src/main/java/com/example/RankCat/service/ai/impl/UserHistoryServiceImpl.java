package com.example.RankCat.service.ai.impl;

import com.example.RankCat.dto.SaveHistoryRequest;
import com.example.RankCat.dto.UserHistoryResponse;
import com.example.RankCat.model.User;
import com.example.RankCat.model.UserHistory;
import com.example.RankCat.repository.UserHistoryRepository;
import com.example.RankCat.service.ai.interfaces.UserHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHistoryServiceImpl implements UserHistoryService {
    private final UserHistoryRepository userHistoryRepository;

    @Transactional
    @Override
    public UserHistory save(User user, SaveHistoryRequest r) {
        UserHistory history = UserHistory.builder()
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
    public UserHistoryResponse getUserHistories(User user) {
        List<UserHistory> histories = userHistoryRepository.findAllByUserOrderByCreatedAtDesc(user);

        List<UserHistoryResponse.HistoryDto> historyDtos = histories.stream()
                .map(UserHistoryResponse.HistoryDto::fromEntity)
                .toList();

        return UserHistoryResponse.builder()
                .nickname(user.getNickname())   // AuthenticationPrincipal에서 넘어온 user의 닉네임
                .histories(historyDtos)
                .build();
    }
}
