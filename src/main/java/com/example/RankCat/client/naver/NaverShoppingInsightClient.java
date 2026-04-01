package com.example.RankCat.client.naver;

import com.example.RankCat.common.exception.BusinessException;
import com.example.RankCat.common.exception.ErrorCode;
import com.example.RankCat.model.ShoppingInsightTrendResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class NaverShoppingInsightClient {

    private final RestTemplate shoppingInsightRestTemplate;

    public NaverShoppingInsightClient(
            @Qualifier("shoppingInsightRestTemplate") RestTemplate shoppingInsightRestTemplate) {
        this.shoppingInsightRestTemplate = shoppingInsightRestTemplate;
    }

    public ShoppingInsightTrendResponse fetchCategoryTrend(
            String startDate,
            String endDate,
            String timeUnit,
            String categoryName,
            String categoryCode) {
        CategoryTrendRequest request =
                new CategoryTrendRequest(
                        startDate,
                        endDate,
                        timeUnit,
                        List.of(new CategoryGroup(categoryName, List.of(categoryCode))),
                        "",
                        "",
                        List.of());
        try {
            return shoppingInsightRestTemplate.postForObject(
                    "/v1/datalab/shopping/categories",
                    new HttpEntity<>(request),
                    ShoppingInsightTrendResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw externalException(ErrorCode.EXTERNAL_API_UNAUTHORIZED, "fetchCategoryTrend", e);
        } catch (ResourceAccessException e) {
            throw externalException(ErrorCode.EXTERNAL_API_TIMEOUT, "fetchCategoryTrend", e);
        } catch (HttpClientErrorException e) {
            throw externalException(ErrorCode.EXTERNAL_API_CLIENT_ERROR, "fetchCategoryTrend", e);
        } catch (HttpServerErrorException e) {
            throw externalException(ErrorCode.EXTERNAL_API_SERVER_ERROR, "fetchCategoryTrend", e);
        } catch (RestClientException e) {
            throw externalException(ErrorCode.EXTERNAL_API_ERROR, "fetchCategoryTrend", e);
        }
    }

    public ShoppingInsightTrendResponse fetchKeywordTrend(
            String startDate,
            String endDate,
            String timeUnit,
            String categoryCode,
            List<String> keywords) {
        List<KeywordGroup> keywordGroups =
                keywords.stream().map(k -> new KeywordGroup(k, List.of(k))).toList();
        KeywordTrendRequest request =
                new KeywordTrendRequest(
                        startDate,
                        endDate,
                        timeUnit,
                        categoryCode,
                        keywordGroups,
                        "",
                        "",
                        List.of());
        try {
            return shoppingInsightRestTemplate.postForObject(
                    "/v1/datalab/shopping/category/keywords",
                    new HttpEntity<>(request),
                    ShoppingInsightTrendResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw externalException(ErrorCode.EXTERNAL_API_UNAUTHORIZED, "fetchKeywordTrend", e);
        } catch (ResourceAccessException e) {
            throw externalException(ErrorCode.EXTERNAL_API_TIMEOUT, "fetchKeywordTrend", e);
        } catch (HttpClientErrorException e) {
            throw externalException(ErrorCode.EXTERNAL_API_CLIENT_ERROR, "fetchKeywordTrend", e);
        } catch (HttpServerErrorException e) {
            throw externalException(ErrorCode.EXTERNAL_API_SERVER_ERROR, "fetchKeywordTrend", e);
        } catch (RestClientException e) {
            throw externalException(ErrorCode.EXTERNAL_API_ERROR, "fetchKeywordTrend", e);
        }
    }

    private BusinessException externalException(ErrorCode errorCode, String action, Exception e) {
        log.error("Naver API 호출 실패 ({}): {}", action, e.getMessage());
        return new BusinessException(errorCode, e.getMessage(), e);
    }

    private record CategoryTrendRequest(
            String startDate,
            String endDate,
            String timeUnit,
            List<CategoryGroup> category,
            String device,
            String gender,
            List<String> ages) {}

    private record CategoryGroup(String name, List<String> param) {}

    private record KeywordTrendRequest(
            String startDate,
            String endDate,
            String timeUnit,
            String category,
            List<KeywordGroup> keyword,
            String device,
            String gender,
            List<String> ages) {}

    private record KeywordGroup(String name, List<String> param) {}
}
