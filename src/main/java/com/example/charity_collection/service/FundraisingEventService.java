package com.example.charity_collection.service;

import com.example.charity_collection.dto.FundraisingEventDto;
import com.example.charity_collection.model.Currency;
import com.example.charity_collection.model.FundraisingEvent;
import com.example.charity_collection.repository.CurrencyRepository;
import com.example.charity_collection.repository.FundraisingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;


@Service
public class FundraisingEventService {

    private final FundraisingEventRepository fundraisingEventRepository;
    private final CurrencyRepository currencyRepository;

    public FundraisingEventService(FundraisingEventRepository fundraisingEventRepository, CurrencyRepository currencyRepository) {
        this.fundraisingEventRepository = fundraisingEventRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    public FundraisingEventDto createFundraisingEvent(FundraisingEventDto eventDto) {

        if (!eventDto.getStartDate().isBefore(eventDto.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        Currency currency = currencyRepository.findByCode(eventDto.getCurrencyCode())
                .orElseThrow(() -> new IllegalArgumentException("Currency not supported: " + eventDto.getCurrencyCode()));

        FundraisingEvent event = FundraisingEvent.builder()
                .name(eventDto.getName())
                .description(eventDto.getDescription())
                .startDate(eventDto.getStartDate())
                .endDate(eventDto.getEndDate())
                .currency(currency)
                .accountBalance(BigDecimal.ZERO)
                .build();

        event = fundraisingEventRepository.save(event);

        return FundraisingEventDto.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .currencyCode(event.getCurrency().getCode())
                .accountBalance(event.getAccountBalance())
                .build();
    }

}
