package com.example.charity_collection.controller;

import com.example.charity_collection.dto.FinancialReportDto;
import com.example.charity_collection.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/financial")
    public ResponseEntity<List<FinancialReportDto>> getFinancialReport() {
        List<FinancialReportDto> report = reportService.generateFinancialReport();
        return ResponseEntity.ok(report);
    }
}

