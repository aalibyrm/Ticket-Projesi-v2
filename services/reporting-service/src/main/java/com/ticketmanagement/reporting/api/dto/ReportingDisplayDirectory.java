package com.ticketmanagement.reporting.api.dto;

import java.util.Map;
import java.util.UUID;

final class ReportingDisplayDirectory {

    private static final Map<UUID, AgentDisplay> AGENTS = Map.ofEntries(
            agent("40000000-0000-0000-0000-000000000001", "Elif Aydin"),
            agent("40000000-0000-0000-0000-000000000002", "Mert Kaya"),
            agent("40000000-0000-0000-0000-000000000003", "Deniz Arslan"),
            agent("40000000-0000-0000-0000-000000000004", "Selin Demir"),
            agent("40000000-0000-0000-0000-000000000005", "Baran Yilmaz"),
            agent("40000000-0000-0000-0000-000000000006", "Ece Sahin"),
            agent("40000000-0000-0000-0000-000000000007", "Onur Demir"),
            agent("40000000-0000-0000-0000-000000000008", "Zeynep Ozturk"),
            agent("40000000-0000-0000-0000-000000000009", "Seda Erdem"));

    private static final Map<UUID, TeamDisplay> TEAMS = Map.ofEntries(
            team("20000000-0000-0000-0000-000000000001", "IDENTITY_OPERATIONS", "Identity Operations"),
            team("20000000-0000-0000-0000-000000000002", "PERMISSION_OPERATIONS", "Permission Operations"),
            team("20000000-0000-0000-0000-000000000003", "WEB_APP_SUPPORT", "Web App Support"),
            team("20000000-0000-0000-0000-000000000004", "CORE_APP_SUPPORT", "Core App Support"),
            team("20000000-0000-0000-0000-000000000005", "NETWORK_OPERATIONS", "Network Operations"),
            team("20000000-0000-0000-0000-000000000006", "PLATFORM_OPERATIONS", "Platform Operations"),
            team("20000000-0000-0000-0000-000000000007", "BILLING_OPERATIONS", "Billing Operations"),
            team("20000000-0000-0000-0000-000000000008", "PAYMENT_OPERATIONS_1", "Payment Operations 1"),
            team("20000000-0000-0000-0000-000000000009", "PAYMENT_OPERATIONS_2", "Payment Operations 2"));

    private ReportingDisplayDirectory() {
    }

    static String agentDisplayName(UUID agentId) {
        AgentDisplay display = AGENTS.get(agentId);
        if (display != null) {
            return display.displayName();
        }
        return "Agent " + shortId(agentId);
    }

    static String teamCode(UUID teamId) {
        TeamDisplay display = TEAMS.get(teamId);
        return display == null ? null : display.code();
    }

    static String teamName(UUID teamId) {
        TeamDisplay display = TEAMS.get(teamId);
        return display == null ? "Team " + shortId(teamId) : display.name();
    }

    private static Map.Entry<UUID, AgentDisplay> agent(String id, String displayName) {
        return Map.entry(UUID.fromString(id), new AgentDisplay(displayName));
    }

    private static Map.Entry<UUID, TeamDisplay> team(String id, String code, String name) {
        return Map.entry(UUID.fromString(id), new TeamDisplay(code, name));
    }

    private static String shortId(UUID id) {
        return id.toString().substring(0, 8);
    }

    private record AgentDisplay(String displayName) {
    }

    private record TeamDisplay(String code, String name) {
    }
}
