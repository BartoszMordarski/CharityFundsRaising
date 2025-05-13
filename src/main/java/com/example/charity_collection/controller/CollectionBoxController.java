package com.example.charity_collection.controller;

import com.example.charity_collection.dto.*;
import com.example.charity_collection.service.CollectionBoxService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/collection-boxes")
public class CollectionBoxController {

    private final CollectionBoxService collectionBoxService;

    public CollectionBoxController(CollectionBoxService collectionBoxService) {
        this.collectionBoxService = collectionBoxService;
    }

    @PostMapping
    public ResponseEntity<CollectionBoxDto> registerCollectionBox(@Valid @RequestBody CollectionBoxDto collectionBoxDto) {
        CollectionBoxDto registeredBox = collectionBoxService.registerCollectionBox(collectionBoxDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredBox);
    }

    @GetMapping
    public ResponseEntity<List<CollectionBoxInfoDto>> listAllCollectionBoxes() {
        List<CollectionBoxInfoDto> collectionBoxes = collectionBoxService.listAllCollectionBoxes();
        return ResponseEntity.ok(collectionBoxes);
    }

    @DeleteMapping("/{identifier}")
    public ResponseEntity<MessageResponseDto> unregisterCollectionBox(@PathVariable String identifier) {
        MessageResponseDto response = collectionBoxService.unregisterCollectionBox(identifier);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assign")
    public ResponseEntity<MessageResponseDto> assignCollectionBoxToEvent(@Valid @RequestBody AssignCollectionBoxDto assignCollectionBoxDto) {
        MessageResponseDto response = collectionBoxService.assignCollectionBoxToEvent(assignCollectionBoxDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add")
    public ResponseEntity<MessageResponseDto> addMoneyToCollectionBox(@Valid @RequestBody AddMoneyDto addMoneyDto) {
        MessageResponseDto response = collectionBoxService.addMoneyToCollectionBox(addMoneyDto);
        return ResponseEntity.ok(response);
    }
}
