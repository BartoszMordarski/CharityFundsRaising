package com.example.charity_collection.repository;

import com.example.charity_collection.model.Currency;
import com.example.charity_collection.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {

    Optional<ExchangeRate> findByFromCurrencyAndToCurrency(Currency fromCurrency, Currency toCurrency);

}
