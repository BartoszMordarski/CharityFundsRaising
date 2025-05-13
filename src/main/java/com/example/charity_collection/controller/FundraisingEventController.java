package com.example.charity_collection.controller;

import com.example.charity_collection.dto.FundraisingEventDto;
import com.example.charity_collection.service.FundraisingEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fundraising-events")
public class FundraisingEventController {

    private final FundraisingEventService fundraisingEventService;

    public FundraisingEventController(FundraisingEventService fundraisingEventService) {
        this.fundraisingEventService = fundraisingEventService;
    }

    @PostMapping
    public ResponseEntity<FundraisingEventDto> createFundraisingEvent(@Valid @RequestBody FundraisingEventDto eventDTO) {
        FundraisingEventDto createdEvent = fundraisingEventService.createFundraisingEvent(eventDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }
}
