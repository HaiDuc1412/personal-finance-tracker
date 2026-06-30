package com.haiduc.personalfinancetracker.dashboard;

import com.haiduc.personalfinancetracker.common.ApiResponse;
import com.haiduc.personalfinancetracker.dashboard.dto.DashboardResponse;
import com.haiduc.personalfinancetracker.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard APIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @Operation(summary = "Get dashboard summary (default: current month)")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        DashboardResponse response = dashboardService.getDashboard(currentUser, month, year);
        return ResponseEntity.ok(ApiResponse.success("Dashboard retrieved successfully", response));
    }
}
