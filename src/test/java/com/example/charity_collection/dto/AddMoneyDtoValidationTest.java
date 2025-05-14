package com.example.charity_collection.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AddMoneyDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationForValidDto() {
        AddMoneyDto validDto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .amount(new BigDecimal("100.00"))
                .currencyCode("PLN")
                .build();

        Set<ConstraintViolation<AddMoneyDto>> violations = validator.validate(validDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenCollectionBoxIdentifierIsBlank() {
        AddMoneyDto invalidDto = AddMoneyDto.builder()
                .collectionBoxIdentifier("")
                .amount(new BigDecimal("100.00"))
                .currencyCode("PLN")
                .build();

        Set<ConstraintViolation<AddMoneyDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Box identifier is required"));
    }

    @Test
    void shouldFailValidationWhenCollectionBoxIdentifierIsNull() {
        AddMoneyDto invalidDto = AddMoneyDto.builder()
                .collectionBoxIdentifier(null)
                .amount(new BigDecimal("100.00"))
                .currencyCode("PLN")
                .build();

        Set<ConstraintViolation<AddMoneyDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Box identifier is required"));
    }

    @Test
    void shouldFailValidationWhenAmountIsNull() {
        AddMoneyDto invalidDto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .amount(null)
                .currencyCode("PLN")
                .build();

        Set<ConstraintViolation<AddMoneyDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Amount is required"));
    }

    @Test
    void shouldFailValidationWhenAmountIsZero() {
        AddMoneyDto invalidDto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .amount(new BigDecimal("0.00"))
                .currencyCode("PLN")
                .build();

        Set<ConstraintViolation<AddMoneyDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Amount must be greater than 0"));
    }

    @Test
    void shouldFailValidationWhenCurrencyCodeIsNull() {
        AddMoneyDto invalidDto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .amount(new BigDecimal("100.00"))
                .currencyCode(null)
                .build();

        Set<ConstraintViolation<AddMoneyDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Currency code is required"));
    }

    @Test
    void shouldFailValidationWhenCurrencyCodeIsInvalidFormat() {
        AddMoneyDto invalidDto = AddMoneyDto.builder()
                .collectionBoxIdentifier("BOX123")
                .amount(new BigDecimal("100.00"))
                .currencyCode("pln")
                .build();

        Set<ConstraintViolation<AddMoneyDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Currency code has to contain exactly 3 uppercase letters"));
    }
}