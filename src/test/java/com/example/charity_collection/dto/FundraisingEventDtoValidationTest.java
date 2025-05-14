package com.example.charity_collection.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FundraisingEventDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationForValidDto() {
        FundraisingEventDto validDto = FundraisingEventDto.builder()
                .id(1L)
                .name("Charity Run")
                .description("Annual charity event")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode("PLN")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(validDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenNameIsBlank() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name("")
                .description("Annual charity event")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode("PLN")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Event name is required"));
    }

    @Test
    void shouldFailValidationWhenNameIsNull() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name(null)
                .description("Annual charity event")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode("PLN")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Event name is required"));
    }

    @Test
    void shouldFailValidationWhenNameIsTooLong() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name("A".repeat(256))
                .description("Annual charity event")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode("PLN")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Event name must be less than 255 characters"));
    }

    @Test
    void shouldFailValidationWhenDescriptionIsTooLong() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name("Charity Run")
                .description("A".repeat(501))
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode("PLN")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Description must be less than 500 characters"));
    }

    @Test
    void shouldFailValidationWhenStartDateIsNull() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name("Charity Run")
                .description("Annual charity event")
                .startDate(null)
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode("PLN")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Start date is required"));
    }

    @Test
    void shouldFailValidationWhenEndDateIsNull() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name("Charity Run")
                .description("Annual charity event")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(null)
                .currencyCode("PLN")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("End date is required"));
    }

    @Test
    void shouldFailValidationWhenCurrencyCodeIsBlank() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name("Charity Run")
                .description("Annual charity event")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode("")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Currency code is required"));
    }

    @Test
    void shouldFailValidationWhenCurrencyCodeIsNull() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name("Charity Run")
                .description("Annual charity event")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode(null)
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Currency code is required"));
    }

    @Test
    void shouldFailValidationWhenCurrencyCodeIsInvalidLength() {
        FundraisingEventDto invalidDto = FundraisingEventDto.builder()
                .id(1L)
                .name("Charity Run")
                .description("Annual charity event")
                .startDate(LocalDate.of(2025, 6, 1))
                .endDate(LocalDate.of(2025, 6, 2))
                .currencyCode("PL")
                .accountBalance(new BigDecimal("1000.00"))
                .build();

        Set<ConstraintViolation<FundraisingEventDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Currency code must be 3 characters long"));
    }
}
