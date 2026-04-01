package com.example.RankCat.client.naver;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.model.SearchAdKeywordItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NaverSearchAdKeywordClient {

    private final RestTemplate naverRestTemplate;

    public NaverSearchAdKeywordClient(
            @Qualifier("naverRestTemplate") RestTemplate naverRestTemplate) {
        this.naverRestTemplate = naverRestTemplate;
    }

    public SearchAdKeywordApiResponse fetchRelatedKeywords(String hint) {
        String path = "/keywordstool?hintKeywords={hint}&showDetail=1";
        try {
            SearchAdKeywordApiResponse response =
                    naverRestTemplate
                            .exchange(
                                    path,
                                    HttpMethod.GET,
                                    null,
                                    SearchAdKeywordApiResponse.class,
                                    hint)
                            .getBody();
            return response != null ? response : emptyResponse();
        } catch (HttpClientErrorException.Unauthorized e) {
            throw externalException(ErrorCode.EXTERNAL_API_UNAUTHORIZED, "fetchRelatedKeywords", e);
        } catch (ResourceAccessException e) {
            throw externalException(ErrorCode.EXTERNAL_API_TIMEOUT, "fetchRelatedKeywords", e);
        } catch (HttpClientErrorException e) {
            throw externalException(ErrorCode.EXTERNAL_API_CLIENT_ERROR, "fetchRelatedKeywords", e);
        } catch (HttpServerErrorException e) {
            throw externalException(ErrorCode.EXTERNAL_API_SERVER_ERROR, "fetchRelatedKeywords", e);
        } catch (RestClientException e) {
            throw externalException(ErrorCode.EXTERNAL_API_ERROR, "fetchRelatedKeywords", e);
        }
    }

    private SearchAdKeywordApiResponse emptyResponse() {
        SearchAdKeywordApiResponse response = new SearchAdKeywordApiResponse();
        response.setKeywordList(List.of());
        return response;
    }

    private BusinessException externalException(ErrorCode errorCode, String action, Exception e) {
        log.error("Naver API 호출 실패 ({}): {}", action, e.getMessage());
        return new BusinessException(errorCode, e.getMessage(), e);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchAdKeywordApiResponse {
        private List<SearchAdKeywordItem> keywordList = new ArrayList<>();
    }
}
