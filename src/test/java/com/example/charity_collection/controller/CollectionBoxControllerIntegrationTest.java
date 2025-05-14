package com.example.charity_collection.controller;

import com.example.charity_collection.dto.*;
import com.example.charity_collection.model.*;
import com.example.charity_collection.repository.*;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CollectionBoxControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CollectionBoxRepository collectionBoxRepository;

    @Autowired
    private FundraisingEventRepository fundraisingEventRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    private FundraisingEvent savedEvent;
    private Currency usdCurrency;
    private Currency eurCurrency;

    @BeforeEach
    void setUp() {
        collectionBoxRepository.deleteAll();
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

        ExchangeRate usdToEur = new ExchangeRate();
        usdToEur.setFromCurrency(usdCurrency);
        usdToEur.setToCurrency(eurCurrency);
        usdToEur.setRate(new BigDecimal("0.85"));
        exchangeRateRepository.save(usdToEur);

        ExchangeRate eurToUsd = new ExchangeRate();
        eurToUsd.setFromCurrency(eurCurrency);
        eurToUsd.setToCurrency(usdCurrency);
        eurToUsd.setRate(new BigDecimal("1.18"));
        exchangeRateRepository.save(eurToUsd);

        FundraisingEvent event = FundraisingEvent.builder()
                .name("Test Fundraising Event")
                .description("Test Description")
                .accountBalance(BigDecimal.ZERO)
                .currency(usdCurrency)
                .build();
        savedEvent = fundraisingEventRepository.save(event);
    }


    @Test
    void registerCollectionBox_ShouldCreateNewCollectionBox() throws Exception {
        CollectionBoxDto requestDto = CollectionBoxDto.builder()
                .identifier("BOX123")
                .build();

        mockMvc.perform(post("/api/collection-boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void listAllCollectionBoxes_ShouldReturnAllBoxes() throws Exception {
        CollectionBox box1 = CollectionBox.builder()
                .identifier("BOX001")
                .isEmpty(true)
                .build();
        collectionBoxRepository.save(box1);

        CollectionBox box2 = CollectionBox.builder()
                .identifier("BOX002")
                .isEmpty(false)
                .fundraisingEvent(savedEvent)
                .build();
        collectionBoxRepository.save(box2);

        mockMvc.perform(get("/api/collection-boxes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].identifier").value("BOX001"))
                .andExpect(jsonPath("$[0].empty").value(true))
                .andExpect(jsonPath("$[0].assigned").value(false))
                .andExpect(jsonPath("$[1].identifier").value("BOX002"))
                .andExpect(jsonPath("$[1].empty").value(false))
                .andExpect(jsonPath("$[1].assigned").value(true));
    }

    @Test
    void unregisterCollectionBox_ShouldDeleteBox() throws Exception {
        CollectionBox box = CollectionBox.builder()
                .identifier("BOX123")
                .isEmpty(true)
                .build();
        collectionBoxRepository.save(box);

        mockMvc.perform(delete("/api/collection-boxes/delete/BOX123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("deleted successfully")));

    }

    @Test
    void unregisterCollectionBox_WithNonExistingIdentifier_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/collection-boxes/delete/NONEXISTENT"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("not found")));
    }

    @Test
    void assignCollectionBoxToEvent_ShouldAssignBoxToEvent() throws Exception {
        CollectionBox box = CollectionBox.builder()
                .identifier("BOX123")
                .isEmpty(true)
                .build();
        collectionBoxRepository.save(box);

        AssignCollectionBoxDto requestDto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("BOX123")
                .fundraisingEventId(savedEvent.getId())
                .build();

        mockMvc.perform(post("/api/collection-boxes/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("successfully assigned to event")));
    }

    @Test
    void assignCollectionBoxToEvent_WithNonEmptyBox_ShouldReturnBadRequest() throws Exception {
        CollectionBox box = CollectionBox.builder()
                .identifier("BOX123")
                .isEmpty(false)
                .build();
        collectionBoxRepository.save(box);

        AssignCollectionBoxDto requestDto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("BOX123")
                .fundraisingEventId(savedEvent.getId())
                .build();

        mockMvc.perform(post("/api/collection-boxes/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Non-empty box cannot be assigned")));
    }

    @Test
    void addMoneyToCollectionBox_ShouldAddMoneyToBox() throws Exception {
        CollectionBox box = CollectionBox.builder()
                .identifier("BOX123")
                .isEmpty(true)
                .fundraisingEvent(savedEvent)
                .build();
        collectionBoxRepository.save(box);

        AddMoneyDto requestDto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .amount(new BigDecimal("100.00"))
                .currencyCode("USD")
                .build();

        mockMvc.perform(post("/api/collection-boxes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("added to collection box")));

    }

    @Test
    void addMoneyToCollectionBox_WithNonAssignedBox_ShouldReturnBadRequest() throws Exception {
        CollectionBox box = CollectionBox.builder()
                .identifier("BOX123")
                .isEmpty(true)
                .build();
        collectionBoxRepository.save(box);

        AddMoneyDto requestDto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .amount(new BigDecimal("100.00"))
                .currencyCode("USD")
                .build();

        mockMvc.perform(post("/api/collection-boxes/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("add money to a box that is assigned")));
    }

    @Test
    void emptyCollectionBox_ShouldEmptyBoxAndTransferMoney() throws Exception {
        CollectionBox box = CollectionBox.builder()
                .identifier("BOX123")
                .isEmpty(false)
                .fundraisingEvent(savedEvent)
                .build();
        CollectionBox savedBox = collectionBoxRepository.save(box);

        CollectionBoxMoney money = CollectionBoxMoney.builder()
                .collectionBox(savedBox)
                .currency(usdCurrency)
                .amount(new BigDecimal("200.00"))
                .build();
        savedBox.getMoney().add(money);
        collectionBoxRepository.save(savedBox);

        mockMvc.perform(post("/api/collection-boxes/empty/BOX123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("has been emptied")));

    }

    @Test
    void emptyCollectionBox_WithEmptyBox_ShouldReturnBadRequest() throws Exception {
        CollectionBox box = CollectionBox.builder()
                .identifier("BOX123")
                .isEmpty(true)
                .fundraisingEvent(savedEvent)
                .build();
        collectionBoxRepository.save(box);

        mockMvc.perform(post("/api/collection-boxes/empty/BOX123"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("has no money inside")));
    }
}
