package com.example.charity_collection.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionBoxDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationForValidDto() {
        CollectionBoxDto validDto = CollectionBoxDto.builder()
                .id(1L)
                .identifier("BOX123")
                .isEmpty(true)
                .build();

        Set<ConstraintViolation<CollectionBoxDto>> violations = validator.validate(validDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenIdentifierIsBlank() {
        CollectionBoxDto invalidDto = CollectionBoxDto.builder()
                .id(1L)
                .identifier("")
                .isEmpty(true)
                .build();

        Set<ConstraintViolation<CollectionBoxDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("An identifier is required"));
    }

    @Test
    void shouldFailValidationWhenIdentifierIsNull() {
        CollectionBoxDto invalidDto = CollectionBoxDto.builder()
                .id(1L)
                .identifier(null)
                .isEmpty(true)
                .build();

        Set<ConstraintViolation<CollectionBoxDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("An identifier is required"));
    }
}