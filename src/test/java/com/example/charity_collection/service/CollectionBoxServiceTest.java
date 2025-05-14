package com.example.charity_collection.service;

import com.example.charity_collection.dto.*;
import com.example.charity_collection.model.*;
import com.example.charity_collection.repository.CollectionBoxRepository;
import com.example.charity_collection.repository.CurrencyRepository;
import com.example.charity_collection.repository.ExchangeRateRepository;
import com.example.charity_collection.repository.FundraisingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollectionBoxServiceTest {

    @Mock
    private CollectionBoxRepository collectionBoxRepository;

    @Mock
    private FundraisingEventRepository fundraisingEventRepository;

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private CollectionBoxService collectionBoxService;

    private CollectionBox collectionBox;
    private FundraisingEvent fundraisingEvent;
    private Currency plnCurrency;
    private Currency eurCurrency;
    private ExchangeRate exchangeRate;

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

        fundraisingEvent = FundraisingEvent.builder()
                .id(1L)
                .name("Test Event")
                .description("Test Description")
                .accountBalance(BigDecimal.ZERO)
                .currency(plnCurrency)
                .build();

        collectionBox = CollectionBox.builder()
                .id(1L)
                .identifier("BOX123")
                .isEmpty(true)
                .fundraisingEvent(null)
                .money(new ArrayList<>())
                .build();

        exchangeRate = new ExchangeRate();
        exchangeRate.setId(1L);
        exchangeRate.setFromCurrency(eurCurrency);
        exchangeRate.setToCurrency(plnCurrency);
        exchangeRate.setRate(new BigDecimal("4.5"));
    }


    @Test
    void registerCollectionBox_Success() {
        CollectionBoxDto inputDto = CollectionBoxDto.builder()
                .identifier("BOX123")
                .isEmpty(true)
                .build();

        when(collectionBoxRepository.findByIdentifier(inputDto.getIdentifier())).thenReturn(Optional.empty());
        when(collectionBoxRepository.save(any(CollectionBox.class))).thenReturn(collectionBox);

        CollectionBoxDto result = collectionBoxService.registerCollectionBox(inputDto);

        assertNotNull(result);
        assertEquals(collectionBox.getId(), result.getId());
        assertEquals(collectionBox.getIdentifier(), result.getIdentifier());
        assertEquals(collectionBox.getIsEmpty(), result.getIsEmpty());
        verify(collectionBoxRepository).findByIdentifier(inputDto.getIdentifier());
        verify(collectionBoxRepository).save(any(CollectionBox.class));
    }

    @Test
    void registerCollectionBox_AlreadyExists() {
        CollectionBoxDto inputDto = CollectionBoxDto.builder()
                .identifier("BOX123")
                .isEmpty(true)
                .build();

        when(collectionBoxRepository.findByIdentifier(inputDto.getIdentifier())).thenReturn(Optional.of(collectionBox));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.registerCollectionBox(inputDto);
        });

        assertEquals("Collection box with identifier BOX123 already exists", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(inputDto.getIdentifier());
        verify(collectionBoxRepository, never()).save(any(CollectionBox.class));
    }

    @Test
    void listAllCollectionBoxes() {
        CollectionBox assignedBox = CollectionBox.builder()
                .id(2L)
                .identifier("BOX456")
                .isEmpty(false)
                .fundraisingEvent(fundraisingEvent)
                .money(new ArrayList<>())
                .build();

        List<CollectionBox> boxes = Arrays.asList(collectionBox, assignedBox);
        when(collectionBoxRepository.findAll()).thenReturn(boxes);

        List<CollectionBoxInfoDto> results = collectionBoxService.listAllCollectionBoxes();

        assertEquals(2, results.size());

        CollectionBoxInfoDto firstBox = results.get(0);
        assertEquals(collectionBox.getId(), firstBox.getId());
        assertEquals(collectionBox.getIdentifier(), firstBox.getIdentifier());
        assertEquals(collectionBox.getIsEmpty(), firstBox.isEmpty());
        assertFalse(firstBox.isAssigned());

        CollectionBoxInfoDto secondBox = results.get(1);
        assertEquals(assignedBox.getId(), secondBox.getId());
        assertEquals(assignedBox.getIdentifier(), secondBox.getIdentifier());
        assertEquals(assignedBox.getIsEmpty(), secondBox.isEmpty());
        assertTrue(secondBox.isAssigned());

        verify(collectionBoxRepository).findAll();
    }

    @Test
    void unregisterCollectionBox_Success() {
        String identifier = "BOX123";
        when(collectionBoxRepository.findByIdentifier(identifier)).thenReturn(Optional.of(collectionBox));
        doNothing().when(collectionBoxRepository).delete(collectionBox);

        MessageResponseDto result = collectionBoxService.unregisterCollectionBox(identifier);

        assertNotNull(result);
        assertEquals("Collection box with identifier BOX123 deleted successfully", result.getMessage());
        verify(collectionBoxRepository).findByIdentifier(identifier);
        verify(collectionBoxRepository).delete(collectionBox);
    }

    @Test
    void unregisterCollectionBox_NotFound() {
        String identifier = "NONEXISTENT";
        when(collectionBoxRepository.findByIdentifier(identifier)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.unregisterCollectionBox(identifier);
        });

        assertEquals("Collection box not found with identifier: NONEXISTENT", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(identifier);
        verify(collectionBoxRepository, never()).delete(any(CollectionBox.class));
    }

    @Test
    void assignCollectionBoxToEvent_Success() {
        AssignCollectionBoxDto dto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("BOX123")
                .fundraisingEventId(1L)
                .build();

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.of(collectionBox));
        when(fundraisingEventRepository.findById(dto.getFundraisingEventId())).thenReturn(Optional.of(fundraisingEvent));
        when(collectionBoxRepository.save(any(CollectionBox.class))).thenReturn(collectionBox);

        MessageResponseDto result = collectionBoxService.assignCollectionBoxToEvent(dto);

        assertNotNull(result);
        assertEquals("Collection box with identifier BOX123 successfully assigned to event Test Event", result.getMessage());
        assertEquals(fundraisingEvent, collectionBox.getFundraisingEvent());
        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(fundraisingEventRepository).findById(dto.getFundraisingEventId());
        verify(collectionBoxRepository).save(collectionBox);
    }

    @Test
    void assignCollectionBoxToEvent_BoxNotFound() {
        AssignCollectionBoxDto dto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("NONEXISTENT")
                .fundraisingEventId(1L)
                .build();

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.assignCollectionBoxToEvent(dto);
        });

        assertEquals("Collection box not found with identifier: NONEXISTENT", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(fundraisingEventRepository, never()).findById(anyLong());
        verify(collectionBoxRepository, never()).save(any(CollectionBox.class));
    }

    @Test
    void assignCollectionBoxToEvent_EventNotFound() {
        AssignCollectionBoxDto dto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("BOX123")
                .fundraisingEventId(999L)
                .build();

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.of(collectionBox));
        when(fundraisingEventRepository.findById(dto.getFundraisingEventId())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.assignCollectionBoxToEvent(dto);
        });

        assertEquals("Fundraising event not found with id: 999", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(fundraisingEventRepository).findById(dto.getFundraisingEventId());
        verify(collectionBoxRepository, never()).save(any(CollectionBox.class));
    }

    @Test
    void assignCollectionBoxToEvent_NonEmptyBox() {
        AssignCollectionBoxDto dto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("BOX123")
                .fundraisingEventId(1L)
                .build();

        collectionBox.setIsEmpty(false);

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.of(collectionBox));
        when(fundraisingEventRepository.findById(dto.getFundraisingEventId())).thenReturn(Optional.of(fundraisingEvent));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.assignCollectionBoxToEvent(dto);
        });

        assertEquals("Non-empty box cannot be assigned to a fundraising event", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(fundraisingEventRepository).findById(dto.getFundraisingEventId());
        verify(collectionBoxRepository, never()).save(any(CollectionBox.class));
    }

    @Test
    void addMoneyToCollectionBox_Success_NewCurrency() {
        // Arrange
        AddMoneyDto dto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .currencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        collectionBox.setFundraisingEvent(fundraisingEvent);

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.of(collectionBox));
        when(currencyRepository.findByCode(dto.getCurrencyCode())).thenReturn(Optional.of(eurCurrency));
        when(collectionBoxRepository.save(any(CollectionBox.class))).thenReturn(collectionBox);

        // Act
        MessageResponseDto result = collectionBoxService.addMoneyToCollectionBox(dto);

        // Assert
        assertNotNull(result);
        assertEquals("Amount 100.00 EUR added to collection box with identifier BOX123", result.getMessage());
        assertFalse(collectionBox.getIsEmpty());
        assertEquals(1, collectionBox.getMoney().size());
        assertEquals(eurCurrency, collectionBox.getMoney().get(0).getCurrency());
        assertEquals(new BigDecimal("100.00"), collectionBox.getMoney().get(0).getAmount());

        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(currencyRepository).findByCode(dto.getCurrencyCode());
        verify(collectionBoxRepository).save(collectionBox);
    }

    @Test
    void addMoneyToCollectionBox_Success_ExistingCurrency() {
        AddMoneyDto dto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .currencyCode("EUR")
                .amount(new BigDecimal("50.00"))
                .build();

        collectionBox.setFundraisingEvent(fundraisingEvent);

        CollectionBoxMoney existingMoney = CollectionBoxMoney.builder()
                .id(1L)
                .collectionBox(collectionBox)
                .currency(eurCurrency)
                .amount(new BigDecimal("100.00"))
                .build();

        collectionBox.getMoney().add(existingMoney);
        collectionBox.setIsEmpty(false);

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.of(collectionBox));
        when(currencyRepository.findByCode(dto.getCurrencyCode())).thenReturn(Optional.of(eurCurrency));
        when(collectionBoxRepository.save(any(CollectionBox.class))).thenReturn(collectionBox);

        MessageResponseDto result = collectionBoxService.addMoneyToCollectionBox(dto);

        assertNotNull(result);
        assertEquals("Amount 50.00 EUR added to collection box with identifier BOX123", result.getMessage());
        assertFalse(collectionBox.getIsEmpty());
        assertEquals(1, collectionBox.getMoney().size());
        assertEquals(eurCurrency, collectionBox.getMoney().get(0).getCurrency());
        assertEquals(new BigDecimal("150.00"), collectionBox.getMoney().get(0).getAmount());

        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(currencyRepository).findByCode(dto.getCurrencyCode());
        verify(collectionBoxRepository).save(collectionBox);
    }

    @Test
    void addMoneyToCollectionBox_BoxNotFound() {
        AddMoneyDto dto = AddMoneyDto.builder()
                .collectionBoxIdentifier("NONEXISTENT")
                .currencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.addMoneyToCollectionBox(dto);
        });

        assertEquals("Collection box not found with identifier: NONEXISTENT", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(currencyRepository, never()).findByCode(anyString());
        verify(collectionBoxRepository, never()).save(any(CollectionBox.class));
    }

    @Test
    void addMoneyToCollectionBox_BoxNotAssigned() {
        AddMoneyDto dto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .currencyCode("EUR")
                .amount(new BigDecimal("100.00"))
                .build();

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.of(collectionBox));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.addMoneyToCollectionBox(dto);
        });

        assertEquals("You can only add money to a box that is assigned to a fundraising event", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(currencyRepository, never()).findByCode(anyString());
        verify(collectionBoxRepository, never()).save(any(CollectionBox.class));
    }

    @Test
    void addMoneyToCollectionBox_CurrencyNotSupported() {
        AddMoneyDto dto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .currencyCode("USD")
                .amount(new BigDecimal("100.00"))
                .build();

        collectionBox.setFundraisingEvent(fundraisingEvent);

        when(collectionBoxRepository.findByIdentifier(dto.getCollectionBoxIdentifier())).thenReturn(Optional.of(collectionBox));
        when(currencyRepository.findByCode(dto.getCurrencyCode())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.addMoneyToCollectionBox(dto);
        });

        assertEquals("Currency not supported: USD", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(dto.getCollectionBoxIdentifier());
        verify(currencyRepository).findByCode(dto.getCurrencyCode());
        verify(collectionBoxRepository, never()).save(any(CollectionBox.class));
    }

    @Test
    void emptyCollectionBox_Success() {
        String identifier = "BOX123";

        collectionBox.setFundraisingEvent(fundraisingEvent);
        collectionBox.setIsEmpty(false);

        CollectionBoxMoney money1 = CollectionBoxMoney.builder()
                .id(1L)
                .collectionBox(collectionBox)
                .currency(plnCurrency)
                .amount(new BigDecimal("100.00"))
                .build();

        CollectionBoxMoney money2 = CollectionBoxMoney.builder()
                .id(2L)
                .collectionBox(collectionBox)
                .currency(eurCurrency)
                .amount(new BigDecimal("50.00"))
                .build();

        collectionBox.getMoney().add(money1);
        collectionBox.getMoney().add(money2);

        when(collectionBoxRepository.findByIdentifier(identifier)).thenReturn(Optional.of(collectionBox));
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency(eurCurrency, plnCurrency))
                .thenReturn(Optional.of(exchangeRate));
        when(fundraisingEventRepository.save(any(FundraisingEvent.class))).thenReturn(fundraisingEvent);
        when(collectionBoxRepository.save(any(CollectionBox.class))).thenReturn(collectionBox);

        MessageResponseDto result = collectionBoxService.emptyCollectionBox(identifier);

        assertNotNull(result);
        String expectedMessage = "Collection box with identifier BOX123 has been emptied. Amount 325.00 PLN has been transfered to Test Event";
        assertEquals(expectedMessage, result.getMessage());

        assertEquals(new BigDecimal("325.00").setScale(2, RoundingMode.HALF_UP),
                fundraisingEvent.getAccountBalance().setScale(2, RoundingMode.HALF_UP));

        assertTrue(collectionBox.getIsEmpty());
        assertTrue(collectionBox.getMoney().isEmpty());

        verify(collectionBoxRepository).findByIdentifier(identifier);
        verify(exchangeRateRepository).findByFromCurrencyAndToCurrency(eurCurrency, plnCurrency);
        verify(fundraisingEventRepository).save(fundraisingEvent);
        verify(collectionBoxRepository).save(collectionBox);
    }

    @Test
    void emptyCollectionBox_NotFound() {
        String identifier = "NONEXISTENT";

        when(collectionBoxRepository.findByIdentifier(identifier)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.emptyCollectionBox(identifier);
        });

        assertEquals("Collection box not found", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(identifier);
        verify(exchangeRateRepository, never()).findByFromCurrencyAndToCurrency(any(), any());
        verify(fundraisingEventRepository, never()).save(any());
        verify(collectionBoxRepository, never()).save(any());
    }

    @Test
    void emptyCollectionBox_EmptyBox() {
        String identifier = "BOX123";

        collectionBox.setIsEmpty(true);

        when(collectionBoxRepository.findByIdentifier(identifier)).thenReturn(Optional.of(collectionBox));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.emptyCollectionBox(identifier);
        });

        assertEquals("Collection box has no money inside", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(identifier);
        verify(exchangeRateRepository, never()).findByFromCurrencyAndToCurrency(any(), any());
        verify(fundraisingEventRepository, never()).save(any());
        verify(collectionBoxRepository, never()).save(any());
    }

    @Test
    void emptyCollectionBox_NotAssigned() {
        String identifier = "BOX123";

        collectionBox.setIsEmpty(false);

        when(collectionBoxRepository.findByIdentifier(identifier)).thenReturn(Optional.of(collectionBox));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            collectionBoxService.emptyCollectionBox(identifier);
        });

        assertEquals("Collection box is not assigned to an event", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(identifier);
        verify(exchangeRateRepository, never()).findByFromCurrencyAndToCurrency(any(), any());
        verify(fundraisingEventRepository, never()).save(any());
        verify(collectionBoxRepository, never()).save(any());
    }

    @Test
    void emptyCollectionBox_ExchangeRateNotFound() {
        String identifier = "BOX123";

        collectionBox.setFundraisingEvent(fundraisingEvent);
        collectionBox.setIsEmpty(false);

        CollectionBoxMoney money = CollectionBoxMoney.builder()
                .id(1L)
                .collectionBox(collectionBox)
                .currency(eurCurrency)
                .amount(new BigDecimal("50.00"))
                .build();

        collectionBox.getMoney().add(money);

        when(collectionBoxRepository.findByIdentifier(identifier)).thenReturn(Optional.of(collectionBox));
        when(exchangeRateRepository.findByFromCurrencyAndToCurrency(eurCurrency, plnCurrency))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            collectionBoxService.emptyCollectionBox(identifier);
        });

        assertEquals("Exchange rate not found for currency pair: EUR to PLN", exception.getMessage());
        verify(collectionBoxRepository).findByIdentifier(identifier);
        verify(exchangeRateRepository).findByFromCurrencyAndToCurrency(eurCurrency, plnCurrency);
        verify(fundraisingEventRepository, never()).save(any());
        verify(collectionBoxRepository, never()).save(any());
    }
}