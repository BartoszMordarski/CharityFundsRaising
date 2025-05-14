package com.example.charity_collection.service;

import com.example.charity_collection.dto.FinancialReportDto;
import com.example.charity_collection.model.Currency;
import com.example.charity_collection.model.FundraisingEvent;
import com.example.charity_collection.repository.FundraisingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

    @Mock
    private FundraisingEventRepository fundraisingEventRepository;

    @InjectMocks
    private ReportService reportService;

    private FundraisingEvent event1;
    private FundraisingEvent event2;
    private Currency plnCurrency;
    private Currency eurCurrency;

    @BeforeEach
    void setUp() {
        plnCurrency = new Currency();
        plnCurrency.setId(1L);
        plnCurrency.setCode("PLN");
        plnCurrency.setName("Polish Zloty");

        eurCurrency = new Currency();
        eurCurrency.setId(2L);
        eurCurrency.setCode("EUR");
        eurCurrency.setName("Euro");

        event1 = FundraisingEvent.builder()
                .id(1L)
                .name("Charity One")
                .description("Cancer research charity")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .currency(plnCurrency)
                .accountBalance(new BigDecimal("1500.50"))
                .build();

        event2 = FundraisingEvent.builder()
                .id(2L)
                .name("Charity Two")
                .description("Charity collecting money for Caritas")
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(5))
                .currency(eurCurrency)
                .accountBalance(new BigDecimal("750.25"))
                .build();
    }

    @Test
    void generateFinancialReport_WithMultipleEvents() {
        when(fundraisingEventRepository.findAll()).thenReturn(Arrays.asList(event1, event2));

        List<FinancialReportDto> report = reportService.generateFinancialReport();

        assertEquals(2, report.size());

        FinancialReportDto report1 = report.get(0);
        assertEquals("Charity One", report1.getEventName());
        assertEquals(new BigDecimal("1500.50"), report1.getAmount());
        assertEquals("PLN", report1.getCurrency());

        FinancialReportDto report2 = report.get(1);
        assertEquals("Charity Two", report2.getEventName());
        assertEquals(new BigDecimal("750.25"), report2.getAmount());
        assertEquals("EUR", report2.getCurrency());

        verify(fundraisingEventRepository).findAll();
    }

    @Test
    void generateFinancialReport_WithSingleEvent() {
        when(fundraisingEventRepository.findAll()).thenReturn(Collections.singletonList(event1));

        List<FinancialReportDto> report = reportService.generateFinancialReport();

        assertEquals(1, report.size());

        FinancialReportDto reportDto = report.get(0);
        assertEquals("Charity One", reportDto.getEventName());
        assertEquals(new BigDecimal("1500.50"), reportDto.getAmount());
        assertEquals("PLN", reportDto.getCurrency());

        verify(fundraisingEventRepository).findAll();
    }

    @Test
    void generateFinancialReport_WithNoEvents() {
        when(fundraisingEventRepository.findAll()).thenReturn(Collections.emptyList());

        List<FinancialReportDto> report = reportService.generateFinancialReport();

        assertTrue(report.isEmpty());
        verify(fundraisingEventRepository).findAll();
    }

}