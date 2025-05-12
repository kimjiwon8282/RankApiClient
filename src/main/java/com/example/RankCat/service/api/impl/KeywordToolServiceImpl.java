package com.example.RankCat.service.api.impl;

import com.example.RankCat.service.api.interfaces.KeywordToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeywordToolServiceImpl implements KeywordToolService {

    private final RestTemplate naverRestTemplate;

    @Override
    @SuppressWarnings("unchecked")//맵에서 꺼는 값을 제네릭 캐스팅 할때 발생하는 경고 무시
    public List<Map<String, Object>> getRelatedKeywords(String hint) {
        String path = "/keywordstool?hintKeywords={hint}&showDetail=1"; //base url은 RestTemplate에 이미 설정. 경로만 작성
        Map<String, Object> resp = naverRestTemplate //RestTemplate로 GET요청 전송
                .exchange(path, HttpMethod.GET, null, Map.class, hint)//Map.class -> json전체를 Map<>으로 파싱함
                .getBody();
        return (List<Map<String, Object>>) resp.getOrDefault("keywordList", List.of());
    }
}