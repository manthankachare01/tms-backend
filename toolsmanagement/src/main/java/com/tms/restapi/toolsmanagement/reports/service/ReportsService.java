package com.tms.restapi.toolsmanagement.reports.service;

import com.tms.restapi.toolsmanagement.reports.dto.DashboardOverviewDTO;
import com.tms.restapi.toolsmanagement.reports.dto.IssuanceStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.LocationStatisticsDTO;
import com.tms.restapi.toolsmanagement.reports.dto.ToolStatisticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ReportsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Get overall tool statistics for charts
     */
    public ToolStatisticsDTO getToolStatistics() {
        try {
            // Total tools
            Long totalTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools",
                Long.class
            );

            // Available tools (where availability > 0)
            Long availableTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE availability > 0",
                Long.class
            );

            // Unavailable tools (where availability = 0)
            Long unavailableTools = totalTools - availableTools;

            // Availability percentage
            Double availabilityPercentage = totalTools > 0 ?
                (double) (availableTools * 100) / totalTools : 0.0;

            // Tools needing calibration
            Long toolsNeedingCalibration = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE calibration_required = true",
                Long.class
            );

            // Damaged tools
            Long damagedTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE tool_condition = 'Damaged'",
                Long.class
            );

            return new ToolStatisticsDTO(
                totalTools,
                availableTools,
                unavailableTools,
                Math.round(availabilityPercentage * 100.0) / 100.0,
                toolsNeedingCalibration,
                damagedTools
            );

        } catch (Exception e) {
            // Return default values if query fails
            return new ToolStatisticsDTO(0L, 0L, 0L, 0.0, 0L, 0L);
        }
    }

    /**
     * Get issuance statistics for charts
     */
    public IssuanceStatisticsDTO getIssuanceStatistics() {
        try {
            // Total issuances
            Long totalIssuances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance",
                Long.class
            );

            // Issued tools (status = 'Issued')
            Long issuedTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE status = 'Issued' OR status = 'issued'",
                Long.class
            );

            // Returned tools (status = 'Returned')
            Long returnedTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE status = 'Returned' OR status = 'returned'",
                Long.class
            );

            // Pending returns (status = 'Issued' but return_date is null or in future)
            Long pendingReturns = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE (status = 'Issued' OR status = 'issued') AND (return_date IS NULL OR return_date > NOW())",
                Long.class
            );

            // Approved issuances (approval_status = 'Approved')
            Long approvedIssuances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE approval_status = 'Approved' OR approval_status = 'approved'",
                Long.class
            );

            // Pending approvals (approval_status = 'Pending')
            Long pendingApprovals = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE approval_status = 'Pending' OR approval_status = 'pending'",
                Long.class
            );

            // Rejected issuances (approval_status = 'Rejected')
            Long rejectedIssuances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE approval_status = 'Rejected' OR approval_status = 'rejected'",
                Long.class
            );

            return new IssuanceStatisticsDTO(
                totalIssuances,
                issuedTools,
                returnedTools,
                pendingReturns,
                approvedIssuances,
                pendingApprovals,
                rejectedIssuances
            );

        } catch (Exception e) {
            // Return default values if query fails
            return new IssuanceStatisticsDTO(0L, 0L, 0L, 0L, 0L, 0L, 0L);
        }
    }

    /**
     * Get statistics by location
     */
    public List<LocationStatisticsDTO> getLocationStatistics() {
        try {
            String sql = "SELECT " +
                        "l.location, " +
                        "COUNT(DISTINCT t.id) as total_tools, " +
                        "COUNT(DISTINCT CASE WHEN t.availability > 0 THEN t.id END) as available_tools, " +
                        "COUNT(DISTINCT CASE WHEN i.status = 'Issued' OR i.status = 'issued' THEN i.id END) as issued_tools, " +
                        "ROUND((COUNT(DISTINCT CASE WHEN t.availability > 0 THEN t.id END) * 100.0 / COUNT(DISTINCT t.id)), 2) as availability_percentage " +
                        "FROM tools t " +
                        "LEFT JOIN issuance i ON t.id = i.tool_id " +
                        "CROSS JOIN (SELECT DISTINCT location FROM tools) l " +
                        "WHERE t.location = l.location " +
                        "GROUP BY l.location";

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);

            return results.stream().map(row ->
                new LocationStatisticsDTO(
                    (String) row.get("location"),
                    ((Number) row.get("total_tools")).longValue(),
                    ((Number) row.get("available_tools")).longValue(),
                    ((Number) row.get("issued_tools")).longValue(),
                    ((Number) row.get("availability_percentage")).doubleValue()
                )
            ).toList();

        } catch (Exception e) {
            // Return empty list if query fails
            return List.of();
        }
    }

    /**
     * Get dashboard overview with key metrics
     */
    public DashboardOverviewDTO getDashboardOverview() {
        try {
            // Total tools
            Long totalTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools",
                Long.class
            );

            // Total issuances
            Long totalIssuances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance",
                Long.class
            );

            // Total trainers
            Long totalTrainers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM trainer",
                Long.class
            );

            // Total admins
            Long totalAdmins = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admin",
                Long.class
            );

            // Tool availability percentage
            Long availableTools = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE availability > 0",
                Long.class
            );
            Double toolAvailabilityPercentage = totalTools > 0 ?
                Math.round((double) (availableTools * 100) / totalTools * 100.0) / 100.0 : 0.0;

            // Pending approvals
            Long pendingApprovals = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM issuance WHERE approval_status = 'Pending' OR approval_status = 'pending'",
                Long.class
            );

            // Tools needing maintenance (calibration or damaged)
            Long toolsNeedingMaintenance = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tools WHERE calibration_required = true OR tool_condition = 'Damaged'",
                Long.class
            );

            return new DashboardOverviewDTO(
                totalTools,
                totalIssuances,
                totalTrainers,
                totalAdmins,
                toolAvailabilityPercentage,
                pendingApprovals,
                toolsNeedingMaintenance
            );

        } catch (Exception e) {
            // Return default values if query fails
            return new DashboardOverviewDTO(0L, 0L, 0L, 0L, 0.0, 0L, 0L);
        }
    }

    /**
     * Get top issued tools (most frequently issued)
     */
    public List<Map<String, Object>> getTopIssuedTools(int limit) {
        try {
            String sql = "SELECT t.id, t.description, t.tool_no, COUNT(i.id) as issue_count " +
                        "FROM tools t " +
                        "LEFT JOIN issuance i ON t.id = i.tool_id " +
                        "GROUP BY t.id, t.description, t.tool_no " +
                        "ORDER BY issue_count DESC " +
                        "LIMIT " + limit;

            return jdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Get monthly issuance trend
     */
    public List<Map<String, Object>> getMonthlyIssuanceTrend() {
        try {
            String sql = "SELECT " +
                        "DATE_FORMAT(issued_date, '%Y-%m') as month, " +
                        "COUNT(*) as issue_count, " +
                        "COUNT(CASE WHEN status = 'Returned' OR status = 'returned' THEN 1 END) as return_count " +
                        "FROM issuance " +
                        "WHERE issued_date >= DATE_SUB(NOW(), INTERVAL 12 MONTH) " +
                        "GROUP BY DATE_FORMAT(issued_date, '%Y-%m') " +
                        "ORDER BY month ASC";

            return jdbcTemplate.queryForList(sql);

        } catch (Exception e) {
            return List.of();
        }
    }
}
