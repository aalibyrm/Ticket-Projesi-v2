import { useCallback, useEffect, useMemo, useState } from "react";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import type {
  AgentPerformanceReportResponse,
  ClosedTicketDateRangeResponse,
  SlaComplianceReportResponse,
  TicketStatusDistributionResponse
} from "../../api/mobileApiTypes";
import {
  getAgentPerformanceReport,
  getClosedTicketReport,
  getSlaComplianceReport,
  getTicketStatusDistribution
} from "../../api/reportApi";
import { EmptyState, ErrorState } from "../../components/MobilePrimitives";
import { colors, spacing, typography } from "../../theme/tokens";
import { minutesToShortDuration } from "../../utils/formatters";

export function ManagerSummaryScreen() {
  const [statusDistribution, setStatusDistribution] = useState<TicketStatusDistributionResponse | undefined>();
  const [closedReport, setClosedReport] = useState<ClosedTicketDateRangeResponse | undefined>();
  const [slaReport, setSlaReport] = useState<SlaComplianceReportResponse | undefined>();
  const [agentReport, setAgentReport] = useState<AgentPerformanceReportResponse | undefined>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();

  const range = useMemo(() => {
    const to = new Date();
    const from = new Date();
    from.setDate(to.getDate() - 30);

    return {
      fromDate: from.toISOString().slice(0, 10),
      toDate: to.toISOString().slice(0, 10)
    };
  }, []);

  const loadReports = useCallback(async () => {
    setLoading(true);
    setError(undefined);

    try {
      const [nextStatus, nextClosed, nextSla, nextAgent] = await Promise.all([
        getTicketStatusDistribution(),
        getClosedTicketReport(range.fromDate, range.toDate),
        getSlaComplianceReport(),
        getAgentPerformanceReport()
      ]);
      setStatusDistribution(nextStatus);
      setClosedReport(nextClosed);
      setSlaReport(nextSla);
      setAgentReport(nextAgent);
    } catch {
      setError("Raporlar yuklenemedi.");
    } finally {
      setLoading(false);
    }
  }, [range.fromDate, range.toDate]);

  useEffect(() => {
    void loadReports();
  }, [loadReports]);

  if (loading) {
    return <EmptyState message="Ozet yukleniyor." />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadReports} />;
  }

  const maxDepartmentCount = Math.max(1, ...(statusDistribution?.departmentCounts ?? []).map((item) => item.count));
  const topAgents = (agentReport?.rows ?? []).slice(0, 4);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.metricBlock}>
        <Text style={styles.metricValue}>{statusDistribution?.totalOpenTickets ?? 0}</Text>
        <Sparkline tone="neutral" />
        <Text style={styles.metricLabel}>Toplam acik bilet</Text>
      </View>

      <View style={styles.metricBlock}>
        <Text style={[styles.metricValue, styles.primaryValue]}>{Number(slaReport?.compliancePercentage ?? 0).toFixed(1)}%</Text>
        <Sparkline tone="primary" />
        <Text style={styles.metricLabel}>SLA uyumu</Text>
      </View>

      <View style={styles.metricBlock}>
        <Text style={styles.metricValue}>{minutesToShortDuration(closedReport?.averageResolutionMinutes)}</Text>
        <Text style={styles.metricLabel}>Ort. cozum suresi</Text>
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Son 30 gun</Text>
        <TrendLine values={(closedReport?.dailyCounts ?? []).map((item) => item.count)} />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Kategori dagilimi</Text>
        {(statusDistribution?.departmentCounts ?? []).map((item) => (
          <View key={item.routedDepartmentId} style={styles.barRow}>
            <Text style={styles.barLabel}>{item.routedDepartmentName || item.routedDepartmentCode || "Departman"}</Text>
            <View style={styles.barTrack}>
              <View style={[styles.barFill, { width: `${Math.max(8, (item.count / maxDepartmentCount) * 100)}%` }]} />
            </View>
            <Text style={styles.barCount}>{item.count}</Text>
          </View>
        ))}
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Ekip performansi</Text>
        {topAgents.map((agent) => (
          <View key={agent.agentId} style={styles.agentRow}>
            <View style={styles.agentAvatar}><Text style={styles.agentAvatarText}>{agent.agentId.slice(0, 2).toUpperCase()}</Text></View>
            <View style={styles.agentBody}>
              <Text numberOfLines={1} style={styles.agentName}>{agent.agentId}</Text>
              <Text style={styles.agentMeta}>{agent.resolvedTicketCount} bilet · {minutesToShortDuration(agent.averageResolutionMinutes)} ort.</Text>
            </View>
            <Text style={styles.agentScore}>{agent.assignedTicketCount}</Text>
          </View>
        ))}
        {topAgents.length === 0 ? <EmptyState message="Ekip verisi yok." /> : undefined}
      </View>
    </ScrollView>
  );
}

