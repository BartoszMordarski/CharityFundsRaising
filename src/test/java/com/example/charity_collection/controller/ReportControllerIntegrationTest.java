package com.example.charity_collection.controller;

import com.example.charity_collection.model.Currency;
import com.example.charity_collection.model.FundraisingEvent;
import com.example.charity_collection.repository.CurrencyRepository;
import com.example.charity_collection.repository.ExchangeRateRepository;
import com.example.charity_collection.repository.FundraisingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FundraisingEventRepository fundraisingEventRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

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
    void getFinancialReport_ShouldReturnEmptyList_WhenNoEvents() throws Exception {
        mockMvc.perform(get("/api/reports/financial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getFinancialReport_ShouldReturnSingleEvent_WhenOneEventExists() throws Exception {
        FundraisingEvent event = FundraisingEvent.builder()
                .name("Test Event")
                .description("Test Description")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .accountBalance(new BigDecimal("1000.00"))
                .currency(usdCurrency)
                .build();

        fundraisingEventRepository.save(event);

        mockMvc.perform(get("/api/reports/financial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventName").value("Test Event"))
                .andExpect(jsonPath("$[0].amount").value(1000.00))
                .andExpect(jsonPath("$[0].currency").value("USD"));
    }

    @Test
    void getFinancialReport_ShouldReturnMultipleEvents_WhenMultipleEventsExist() throws Exception {
        FundraisingEvent event1 = FundraisingEvent.builder()
                .name("Charity Run")
                .description("Annual charity run")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .accountBalance(new BigDecimal("2500.50"))
                .currency(usdCurrency)
                .build();

        FundraisingEvent event2 = FundraisingEvent.builder()
                .name("Food Drive")
                .description("Community food drive")
                .startDate(LocalDate.now().plusDays(20))
                .endDate(LocalDate.now().plusDays(30))
                .accountBalance(new BigDecimal("1200.75"))
                .currency(eurCurrency)
                .build();

        fundraisingEventRepository.save(event1);
        fundraisingEventRepository.save(event2);

        mockMvc.perform(get("/api/reports/financial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].eventName", containsInAnyOrder("Charity Run", "Food Drive")))
                .andExpect(jsonPath("$[*].currency", containsInAnyOrder("USD", "EUR")))
                .andReturn();
    }

    @Test
    void getFinancialReport_ShouldContainZeroBalance_WhenEventHasNoMoney() throws Exception {
        FundraisingEvent event = FundraisingEvent.builder()
                .name("Zero Balance Event")
                .description("Event with no money")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .accountBalance(BigDecimal.ZERO)
                .currency(usdCurrency)
                .build();

        fundraisingEventRepository.save(event);

        mockMvc.perform(get("/api/reports/financial")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].eventName").value("Zero Balance Event"))
                .andExpect(jsonPath("$[0].amount").value(0.00))
                .andExpect(jsonPath("$[0].currency").value("USD"));
    }
}