package com.example.charity_collection.service;

import com.example.charity_collection.dto.FinancialReportDto;
import com.example.charity_collection.repository.FundraisingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final FundraisingEventRepository fundraisingEventRepository;

    public ReportService(FundraisingEventRepository fundraisingEventRepository) {
        this.fundraisingEventRepository = fundraisingEventRepository;
    }

    @Transactional(readOnly = true)
    public List<FinancialReportDto> generateFinancialReport() {
        return fundraisingEventRepository.findAll().stream()
                .map(event -> FinancialReportDto.builder()
                        .eventName(event.getName())
                        .amount(event.getAccountBalance())
                        .currency(event.getCurrency().getCode())
                        .build())
                .collect(Collectors.toList());
    }
}
