package com.example.charity_collection.service;

import com.example.charity_collection.dto.FundraisingEventDto;
import com.example.charity_collection.model.Currency;
import com.example.charity_collection.model.FundraisingEvent;
import com.example.charity_collection.repository.CurrencyRepository;
import com.example.charity_collection.repository.FundraisingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FundraisingEventServiceTest {

    @Mock
    private FundraisingEventRepository fundraisingEventRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private FundraisingEventService fundraisingEventService;

    private FundraisingEventDto fundraisingEventDto;
    private FundraisingEvent fundraisingEvent;
    private Currency currency;

    @BeforeEach
    void setUp() {
        currency = new Currency();
        currency.setId(1L);
        currency.setCode("PLN");
        currency.setName("Polish Zloty");

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);

        fundraisingEventDto = FundraisingEventDto.builder()
                .name("Test Fundraising Event")
                .description("Test Event Description")
                .startDate(startDate)
                .endDate(endDate)
                .currencyCode("PLN")
                .build();

        fundraisingEvent = FundraisingEvent.builder()
                .id(1L)
                .name(fundraisingEventDto.getName())
                .description(fundraisingEventDto.getDescription())
                .startDate(fundraisingEventDto.getStartDate())
                .endDate(fundraisingEventDto.getEndDate())
                .currency(currency)
                .accountBalance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void createFundraisingEvent_Success() {
        when(currencyRepository.findByCode(fundraisingEventDto.getCurrencyCode())).thenReturn(Optional.of(currency));
        when(fundraisingEventRepository.save(any(FundraisingEvent.class))).thenReturn(fundraisingEvent);

        FundraisingEventDto result = fundraisingEventService.createFundraisingEvent(fundraisingEventDto);

        assertNotNull(result);
        assertEquals(fundraisingEvent.getId(), result.getId());
        assertEquals(fundraisingEvent.getName(), result.getName());
        assertEquals(fundraisingEvent.getDescription(), result.getDescription());
        assertEquals(fundraisingEvent.getStartDate(), result.getStartDate());
        assertEquals(fundraisingEvent.getEndDate(), result.getEndDate());
        assertEquals(fundraisingEvent.getCurrency().getCode(), result.getCurrencyCode());
        assertEquals(fundraisingEvent.getAccountBalance(), result.getAccountBalance());

        verify(currencyRepository).findByCode(fundraisingEventDto.getCurrencyCode());
        verify(fundraisingEventRepository).save(any(FundraisingEvent.class));
    }

    @Test
    void createFundraisingEvent_InvalidDates() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(1);

        fundraisingEventDto.setStartDate(startDate);
        fundraisingEventDto.setEndDate(endDate);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fundraisingEventService.createFundraisingEvent(fundraisingEventDto);
        });

        assertEquals("Start date must be before end date", exception.getMessage());
        verify(currencyRepository, never()).findByCode(anyString());
        verify(fundraisingEventRepository, never()).save(any(FundraisingEvent.class));
    }

    @Test
    void createFundraisingEvent_SameDates() {
        LocalDate sameDate = LocalDate.now();

        fundraisingEventDto.setStartDate(sameDate);
        fundraisingEventDto.setEndDate(sameDate);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fundraisingEventService.createFundraisingEvent(fundraisingEventDto);
        });

        assertEquals("Start date must be before end date", exception.getMessage());
        verify(currencyRepository, never()).findByCode(anyString());
        verify(fundraisingEventRepository, never()).save(any(FundraisingEvent.class));
    }

    @Test
    void createFundraisingEvent_CurrencyNotSupported() {
        fundraisingEventDto.setCurrencyCode("XYZ");

        when(currencyRepository.findByCode(fundraisingEventDto.getCurrencyCode())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            fundraisingEventService.createFundraisingEvent(fundraisingEventDto);
        });

        assertEquals("Currency not supported: XYZ", exception.getMessage());
        verify(currencyRepository).findByCode(fundraisingEventDto.getCurrencyCode());
        verify(fundraisingEventRepository, never()).save(any(FundraisingEvent.class));
    }

    @Test
    void createFundraisingEvent_AccountBalanceInitializedToZero() {
        when(currencyRepository.findByCode(fundraisingEventDto.getCurrencyCode())).thenReturn(Optional.of(currency));
        when(fundraisingEventRepository.save(any(FundraisingEvent.class))).thenReturn(fundraisingEvent);

        FundraisingEventDto result = fundraisingEventService.createFundraisingEvent(fundraisingEventDto);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getAccountBalance());

        verify(fundraisingEventRepository).save(argThat(event ->
                event.getAccountBalance().compareTo(BigDecimal.ZERO) == 0
        ));
    }
}