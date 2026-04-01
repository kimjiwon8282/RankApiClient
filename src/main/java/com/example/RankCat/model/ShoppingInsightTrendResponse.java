package com.example.RankCat.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShoppingInsightTrendResponse {
    private String startDate;
    private String endDate;
    private String timeUnit;
    private List<TrendResult> results = new ArrayList<>();
    private Map<String, Object> extraFields = new LinkedHashMap<>();

    @JsonAnySetter
    public void addExtraField(String key, Object value) {
        extraFields.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getExtraFields() {
        return extraFields;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TrendResult {
        private String title;
        private List<String> category = new ArrayList<>();
        private List<String> keyword = new ArrayList<>();
        private List<TrendPoint> data = new ArrayList<>();
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

    @Getter
    @Setter
    @NoArgsConstructor
    public static class TrendPoint {
        private String period;
        private Double ratio;
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
}
