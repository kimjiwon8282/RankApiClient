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
public class SearchAdKeywordItem {
    private String relKeyword;
    private String monthlyPcQcCnt;
    private String monthlyMobileQcCnt;
    private String monthlyAvePcClkCnt;
    private String monthlyAveMobileClkCnt;
    private String monthlyAvePcCtr;
    private String monthlyAveMobileCtr;
    private String compIdx;
    private String plAvgDepth;
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
