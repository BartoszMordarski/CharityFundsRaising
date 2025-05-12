package com.example.charity_collection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionBoxDto {
    private Long id;

    @NotBlank(message = "An identifier is required")
    private String identifier;

    private Boolean isEmpty = true;
}
