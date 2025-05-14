package com.example.charity_collection.controller;

import com.example.charity_collection.dto.MessageResponseDto;
import com.example.charity_collection.service.ExchangeRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @PostMapping("/update")
    public ResponseEntity<MessageResponseDto> updateExchangeRates() {
        exchangeRateService.manuallyUpdateExchangeRates();
        return ResponseEntity.ok(new MessageResponseDto("Exchange rated updated successfully"));
    }
}
