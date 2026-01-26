package com.tms.restapi.toolsmanagement.reports.controller;

import com.tms.restapi.toolsmanagement.reports.dto.DashboardOverviewDTO;
import com.tms.restapi.toolsmanagement.reports.dto.IssuanceStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.LocationStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.ToolStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    /**
     * Get overall tool statistics
     * Returns: total tools, available, unavailable, availability %, needing calibration, damaged
     */
    @GetMapping("/tools/statistics")
    public ResponseEntity<?> getToolStatistics() {
        try {
            ToolStatisticsDTO stats = reportsService.getToolStatistics();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve tool statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Get issuance statistics
     * Returns: total issuances, issued, returned, pending, approved, pending approvals, rejected
     */
    @GetMapping("/issuance/statistics")
    public ResponseEntity<?> getIssuanceStatistics() {
        try {
            IssuanceStatisticsDTO stats = reportsService.getIssuanceStatistics();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve issuance statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Get statistics grouped by location
     * Returns: location-wise tool counts and availability
     */
    @GetMapping("/location/statistics")
    public ResponseEntity<?> getLocationStatistics() {
        try {
            List<LocationStatisticsDTO> stats = reportsService.getLocationStatistics();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", stats);
                put("total", stats.size());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve location statistics: " + e.getMessage());
            }});
        }
    }

    /**
     * Get dashboard overview with key metrics
     * Returns: total tools, issuances, trainers, admins, availability %, pending approvals, tools needing maintenance
     */
    @GetMapping("/dashboard/overview")
    public ResponseEntity<?> getDashboardOverview() {
        try {
            DashboardOverviewDTO overview = reportsService.getDashboardOverview();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", overview);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve dashboard overview: " + e.getMessage());
            }});
        }
    }

    /**
     * Get top issued tools (most frequently issued)
     * @param limit number of top tools to return (default: 10)
     */
    @GetMapping("/top-issued-tools")
    public ResponseEntity<?> getTopIssuedTools(@RequestParam(defaultValue = "10") int limit) {
        try {
            if (limit <= 0 || limit > 100) {
                limit = 10;
            }

            List<Map<String, Object>> topTools = reportsService.getTopIssuedTools(limit);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", topTools);
                put("total", topTools.size());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve top issued tools: " + e.getMessage());
            }});
        }
    }

    /**
     * Get monthly issuance trend for last 12 months
     * Returns: monthly issue counts and return counts for chart visualization
     */
    @GetMapping("/monthly-trend")
    public ResponseEntity<?> getMonthlyTrend() {
        try {
            List<Map<String, Object>> trend = reportsService.getMonthlyIssuanceTrend();
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("data", trend);
                put("total", trend.size());
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve monthly trend: " + e.getMessage());
            }});
        }
    }

    /**
     * Get comprehensive analytical data for dashboard
     * Combines all statistics in one endpoint for efficient loading
     */
    @GetMapping("/comprehensive")
    public ResponseEntity<?> getComprehensiveReport() {
        try {
            ToolStatisticsDTO toolStats = reportsService.getToolStatistics();
            IssuanceStatisticsDTO issuanceStats = reportsService.getIssuanceStatistics();
            DashboardOverviewDTO overview = reportsService.getDashboardOverview();
            List<LocationStatisticsDTO> locationStats = reportsService.getLocationStatistics();

            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("toolStatistics", toolStats);
                put("issuanceStatistics", issuanceStats);
                put("dashboardOverview", overview);
                put("locationStatistics", locationStats);
            }});

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new HashMap<String, String>() {{
                put("error", "Failed to retrieve comprehensive report: " + e.getMessage());
            }});
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new HashMap<String, String>() {{
            put("status", "Reports service is running");
        }});
    }
}
