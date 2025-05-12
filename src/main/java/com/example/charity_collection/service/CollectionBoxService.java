package com.example.charity_collection.service;

import com.example.charity_collection.dto.CollectionBoxDto;
import com.example.charity_collection.dto.CollectionBoxInfoDto;
import com.example.charity_collection.model.CollectionBox;
import com.example.charity_collection.repository.CollectionBoxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectionBoxService {

    private final CollectionBoxRepository collectionBoxRepository;

    public CollectionBoxService(CollectionBoxRepository collectionBoxRepository) {
        this.collectionBoxRepository = collectionBoxRepository;
    }

    @Transactional
    public CollectionBoxDto registerCollectionBox(CollectionBoxDto collectionBoxDto) {

        if (collectionBoxRepository.findByIdentifier(collectionBoxDto.getIdentifier()).isPresent()) {
            throw new IllegalArgumentException("Collection box with identifier " +
                    collectionBoxDto.getIdentifier() + " already exists");
        }

        CollectionBox collectionBox = CollectionBox.builder()
                .identifier(collectionBoxDto.getIdentifier())
                .isEmpty(true)
                .build();

        CollectionBox savedBox = collectionBoxRepository.save(collectionBox);

        return CollectionBoxDto.builder()
                .id(savedBox.getId())
                .identifier(savedBox.getIdentifier())
                .isEmpty(savedBox.getIsEmpty())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CollectionBoxInfoDto> listAllCollectionBoxes() {
        return collectionBoxRepository.findAll().stream()
                .map(collectionBox -> CollectionBoxInfoDto.builder()
                        .id(collectionBox.getId())
                        .identifier(collectionBox.getIdentifier())
                        .isEmpty(collectionBox.getIsEmpty())
                        .isAssigned(collectionBox.getFundraisingEvent() != null)
                        .build())
                .collect(Collectors.toList());
    }
}
