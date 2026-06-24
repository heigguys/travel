package com.two.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ConsultationOverviewResponse(
        int unreadTotal,
        List<EmployeeOverview> employees
) {
    public record EmployeeOverview(
            Long participantUserId,
            String employeeNo,
            String userName,
            int unreadCount,
            List<PlanOverview> plans
    ) {
    }

    public record PlanOverview(
            Long planId,
            String planNo,
            String destination,
            LocalDateTime latestCreatedAt,
            int unreadCount
    ) {
    }
}
