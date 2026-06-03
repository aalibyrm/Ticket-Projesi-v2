import { useCallback, useEffect, useState } from "react";
import { Modal, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { addAgentExternalComment, changeAgentTicketStatus, getAgentTicket, listAgentTicketComments } from "../../api/agentApi";
import type { TicketCommentResponse, TicketResponse } from "../../api/mobileApiTypes";
import {
  EmptyState,
  ErrorState,
  HeaderBar,
  PrimaryButton,
  ScreenContainer,
  SecondaryButton,
  StatusPill,
  UnderlineInput,
  primitiveStyles
} from "../../components/MobilePrimitives";
import { colors, radius, spacing, typography } from "../../theme/tokens";
import { formatDate, formatTime, priorityLabel, statusLabel } from "../../utils/formatters";

export function AgentTicketDetailScreen({ onBack, ticketId }: { onBack: () => void; ticketId: string }) {
  const [ticket, setTicket] = useState<TicketResponse | undefined>();
  const [comments, setComments] = useState<TicketCommentResponse[]>([]);
  const [message, setMessage] = useState("");
  const [sheetOpen, setSheetOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | undefined>();

  const loadDetail = useCallback(async () => {
    setLoading(true);
    setError(undefined);

    try {
      const [nextTicket, nextComments] = await Promise.all([
        getAgentTicket(ticketId),
        listAgentTicketComments(ticketId)
      ]);
      setTicket(nextTicket);
      setComments(nextComments);
    } catch {
      setError("Bilet detayi yuklenemedi.");
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  useEffect(() => {
    void loadDetail();
  }, [loadDetail]);

  async function sendReply() {
    const body = message.trim();

    if (!body) {
      return;
    }

    setSaving(true);
    setError(undefined);

    try {
      const created = await addAgentExternalComment(ticketId, body);
      setComments((current) => [...current, created]);
      setMessage("");
    } catch {
      setError("Yanit gonderilemedi.");
    } finally {
      setSaving(false);
    }
  }

  async function closeTicket() {
    setSaving(true);

    try {
      const updated = await changeAgentTicketStatus(ticketId, { status: "RESOLVED" });
      setTicket(updated);
      setSheetOpen(false);
    } catch {
      setError("Bilet kapatilamadi.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <ScreenContainer>
      <HeaderBar
        onBack={onBack}
        right={<Pressable onPress={() => setSheetOpen(true)}><Text style={styles.infoButton}>@</Text></Pressable>}
        title={ticket?.summary ?? "Bilet"}
      />
      {loading ? <EmptyState message="Detay yukleniyor." /> : undefined}
      {error ? <ErrorState message={error} onRetry={loadDetail} /> : undefined}
      {ticket && !loading ? (
        <>
          <ScrollView contentContainerStyle={styles.messages}>
            <View style={styles.priorityLine}>
              <Text style={styles.ticketNumber}>#{ticket.ticketNumber}</Text>
              <StatusPill label={priorityLabel(ticket.priority)} tone={ticket.priority === "HIGH" ? "danger" : "neutral"} />
            </View>
            {comments.map((comment) => (
              <View
                key={comment.id}
                style={[styles.messageBubble, comment.authorId === ticket.customerId ? styles.customerBubble : styles.agentBubble]}
              >
                <Text style={styles.messageText}>{comment.body}</Text>
                <Text style={styles.messageTime}>{formatTime(comment.createdAt)}</Text>
              </View>
            ))}
            {comments.length === 0 ? <EmptyState message="Henuz mesaj yok." /> : undefined}
          </ScrollView>

          <View style={styles.composer}>
            <UnderlineInput
              label="Yanit"
              onChangeText={setMessage}
              placeholder="Mesaj yazin..."
              value={message}
            />
            <PrimaryButton disabled={saving} label={saving ? "..." : "Yanitla"} onPress={sendReply} />
          </View>

          <AgentContextSheet
            onClose={() => setSheetOpen(false)}
            onResolve={() => void closeTicket()}
            open={sheetOpen}
            saving={saving}
            ticket={ticket}
          />
        </>
      ) : undefined}
    </ScreenContainer>
  );
}

function AgentContextSheet({
  onClose,
  onResolve,
  open,
  saving,
  ticket
}: {
  onClose: () => void;
  onResolve: () => void;
  open: boolean;
  saving: boolean;
  ticket: TicketResponse;
}) {
  return (
    <Modal animationType="slide" onRequestClose={onClose} transparent visible={open}>
      <Pressable onPress={onClose} style={styles.sheetBackdrop} />
      <View style={styles.sheet}>
        <View style={styles.sheetHandle} />
        <Text style={styles.sheetTitle}>Musteri context</Text>
        <Text style={styles.sheetMeta}>{ticket.customerId}</Text>
        <View style={primitiveStyles.divider} />
        <InfoRow label="Kategori" value={ticket.productName} />
        <InfoRow label="Oncelik" value={priorityLabel(ticket.priority)} />
        <InfoRow label="Durum" value={statusLabel(ticket.status)} />
        <InfoRow label="Olusturulma" value={formatDate(ticket.createdAt)} />
        <SecondaryButton disabled={saving} label="Bileti kapat" onPress={onResolve} />
        <SecondaryButton destructive label="Ust kademeye aktar" onPress={onClose} />
      </View>
    </Modal>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <View style={styles.infoRow}>
      <Text style={styles.infoLabel}>{label}</Text>
      <Text style={styles.infoValue}>{value}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  agentBubble: {
    alignSelf: "flex-end",
    backgroundColor: colors.surface
  },
  composer: {
    backgroundColor: colors.surface,
    borderTopColor: colors.border,
    borderTopWidth: 1,
    gap: spacing.sm,
    padding: spacing.md
  },
  customerBubble: {
    alignSelf: "flex-start",
    backgroundColor: colors.surfaceMuted
  },
  infoButton: {
    color: colors.textMuted,
    fontSize: 24
  },
  infoLabel: {
    ...typography.body,
    color: colors.textMuted
  },
  infoRow: {
    flexDirection: "row",
    justifyContent: "space-between"
  },
  infoValue: {
    ...typography.body,
    color: colors.text
  },
  messageBubble: {
    borderColor: colors.border,
    borderRadius: radius.md,
    borderWidth: 1,
    gap: spacing.sm,
    maxWidth: "82%",
    padding: spacing.md
  },
  messageText: {
    ...typography.body,
    color: colors.text
  },
  messageTime: {
    ...typography.label,
    color: colors.textMuted
  },
  messages: {
    gap: spacing.md,
    padding: spacing.md
  },
  priorityLine: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between"
  },
  sheet: {
    backgroundColor: colors.surface,
    borderTopLeftRadius: radius.lg,
    borderTopRightRadius: radius.lg,
    bottom: 0,
    gap: spacing.md,
    left: 0,
    padding: spacing.lg,
    position: "absolute",
    right: 0
  },
  sheetBackdrop: {
    backgroundColor: "rgba(0,0,0,0.28)",
    flex: 1
  },
  sheetHandle: {
    alignSelf: "center",
    backgroundColor: colors.border,
    borderRadius: radius.full,
    height: 5,
    width: 72
  },
  sheetMeta: {
    ...typography.body,
    color: colors.textMuted
  },
  sheetTitle: {
    ...typography.heading,
    color: colors.text
  },
  ticketNumber: {
    ...typography.label,
    color: colors.textMuted
  }
});
