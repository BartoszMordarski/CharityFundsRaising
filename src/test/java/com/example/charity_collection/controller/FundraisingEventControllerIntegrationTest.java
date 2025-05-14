package com.example.charity_collection.controller;

import com.example.charity_collection.dto.FundraisingEventDto;
import com.example.charity_collection.model.Currency;
import com.example.charity_collection.repository.CurrencyRepository;
import com.example.charity_collection.repository.ExchangeRateRepository;
import com.example.charity_collection.repository.FundraisingEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FundraisingEventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FundraisingEventRepository fundraisingEventRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    private Currency usdCurrency;
    private Currency eurCurrency;

    @BeforeEach
    void setUp() {
        fundraisingEventRepository.deleteAll();
        exchangeRateRepository.deleteAll();
        currencyRepository.deleteAll();

        usdCurrency = new Currency();
        usdCurrency.setCode("USD");
        usdCurrency.setName("US Dollar");
        usdCurrency = currencyRepository.save(usdCurrency);

        eurCurrency = new Currency();
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");
        eurCurrency = currencyRepository.save(eurCurrency);
    }

    @Test
    void createFundraisingEvent_ShouldCreateNewEvent() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);

        FundraisingEventDto requestDto = FundraisingEventDto.builder()
                .name("Test Fundraising Event")
                .description("Event for testing purposes")
                .startDate(startDate)
                .endDate(endDate)
                .currencyCode("USD")
                .build();

        mockMvc.perform(post("/api/fundraising-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Test Fundraising Event"))
                .andExpect(jsonPath("$.description").value("Event for testing purposes"))
                .andExpect(jsonPath("$.startDate").value(startDate.toString()))
                .andExpect(jsonPath("$.endDate").value(endDate.toString()))
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.accountBalance").value("0"));
    }

    @Test
    void createFundraisingEvent_WithInvalidDateRange_ShouldReturnBadRequest() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(1); // End date before start date

        FundraisingEventDto requestDto = FundraisingEventDto.builder()
                .name("Invalid Event")
                .description("Event with invalid date range")
                .startDate(startDate)
                .endDate(endDate)
                .currencyCode("USD")
                .build();

        mockMvc.perform(post("/api/fundraising-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Start date must be before end date")));
    }

    @Test
    void createFundraisingEvent_WithUnsupportedCurrency_ShouldReturnBadRequest() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);

        FundraisingEventDto requestDto = FundraisingEventDto.builder()
                .name("Currency Test Event")
                .description("Event with unsupported currency")
                .startDate(startDate)
                .endDate(endDate)
                .currencyCode("THB")
                .build();

        mockMvc.perform(post("/api/fundraising-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Currency not supported: THB")));
    }

    @Test
    void createFundraisingEvent_WithMissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        FundraisingEventDto requestDto = FundraisingEventDto.builder()
                .description("Event with missing fields")
                .build();

        mockMvc.perform(post("/api/fundraising-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}