function Sparkline({ tone }: { tone: "neutral" | "primary" }) {
  return (
    <View style={styles.sparkline}>
      {[18, 26, 14, 34, 24, 42, 30, 50].map((height, index) => (
        <View
          key={`${height}-${index}`}
          style={[
            styles.sparkSegment,
            { height },
            tone === "primary" && styles.sparkSegmentPrimary
          ]}
        />
      ))}
    </View>
  );
}

function TrendLine({ values }: { values: number[] }) {
  const normalized = values.length > 0 ? values : [2, 5, 4, 9, 8, 12, 15, 14];
  const maxValue = Math.max(1, ...normalized);

  return (
    <View style={styles.trend}>
      {normalized.slice(-12).map((value, index) => (
        <View
          key={`${value}-${index}`}
          style={[styles.trendSegment, { height: 16 + (value / maxValue) * 92 }]}
        />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  agentAvatar: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: 999,
    height: 40,
    justifyContent: "center",
    width: 40
  },
  agentAvatarText: {
    ...typography.label,
    color: colors.text
  },
  agentBody: {
    flex: 1,
    gap: spacing.xs
  },
  agentMeta: {
    ...typography.label,
    color: colors.textMuted
  },
  agentName: {
    ...typography.body,
    color: colors.text,
    fontWeight: "600"
  },
  agentRow: {
    alignItems: "center",
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    paddingVertical: spacing.md
  },
  agentScore: {
    ...typography.heading,
    color: colors.primary
  },
  barCount: {
    ...typography.label,
    color: colors.textMuted,
    width: 44
  },
  barFill: {
    backgroundColor: colors.text,
    borderRadius: 999,
    height: 6
  },
  barLabel: {
    ...typography.label,
    color: colors.text,
    width: 104
  },
  barRow: {
    alignItems: "center",
    flexDirection: "row",
    gap: spacing.md
  },
  barTrack: {
    backgroundColor: colors.border,
    borderRadius: 999,
    flex: 1,
    height: 6
  },
  container: {
    gap: spacing.xl,
    padding: spacing.md,
    paddingBottom: spacing.xl
  },
  metricBlock: {
    gap: spacing.sm
  },
  metricLabel: {
    ...typography.label,
    color: colors.textMuted,
    textTransform: "uppercase"
  },
  metricValue: {
    color: colors.text,
    fontSize: 40,
    fontWeight: "600",
    letterSpacing: 0,
    lineHeight: 44
  },
  primaryValue: {
    color: colors.primary
  },
  section: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
    gap: spacing.md,
    paddingTop: spacing.lg
  },
  sectionTitle: {
    ...typography.heading,
    color: colors.text
  },
  sparkSegment: {
    backgroundColor: colors.textMuted,
    flex: 1,
    maxWidth: 24
  },
  sparkSegmentPrimary: {
    backgroundColor: colors.primary
  },
  sparkline: {
    alignItems: "flex-end",
    flexDirection: "row",
    gap: spacing.sm,
    height: 56
  },
  trend: {
    alignItems: "flex-end",
    flexDirection: "row",
    gap: spacing.sm,
    height: 132
  },
  trendSegment: {
    backgroundColor: colors.text,
    flex: 1,
    maxWidth: 18
  }
});
