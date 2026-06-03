import { useCallback, useEffect, useMemo, useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { assignAgentTicket, listAgentTickets } from "../../api/agentApi";
import type { TicketPriority, TicketResponse } from "../../api/mobileApiTypes";
import { useAuth } from "../../auth/AuthProvider";
import { Chip, EmptyState, ErrorState, StatusPill, UnderlineInput } from "../../components/MobilePrimitives";
import { colors, spacing, typography } from "../../theme/tokens";
import { formatTime, priorityLabel, statusLabel } from "../../utils/formatters";
import { filterTicketsByPriority, filterTicketsBySearch, type TicketPriorityFilter } from "../../utils/ticketFilters";

const priorityFilters: { key: TicketPriorityFilter; label: string }[] = [
  { key: "ALL", label: "Tumu" },
  { key: "HIGH", label: "Yuksek" },
  { key: "MEDIUM", label: "Normal" },
  { key: "LOW", label: "Dusuk" }
];

export function AgentTicketsScreen({
  mode,
  onOpenTicket
}: {
  mode: "queue" | "assigned";
  onOpenTicket: (ticketId: string) => void;
}) {
  const { user } = useAuth();
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [priority, setPriority] = useState<TicketPriorityFilter>("ALL");
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();
  const [assigningId, setAssigningId] = useState<string | undefined>();

  const loadTickets = useCallback(async () => {
    setLoading(true);
    setError(undefined);

    try {
      setTickets(await listAgentTickets());
    } catch {
      setError("Agent biletleri yuklenemedi.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadTickets();
  }, [loadTickets]);

  const visibleTickets = useMemo(() => {
    const scoped = mode === "queue"
      ? tickets.filter((ticket) => !ticket.assigneeId)
      : tickets.filter((ticket) => ticket.assigneeId === user?.id || ticket.assigneeId);

    return filterTicketsBySearch(filterTicketsByPriority(scoped, priority), search);
  }, [mode, priority, search, tickets, user?.id]);

  async function assignToMe(ticket: TicketResponse) {
    if (!user?.id) {
      setError("Kullanici kimligi okunamadi.");
      return;
    }

    setAssigningId(ticket.id);
    setError(undefined);

    try {
      const updated = await assignAgentTicket(ticket.id, {
        assigneeId: user.id,
        assignedTeamId: ticket.assignedTeamId ?? null
      });
      setTickets((current) => current.map((item) => item.id === updated.id ? updated : item));
    } catch {
      setError("Bilet ustlenilemedi.");
    } finally {
      setAssigningId(undefined);
    }
  }

  return (
    <View style={styles.container}>
      <View style={styles.search}>
        <UnderlineInput label="Ara" onChangeText={setSearch} placeholder="Bilet ara..." value={search} />
      </View>
      <View style={styles.stats}>
        <Text style={styles.statValue}>{visibleTickets.length}</Text>
        <Text style={styles.statLabel}>{mode === "queue" ? "Kuyruk" : "Atanan"}</Text>
        <Text style={styles.statValue}>{tickets.filter((ticket) => ticket.priority === "HIGH").length}</Text>
        <Text style={styles.statLabel}>Acil</Text>
      </View>
      <View style={styles.filters}>
        {priorityFilters.map((item) => (
          <Chip active={priority === item.key} key={item.key} label={item.label} onPress={() => setPriority(item.key)} />
        ))}
      </View>

      {loading ? <EmptyState message="Biletler yukleniyor." /> : undefined}
      {error ? <ErrorState message={error} onRetry={loadTickets} /> : undefined}

      {!loading && !error ? (
        <ScrollView>
          {visibleTickets.length === 0 ? <EmptyState message="Bu listede bilet yok." /> : undefined}
          {visibleTickets.map((ticket) => (
            <AgentTicketRow
              assigning={assigningId === ticket.id}
              key={ticket.id}
              mode={mode}
              onAssign={() => void assignToMe(ticket)}
              onOpen={() => onOpenTicket(ticket.id)}
              ticket={ticket}
            />
          ))}
        </ScrollView>
      ) : undefined}
    </View>
  );
}

function AgentTicketRow({
  assigning,
  mode,
  onAssign,
  onOpen,
  ticket
}: {
  assigning: boolean;
  mode: "queue" | "assigned";
  onAssign: () => void;
  onOpen: () => void;
  ticket: TicketResponse;
}) {
  const accentStyle = ticket.priority === "HIGH" ? styles.highAccent : styles.normalAccent;

  return (
    <Pressable accessibilityRole="button" onPress={mode === "assigned" ? onOpen : undefined} style={styles.row}>
      <View style={[styles.accent, accentStyle]} />
      <View style={styles.rowBody}>
        <Text numberOfLines={1} style={styles.rowTitle}>{ticket.summary}</Text>
        <Text numberOfLines={1} style={styles.rowMeta}>{ticket.productName} · {priorityLabel(ticket.priority)}</Text>
        <Text style={styles.rowSla}>{ticket.priority === "HIGH" ? "1s 23dk kaldi" : formatTime(ticket.updatedAt)}</Text>
      </View>
      {mode === "queue" ? (
        <Pressable accessibilityRole="button" disabled={assigning} onPress={onAssign}>
          <Text style={styles.assignText}>{assigning ? "..." : "Ustlen"}</Text>
        </Pressable>
      ) : (
        <StatusPill label={statusLabel(ticket.status)} tone={ticket.priority === "HIGH" ? "danger" : "neutral"} />
      )}
    </Pressable>
  );
}

const styles = StyleSheet.create({
  accent: {
    bottom: 0,
    left: 0,
    position: "absolute",
    top: 0,
    width: 4
  },
  assignText: {
    ...typography.label,
    color: colors.primary
  },
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
  highAccent: {
    backgroundColor: colors.primary
  },
  normalAccent: {
    backgroundColor: colors.textMuted
  },
  row: {
    alignItems: "center",
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md,
    position: "relative"
  },
  rowBody: {
    flex: 1,
    gap: spacing.xs,
    paddingLeft: spacing.sm
  },
  rowMeta: {
    ...typography.body,
    color: colors.textMuted
  },
  rowSla: {
    ...typography.label,
    color: colors.primary
  },
  rowTitle: {
    ...typography.heading,
    color: colors.text,
    fontSize: 18
  },
  search: {
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    padding: spacing.md
  },
  statLabel: {
    ...typography.body,
    color: colors.textMuted
  },
  statValue: {
    ...typography.heading,
    color: colors.text
  },
  stats: {
    alignItems: "center",
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.sm,
    justifyContent: "center",
    padding: spacing.md
  }
});
