package com.example.charity_collection.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignCollectionBoxDto {

    @NotBlank(message = "Box identifier is required")
    private String collectionBoxIdentifier;

    @NotNull(message = "Event id is required")
    private Long fundraisingEventId;
}
