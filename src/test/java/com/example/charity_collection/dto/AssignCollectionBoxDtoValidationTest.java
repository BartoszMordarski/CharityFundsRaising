package com.example.charity_collection.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AssignCollectionBoxDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidationForValidDto() {
        AssignCollectionBoxDto validDto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("BOX123")
                .fundraisingEventId(1L)
                .build();

        Set<ConstraintViolation<AssignCollectionBoxDto>> violations = validator.validate(validDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidationWhenCollectionBoxIdentifierIsBlank() {
        AssignCollectionBoxDto invalidDto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("")
                .fundraisingEventId(1L)
                .build();

        Set<ConstraintViolation<AssignCollectionBoxDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Box identifier is required"));
    }

    @Test
    void shouldFailValidationWhenCollectionBoxIdentifierIsNull() {
        AssignCollectionBoxDto invalidDto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier(null)
                .fundraisingEventId(1L)
                .build();

        Set<ConstraintViolation<AssignCollectionBoxDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Box identifier is required"));
    }

    @Test
    void shouldFailValidationWhenFundraisingEventIdIsNull() {
        AssignCollectionBoxDto invalidDto = AssignCollectionBoxDto.builder()
                .collectionBoxIdentifier("BOX123")
                .fundraisingEventId(null)
                .build();

        Set<ConstraintViolation<AssignCollectionBoxDto>> violations = validator.validate(invalidDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Event id is required"));
    }
}
