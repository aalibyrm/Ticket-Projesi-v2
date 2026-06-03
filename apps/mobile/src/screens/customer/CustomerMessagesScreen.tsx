import { useCallback, useEffect, useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import type { TicketResponse } from "../../api/mobileApiTypes";
import { listCustomerTickets } from "../../api/ticketApi";
import { EmptyState, ErrorState, UnderlineInput } from "../../components/MobilePrimitives";
import { colors, radius, spacing, typography } from "../../theme/tokens";
import { formatTime, statusLabel } from "../../utils/formatters";
import { filterTicketsBySearch } from "../../utils/ticketFilters";

export function CustomerMessagesScreen({ onOpenTicket }: { onOpenTicket: (ticketId: string) => void }) {
  const [tickets, setTickets] = useState<TicketResponse[]>([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();

  const loadTickets = useCallback(async () => {
    setLoading(true);
    setError(undefined);

    try {
      setTickets(await listCustomerTickets());
    } catch {
      setError("Mesaj listesi yuklenemedi.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadTickets();
  }, [loadTickets]);

  const visibleTickets = filterTicketsBySearch(tickets, search);

  return (
    <View style={styles.container}>
      <View style={styles.search}>
        <UnderlineInput label="Ara" onChangeText={setSearch} placeholder="Bilet ara..." value={search} />
      </View>
      {loading ? <EmptyState message="Mesajlar yukleniyor." /> : undefined}
      {error ? <ErrorState message={error} onRetry={loadTickets} /> : undefined}
      {!loading && !error ? (
        <ScrollView>
          {visibleTickets.map((ticket) => (
            <Pressable key={ticket.id} onPress={() => onOpenTicket(ticket.id)} style={styles.row}>
              <View style={styles.avatar}>
                <Text style={styles.avatarText}>{ticket.productName.slice(0, 2).toUpperCase()}</Text>
              </View>
              <View style={styles.rowBody}>
                <Text numberOfLines={1} style={styles.rowTitle}>{ticket.summary}</Text>
                <Text numberOfLines={1} style={styles.rowMeta}>{statusLabel(ticket.status)} · {ticket.description}</Text>
              </View>
              <Text style={styles.rowTime}>{formatTime(ticket.updatedAt)}</Text>
            </Pressable>
          ))}
          {visibleTickets.length === 0 ? <EmptyState message="Mesaj bulunamadi." /> : undefined}
        </ScrollView>
      ) : undefined}
    </View>
  );
}

const styles = StyleSheet.create({
  avatar: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: radius.full,
    height: 48,
    justifyContent: "center",
    width: 48
  },
  avatarText: {
    ...typography.label,
    color: colors.text
  },
  container: {
    flex: 1
  },
  row: {
    alignItems: "center",
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.md
  },
  rowBody: {
    flex: 1,
    gap: spacing.xs
  },
  rowMeta: {
    ...typography.body,
    color: colors.textMuted
  },
  rowTime: {
    ...typography.label,
    color: colors.textMuted
  },
  rowTitle: {
    ...typography.body,
    color: colors.text,
    fontWeight: "600"
  },
  search: {
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    padding: spacing.md
  }
});
