package com.example.charity_collection.service;

import com.example.charity_collection.dto.*;
import com.example.charity_collection.model.CollectionBox;
import com.example.charity_collection.model.CollectionBoxMoney;
import com.example.charity_collection.model.Currency;
import com.example.charity_collection.model.ExchangeRate;
import com.example.charity_collection.model.FundraisingEvent;
import com.example.charity_collection.repository.CollectionBoxRepository;
import com.example.charity_collection.repository.CurrencyRepository;
import com.example.charity_collection.repository.ExchangeRateRepository;
import com.example.charity_collection.repository.FundraisingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollectionBoxService {

    private final CollectionBoxRepository collectionBoxRepository;
    private final FundraisingEventRepository fundraisingEventRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public CollectionBoxService(CollectionBoxRepository collectionBoxRepository,
                                FundraisingEventRepository fundraisingEventRepository,
                                CurrencyRepository currencyRepository,
                                ExchangeRateRepository exchangeRateRepository) {
        this.collectionBoxRepository = collectionBoxRepository;
        this.fundraisingEventRepository = fundraisingEventRepository;
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Transactional
    public CollectionBoxDto registerCollectionBox(CollectionBoxDto collectionBoxDto) {

        if (collectionBoxRepository.findByIdentifier(collectionBoxDto.getIdentifier()).isPresent()) {
            throw new IllegalArgumentException("Collection box with identifier " +
                    collectionBoxDto.getIdentifier() + " already exists");
        }

        CollectionBox collectionBox = CollectionBox.builder()
                .identifier(collectionBoxDto.getIdentifier())
                .isEmpty(true)
                .build();

        CollectionBox savedBox = collectionBoxRepository.save(collectionBox);

        return CollectionBoxDto.builder()
                .id(savedBox.getId())
                .identifier(savedBox.getIdentifier())
                .isEmpty(savedBox.getIsEmpty())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CollectionBoxInfoDto> listAllCollectionBoxes() {
        return collectionBoxRepository.findAll().stream()
                .map(collectionBox -> CollectionBoxInfoDto.builder()
                        .id(collectionBox.getId())
                        .identifier(collectionBox.getIdentifier())
                        .isEmpty(collectionBox.getIsEmpty())
                        .isAssigned(collectionBox.getFundraisingEvent() != null)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageResponseDto unregisterCollectionBox(String identifier) {

        CollectionBox collectionBox = collectionBoxRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Collection box not found with identifier: " + identifier));

        collectionBoxRepository.delete(collectionBox);
        return new MessageResponseDto("Collection box with identifier " + identifier + " deleted successfully");
    }

    @Transactional
    public MessageResponseDto assignCollectionBoxToEvent(AssignCollectionBoxDto assignCollectionBoxDto) {
        CollectionBox collectionBox = collectionBoxRepository.findByIdentifier(assignCollectionBoxDto.getCollectionBoxIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("Collection box not found with identifier: " + assignCollectionBoxDto.getCollectionBoxIdentifier()));

        FundraisingEvent fundraisingEvent = fundraisingEventRepository.findById(assignCollectionBoxDto.getFundraisingEventId())
                .orElseThrow(() -> new IllegalArgumentException("Fundraising event not found with id: " + assignCollectionBoxDto.getFundraisingEventId()));

        if (!collectionBox.getIsEmpty()) {
            throw new IllegalArgumentException("Non-empty box cannot be assigned to a fundraising event");
        }

        collectionBox.setFundraisingEvent(fundraisingEvent);
        collectionBoxRepository.save(collectionBox);

        return new MessageResponseDto("Collection box with identifier " + collectionBox.getIdentifier()
                + " successfully assigned to event " + fundraisingEvent.getName());
    }

    @Transactional
    public MessageResponseDto addMoneyToCollectionBox(AddMoneyDto addMoneyDto) {
        CollectionBox collectionBox = collectionBoxRepository.findByIdentifier(addMoneyDto.getCollectionBoxIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("Collection box not found with identifier: " + addMoneyDto.getCollectionBoxIdentifier()));

        if (collectionBox.getFundraisingEvent() == null) {
            throw new IllegalArgumentException("You can only add money to a box that is assigned to a fundraising event");
        }

        Currency currency = currencyRepository.findByCode(addMoneyDto.getCurrencyCode())
                .orElseThrow(() -> new IllegalArgumentException("Currency not supported: " + addMoneyDto.getCurrencyCode()));

        Optional<CollectionBoxMoney> existingMoney = collectionBox.getMoney().stream()
                .filter(money -> money.getCurrency().getCode().equals(addMoneyDto.getCurrencyCode()))
                .findFirst();

        if (existingMoney.isPresent()) {
            CollectionBoxMoney money = existingMoney.get();
            money.setAmount(money.getAmount().add(addMoneyDto.getAmount()));
        } else {
            CollectionBoxMoney money = CollectionBoxMoney.builder()
                    .collectionBox(collectionBox)
                    .currency(currency)
                    .amount(addMoneyDto.getAmount())
                    .build();

            collectionBox.getMoney().add(money);
        }

        collectionBox.setIsEmpty(false);
        collectionBoxRepository.save(collectionBox);

        return new MessageResponseDto(
                "Amount " + addMoneyDto.getAmount()
                        + " " + addMoneyDto.getCurrencyCode()
                        + " added to collection box with identifier "
                        + addMoneyDto.getCollectionBoxIdentifier()
        );
    }

    @Transactional
    public MessageResponseDto emptyCollectionBox(String identifier) {
        CollectionBox collectionBox = collectionBoxRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Collection box not found"));

        if (collectionBox.getIsEmpty()) {
            throw new IllegalArgumentException("Collection box has no money inside");
        }

        if (collectionBox.getFundraisingEvent() == null) {
            throw new IllegalArgumentException("Collection box is not assigned to an event");
        }

        FundraisingEvent event = collectionBox.getFundraisingEvent();
        Currency eventCurrency = event.getCurrency();

        BigDecimal totalAmountInTheBox = BigDecimal.ZERO;

        for (CollectionBoxMoney money : collectionBox.getMoney()) {
            BigDecimal amountForCurrency;

            BigDecimal exchangeRate = getExchangeRate(money.getCurrency(), eventCurrency);
            amountForCurrency = money.getAmount().multiply(exchangeRate);

            totalAmountInTheBox = totalAmountInTheBox.add(amountForCurrency);
        }

        event.setAccountBalance(event.getAccountBalance().add(totalAmountInTheBox));
        fundraisingEventRepository.save(event);

        collectionBox.getMoney().clear();
        collectionBox.setIsEmpty(true);
        collectionBoxRepository.save(collectionBox);

        return new MessageResponseDto(
                "Collection box with identifier " + collectionBox.getIdentifier() +
                " has been emptied. Amount " + totalAmountInTheBox.setScale(2, RoundingMode.HALF_UP) + " " +
                eventCurrency.getCode() + " has been transfered to " + event.getName()
        );

    }

    private BigDecimal getExchangeRate(Currency fromCurrency, Currency toCurrency) {
        if (fromCurrency.getCode().equals(toCurrency.getCode())) {
            return BigDecimal.ONE;
        }

        return exchangeRateRepository.findByFromCurrencyAndToCurrency(fromCurrency, toCurrency)
                .map(ExchangeRate::getRate)
                .orElseThrow(() -> new IllegalStateException(
                        "Exchange rate not found for currency pair: " + fromCurrency.getCode() + " to " + toCurrency.getCode()
                ));
    }
}
