package com.example.RankCat.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShopSearchTrendItem {
    private Integer rank;
    private String title;
    private String link;
    private String image;
    private String lprice;
    private String hprice;
    private String mallName;
    private String brand;
    private String maker;
    private String productId;
    private String productType;
    private String category1;
    private String category2;
    private String category3;
    private String category4;
    private Map<String, Object> extraFields = new LinkedHashMap<>();

    @JsonAnySetter
    public void addExtraField(String key, Object value) {
        extraFields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtraFields() {
        return extraFields;
    }
}
