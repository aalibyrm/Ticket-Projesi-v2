import { useCallback, useEffect, useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { listCustomerTickets } from "../../api/ticketApi";
import type { TicketResponse } from "../../api/mobileApiTypes";
import { Chip, EmptyState, ErrorState, StatusPill, primitiveStyles } from "../../components/MobilePrimitives";
import { colors, spacing, typography } from "../../theme/tokens";
import { formatDate, priorityLabel, statusLabel } from "../../utils/formatters";
import { filterTicketsByStatus, type TicketStatusFilter } from "../../utils/ticketFilters";

const filters: { key: TicketStatusFilter; label: string }[] = [
  { key: "ALL", label: "Tumu" },
  { key: "OPEN", label: "Acik" },
  { key: "PENDING", label: "Beklemede" },
  { key: "CLOSED", label: "Kapali" }
];

export function CustomerTicketsScreen({ onOpenTicket }: { onOpenTicket: (ticketId: string) => void }) {
  const [filter, setFilter] = useState<TicketStatusFilter>("ALL");
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();

  const loadTickets = useCallback(async () => {
    setLoading(true);
    setError(undefined);

    try {
      setTickets(await listCustomerTickets());
    } catch {
      setError("Talepler yuklenemedi.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadTickets();
  }, [loadTickets]);

  const visibleTickets = filterTicketsByStatus(tickets, filter);

  return (
    <View style={styles.container}>
      <View style={styles.filters}>
        {filters.map((item) => (
          <Chip
            active={filter === item.key}
            key={item.key}
            label={item.label}
            onPress={() => setFilter(item.key)}
          />
        ))}
      </View>

      {loading ? <EmptyState message="Talepler yukleniyor." /> : undefined}
      {error ? <ErrorState message={error} onRetry={loadTickets} /> : undefined}

      {!loading && !error ? (
        <ScrollView contentContainerStyle={styles.list}>
          {visibleTickets.length === 0 ? <EmptyState message="Bu filtrede talep yok." /> : undefined}
          {visibleTickets.map((ticket) => (
            <TicketRow key={ticket.id} onPress={() => onOpenTicket(ticket.id)} ticket={ticket} />
          ))}
        </ScrollView>
      ) : undefined}
    </View>
  );
}

function TicketRow({ onPress, ticket }: { onPress: () => void; ticket: TicketResponse }) {
  return (
    <Pressable accessibilityRole="button" onPress={onPress} style={styles.row}>
      <View style={styles.rowMain}>
        <Text numberOfLines={1} style={styles.rowTitle}>{ticket.summary}</Text>
        <Text numberOfLines={1} style={styles.rowMeta}>
          #{ticket.ticketNumber} · {formatDate(ticket.createdAt)} · {priorityLabel(ticket.priority)}
        </Text>
      </View>
      <StatusPill label={statusLabel(ticket.status)} tone={ticket.priority === "HIGH" ? "danger" : "neutral"} />
    </Pressable>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  filters: {
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.sm,
    padding: spacing.md
  },
  list: {
    paddingBottom: spacing.xl
  },
  row: {
    alignItems: "center",
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md
  },
  rowMain: {
    flex: 1,
    gap: spacing.xs
  },
  rowMeta: {
    ...typography.body,
    color: colors.textMuted
  },
  rowTitle: {
    ...typography.heading,
    color: colors.text,
    fontSize: 18
  },
  title: primitiveStyles.sectionTitle
});
