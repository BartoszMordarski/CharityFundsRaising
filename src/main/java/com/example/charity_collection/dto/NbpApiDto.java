package com.example.charity_collection.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NbpApiDto {
    private String table;
    private String no;
    private String effectiveDate;
    private List<RateDto> rates;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RateDto {
        private String currency;
        private String code;
        private String mid;
    }
}
