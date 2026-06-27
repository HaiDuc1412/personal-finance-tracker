package com.haiduc.personalfinancetracker.category;

import com.haiduc.personalfinancetracker.category.dto.CategoryRequest;
import com.haiduc.personalfinancetracker.category.dto.CategoryResponse;
import com.haiduc.personalfinancetracker.common.ApiResponse;
import com.haiduc.personalfinancetracker.common.enums.TransactionType;
import com.haiduc.personalfinancetracker.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories available for current user")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) TransactionType type) {

        List<CategoryResponse> data = categoryService.getCategories(currentUser, type);
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved", data));
    }

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse data = categoryService.createCategory(currentUser, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", data));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse data = categoryService.updateCategory(currentUser, id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated", data));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category (transactions will be moved to Uncategorized)")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        categoryService.deleteCategory(currentUser, id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
