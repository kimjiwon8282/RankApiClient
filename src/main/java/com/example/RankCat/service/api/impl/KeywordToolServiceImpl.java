package com.example.RankCat.service.api.impl;

import com.example.RankCat.dto.api.KeywordRecommendResponse;
import com.example.RankCat.model.SearchAdKeywordResult;
import com.example.RankCat.repository.SearchAdKeywordRepository;
import com.example.RankCat.service.api.interfaces.KeywordToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class KeywordToolServiceImpl implements KeywordToolService {

    private final RestTemplate naverRestTemplate;
    private final SearchAdKeywordRepository keywordRepository;

    @Override
    @SuppressWarnings("unchecked")//맵에서 꺼는 값을 제네릭 캐스팅 할때 발생하는 경고 무시
    public List<Map<String, Object>> getRelatedKeywords(String hint) {
        log.info("keyword={} API 호출 중...", hint);
        String path = "/keywordstool?hintKeywords={hint}&showDetail=1"; //base url은 RestTemplate에 이미 설정. 경로만 작성
        Map<String, Object> resp = naverRestTemplate //RestTemplate로 GET요청 전송
                .exchange(path, HttpMethod.GET, null, Map.class, hint)//Map.class -> json전체를 Map<>으로 파싱함
                .getBody();

        List<Map<String, Object>> keywordList = (List<Map<String, Object>>)
                resp.getOrDefault("keywordList", List.of());

        SearchAdKeywordResult result = new SearchAdKeywordResult();
        result.setKeyword(hint);  // keyword를 _id로 사용
        result.setRelatedKeywords(keywordList);
        result.setCallAt(System.currentTimeMillis());

        keywordRepository.save(result); //keyword가 동일하면 자동 덮어 쓰기
        log.info("keyword={} 저장 완료 (관련키워드 수={})", hint, keywordList.size());

        return keywordList;
    }

    @Override
    public KeywordRecommendResponse recommend(String hint, int limit) {
        SearchAdKeywordResult doc = keywordRepository.findById(hint).orElse(null);
        if (doc == null || doc.getRelatedKeywords() == null) {
            log.warn("hint={} 에 대한 추천 키워드 없음", hint);
            return KeywordRecommendResponse.builder()
                    .hint(hint)
                    .recommended(List.of())
                    .build();
        }

        List<String> top = doc.getRelatedKeywords().stream()
                .limit(limit)
                .map(m -> String.valueOf(m.get("relKeyword")))
                .collect(Collectors.toList());

        return KeywordRecommendResponse.builder()
                .hint(hint)
                .recommended(top)
                .build();
    }
}