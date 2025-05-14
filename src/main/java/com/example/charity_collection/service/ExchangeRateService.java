package com.example.charity_collection.service;

import com.example.charity_collection.dto.NbpApiDto;
import com.example.charity_collection.model.Currency;
import com.example.charity_collection.model.ExchangeRate;
import com.example.charity_collection.repository.CurrencyRepository;
import com.example.charity_collection.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final String NBP_API_URL = "https://api.nbp.pl/api/exchangerates/tables/A?format=json";

    private final RestTemplate restTemplate;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(CurrencyRepository currencyRepository,
                               ExchangeRateRepository exchangeRateRepository,
                               RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("Initializing exchange rates on application startup");
        updateExchangeRates();
    }

    @Scheduled(cron = "0 0 14 * * ?")
    @Transactional
    public void updateExchangeRates() {
        logger.info("Starting daily exchange rates update");

        try {
            NbpApiDto[] response = restTemplate.getForObject(NBP_API_URL, NbpApiDto[].class);

            if (response.length > 0) {
                NbpApiDto apiData = response[0];

                Currency pln = currencyRepository.findByCode("PLN")
                        .orElseThrow(() -> new IllegalStateException("PLN currency not found in database"));

                for (Currency currency : currencyRepository.findAll()) {
                    if ("PLN".equals(currency.getCode())) {
                        continue;
                    }

                    Optional<NbpApiDto.RateDto> rateDto = apiData.getRates().stream()
                            .filter(rate -> rate.getCode().equals(currency.getCode()))
                            .findFirst();

                    if (rateDto.isPresent()) {
                        BigDecimal currencyToPlnRate = new BigDecimal(rateDto.get().getMid());
                        saveOrUpdateExchangeRate(pln, currency, BigDecimal.ONE.divide(currencyToPlnRate, 6, RoundingMode.HALF_UP));
                        saveOrUpdateExchangeRate(currency, pln, currencyToPlnRate);
                        updateCrossRates(currency, currencyToPlnRate);
                    } else {
                        logger.warn("Rate for currency " + currency.getCode() + " not found in NBP API response");
                    }
                }

                logger.info("Exchange rates successfully updated");
            } else {
                logger.error("Invalid response format from NBP API");
            }
        } catch (RestClientException e) {
            logger.error("Failed to fetch exchange rates", e);
        } catch (Exception e) {
            logger.error("Error occured", e);
        }
    }


    private void saveOrUpdateExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate) {

        ExchangeRate exchangeRate = exchangeRateRepository
                .findByFromCurrencyAndToCurrency(fromCurrency, toCurrency)
                .orElse(new ExchangeRate());

        exchangeRate.setFromCurrency(fromCurrency);
        exchangeRate.setToCurrency(toCurrency);
        exchangeRate.setRate(rate);

        exchangeRateRepository.save(exchangeRate);
    }

    private void updateCrossRates(Currency baseCurrency, BigDecimal plnRate) {

        for(Currency targetCurrency : currencyRepository.findAll()) {
            if(baseCurrency.equals(targetCurrency) || "PLN".equals(targetCurrency.getCode())) {
                continue;
            }

            Optional<ExchangeRate> targetToPlnRate = exchangeRateRepository.findByFromCurrencyAndToCurrency(
                    targetCurrency,
                    currencyRepository.findByCode("PLN").orElseThrow()
            );

            if(targetToPlnRate.isPresent()) {
                BigDecimal crossRate = plnRate.divide(targetToPlnRate.get().getRate(), 6, RoundingMode.HALF_UP);
                saveOrUpdateExchangeRate(baseCurrency, targetCurrency, crossRate);

                BigDecimal reversedCrossRate = BigDecimal.ONE.divide(crossRate, 6, RoundingMode.HALF_UP);
                saveOrUpdateExchangeRate(targetCurrency, baseCurrency, reversedCrossRate);
            }
        }
    }

    public void manuallyUpdateExchangeRates() {
        updateExchangeRates();
    }

}



