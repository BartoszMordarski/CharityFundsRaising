package com.example.charity_collection.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionBoxInfoDto {
    private Long id;
    private String identifier;
    private boolean isEmpty;
    private boolean isAssigned;
}
