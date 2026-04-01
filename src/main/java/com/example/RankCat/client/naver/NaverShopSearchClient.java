package com.example.RankCat.client.naver;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.model.ShopSearchTrendItem;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NaverShopSearchClient {

    private final RestTemplate shoppingInsightRestTemplate;

    public NaverShopSearchClient(
            @Qualifier("shoppingInsightRestTemplate") RestTemplate shoppingInsightRestTemplate) {
        this.shoppingInsightRestTemplate = shoppingInsightRestTemplate;
    }

    public NaverShopSearchResponse search(String query, int start, int display) {
        String url = "/v1/search/shop?query={query}&display={display}&start={start}";
        Map<String, Object> uriVars = new HashMap<>();
        uriVars.put("query", query);
        uriVars.put("display", Math.min(display, 100));
        uriVars.put("start", start);
        try {
            NaverShopSearchResponse response =
                    shoppingInsightRestTemplate.getForObject(
                            url, NaverShopSearchResponse.class, uriVars);
            return response != null ? response : emptyResponse(start, display);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw externalException(ErrorCode.EXTERNAL_API_UNAUTHORIZED, "searchShop", e);
        } catch (ResourceAccessException e) {
            throw externalException(ErrorCode.EXTERNAL_API_TIMEOUT, "searchShop", e);
        } catch (HttpClientErrorException e) {
            throw externalException(ErrorCode.EXTERNAL_API_CLIENT_ERROR, "searchShop", e);
        } catch (HttpServerErrorException e) {
            throw externalException(ErrorCode.EXTERNAL_API_SERVER_ERROR, "searchShop", e);
        } catch (RestClientException e) {
            throw externalException(ErrorCode.EXTERNAL_API_ERROR, "searchShop", e);
        }
    }

    private NaverShopSearchResponse emptyResponse(int start, int display) {
        NaverShopSearchResponse response = new NaverShopSearchResponse();
        response.setTotal(0);
        response.setStart(start);
        response.setDisplay(display);
        response.setItems(List.of());
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
    public static class NaverShopSearchResponse {
        private int total;
        private int start;
        private int display;
        private List<ShopSearchTrendItem> items = new ArrayList<>();
    }
}
