package com.haiduc.personalfinancetracker.transaction;

import com.haiduc.personalfinancetracker.common.ApiResponse;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.transaction.dto.TransactionRequest;
import com.haiduc.personalfinancetracker.transaction.dto.TransactionResponse;
import com.haiduc.personalfinancetracker.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get transactions with optional filters and pagination")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<TransactionResponse> data = transactionService
                .getTransactions(currentUser, type, categoryId, fromDate, toDate, page, size);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", data));
    }

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse data = transactionService.createTransaction(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created", data));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateTransaction(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody TransactionRequest request) {

        TransactionResponse data = transactionService.updateTransaction(currentUser, id, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction updated", data));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a transaction")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        transactionService.deleteTransaction(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Transaction deleted", null));
    }
}
