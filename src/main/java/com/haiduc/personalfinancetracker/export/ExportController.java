package com.haiduc.personalfinancetracker.export;

import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "Export transactions to CSV or PDF")
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/transactions/csv")
    @Operation(summary = "Export transactions as CSV",
            description = "Download all transactions matching the filter as a UTF-8 CSV file (Excel-compatible)")
    public ResponseEntity<ByteArrayResource> exportCsv(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Month (1–12). Leave empty for all months")
            @RequestParam(required = false) Short month,
            @Parameter(description = "Year (e.g. 2025). Leave empty for all years")
            @RequestParam(required = false) Short year,
            @Parameter(description = "Filter by transaction type: INCOME or EXPENSE")
            @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Filter by category UUID")
            @RequestParam(required = false) UUID categoryId) {

        ExportRequest filter = buildFilter(month, year, type, categoryId);
        byte[] data = exportService.exportCsv(currentUser, filter);
        String filename = exportService.buildFilename(filter, "csv");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(data));
    }

    @GetMapping("/transactions/pdf")
    @Operation(summary = "Export transactions as PDF",
            description = "Download all transactions matching the filter as a formatted PDF report")
    public ResponseEntity<ByteArrayResource> exportPdf(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Month (1–12). Leave empty for all months")
            @RequestParam(required = false) Short month,
            @Parameter(description = "Year (e.g. 2025). Leave empty for all years")
            @RequestParam(required = false) Short year,
            @Parameter(description = "Filter by transaction type: INCOME or EXPENSE")
            @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Filter by category UUID")
            @RequestParam(required = false) UUID categoryId) {

        ExportRequest filter = buildFilter(month, year, type, categoryId);
        byte[] data = exportService.exportPdf(currentUser, filter);
        String filename = exportService.buildFilename(filter, "pdf");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(new ByteArrayResource(data));
    }

    private ExportRequest buildFilter(Short month, Short year, TransactionType type, UUID categoryId) {
        ExportRequest filter = new ExportRequest();
        filter.setMonth(month);
        filter.setYear(year);
        filter.setType(type);
        filter.setCategoryId(categoryId);
        return filter;
    }
}
