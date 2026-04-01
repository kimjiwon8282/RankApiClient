package com.example.RankCat.service.api.impl;

import com.example.RankCat.client.naver.NaverSearchAdKeywordClient;
import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.dto.api.KeywordRecommendResponse;
import com.example.RankCat.model.SearchAdKeywordItem;
import com.example.RankCat.model.SearchAdKeywordResult;
import com.example.RankCat.repository.SearchAdKeywordRepository;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class KeywordToolServiceImpl implements KeywordToolService {

    private final NaverSearchAdKeywordClient naverSearchAdKeywordClient;
    private final SearchAdKeywordRepository keywordRepository;

    @Override
    public void collectRelatedKeywords(String hint) {
        log.info("keyword={} API 호출 중...", hint);
        List<SearchAdKeywordItem> keywordList =
                naverSearchAdKeywordClient.fetchRelatedKeywords(hint).getKeywordList();

        SearchAdKeywordResult result = new SearchAdKeywordResult();
        result.setKeyword(hint);
        result.setRelatedKeywords(keywordList);
        result.setCallAt(System.currentTimeMillis());

        keywordRepository.save(result);
        log.info("keyword={} 저장 완료 (관련키워드 수={})", hint, keywordList.size());
    }

    @Override
    public KeywordRecommendResponse recommend(String hint, int limit) {
        SearchAdKeywordResult doc = keywordRepository.findById(hint).orElse(null);
        if (doc == null || doc.getRelatedKeywords() == null) {
            log.warn("hint={} 에 대한 추천 키워드 없음", hint);
            return KeywordRecommendResponse.builder().hint(hint).recommended(List.of()).build();
        }

        List<String> top =
                doc.getRelatedKeywords().stream()
                        .limit(limit)
                        .map(SearchAdKeywordItem::getRelKeyword)
                        .collect(Collectors.toList());

        return KeywordRecommendResponse.builder().hint(hint).recommended(top).build();
    }

    @Override
    public SearchAdKeywordResult getKeywordAnalysis(String query) {
        log.info("DB에서 키워드 분석 데이터 조회: query={}", query);
        return keywordRepository
                .findById(query)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND));
    }
}
