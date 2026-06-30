package com.haiduc.personalfinancetracker.budget;

import com.haiduc.personalfinancetracker.budget.dto.BudgetRequest;
import com.haiduc.personalfinancetracker.budget.dto.BudgetResponse;
import com.haiduc.personalfinancetracker.common.ApiResponse;
import com.haiduc.personalfinancetracker.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
@Tag(name = "Budget", description = "Budget management APIs")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Create a new budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.createBudget(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Budget created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get budgets (default: current month)")
    public ResponseEntity<ApiResponse<List<BudgetResponse>>> getBudgets(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Short month,
            @RequestParam(required = false) Short year) {
        List<BudgetResponse> response = budgetService.getBudgets(currentUser, month, year);
        return ResponseEntity.ok(ApiResponse.success("Budgets retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a budget by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudget(@PathVariable UUID id) {
        BudgetResponse response = budgetService.getBudget(id);
        return ResponseEntity.ok(ApiResponse.success("Budget retrieved successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request) {
        BudgetResponse response = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable UUID id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }
}
