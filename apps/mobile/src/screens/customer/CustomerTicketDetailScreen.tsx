import { useCallback, useEffect, useState } from "react";
import { Modal, Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import type { TicketCommentResponse, TicketResponse } from "../../api/mobileApiTypes";
import { addCustomerTicketComment, getCustomerTicket, listCustomerTicketComments } from "../../api/ticketApi";
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

export function CustomerTicketDetailScreen({
  onBack,
  ticketId
}: {
  onBack: () => void;
  ticketId: string;
}) {
  const [ticket, setTicket] = useState<TicketResponse | undefined>();
  const [comments, setComments] = useState<TicketCommentResponse[]>([]);
  const [message, setMessage] = useState("");
  const [sheetOpen, setSheetOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | undefined>();

  const loadDetail = useCallback(async () => {
    setLoading(true);
    setError(undefined);

    try {
      const [nextTicket, nextComments] = await Promise.all([
        getCustomerTicket(ticketId),
        listCustomerTicketComments(ticketId)
      ]);
      setTicket(nextTicket);
      setComments(nextComments);
    } catch {
      setError("Talep detayi yuklenemedi.");
    } finally {
      setLoading(false);
    }
  }, [ticketId]);

  useEffect(() => {
    void loadDetail();
  }, [loadDetail]);

  async function sendMessage() {
    const body = message.trim();

    if (!body) {
      return;
    }

    setSending(true);

    try {
      const created = await addCustomerTicketComment(ticketId, body);
      setComments((current) => [...current, created]);
      setMessage("");
    } catch {
      setError("Mesaj gonderilemedi.");
    } finally {
      setSending(false);
    }
  }

  return (
    <ScreenContainer>
      <HeaderBar
        onBack={onBack}
        right={<Pressable onPress={() => setSheetOpen(true)}><Text style={styles.infoButton}>i</Text></Pressable>}
        title={ticket?.summary ?? "Talep"}
      />

      {loading ? <EmptyState message="Detay yukleniyor." /> : undefined}
      {error ? <ErrorState message={error} onRetry={loadDetail} /> : undefined}

      {ticket && !loading ? (
        <>
          <ScrollView contentContainerStyle={styles.messages}>
            <Text style={styles.ticketNumber}>#{ticket.ticketNumber}</Text>
            {comments.length === 0 ? <EmptyState message="Henuz mesaj yok." /> : undefined}
            {comments.map((comment) => (
              <View
                key={comment.id}
                style={[
                  styles.messageBubble,
                  comment.authorId === ticket.customerId ? styles.customerBubble : styles.agentBubble
                ]}
              >
                <Text style={[styles.messageText, comment.authorId === ticket.customerId && styles.customerMessageText]}>
                  {comment.body}
                </Text>
                <Text style={styles.messageTime}>{formatTime(comment.createdAt)}</Text>
              </View>
            ))}
          </ScrollView>

          <View style={styles.composer}>
            <UnderlineInput
              label="Mesaj"
              onChangeText={setMessage}
              placeholder="Mesaj yazin..."
              value={message}
            />
            <PrimaryButton disabled={sending} label={sending ? "..." : "Gonder"} onPress={sendMessage} />
          </View>

          <TicketInfoSheet
            onClose={() => setSheetOpen(false)}
            open={sheetOpen}
            ticket={ticket}
          />
        </>
      ) : undefined}
    </ScreenContainer>
  );
}

function TicketInfoSheet({
  onClose,
  open,
  ticket
}: {
  onClose: () => void;
  open: boolean;
  ticket: TicketResponse;
}) {
  return (
    <Modal animationType="slide" onRequestClose={onClose} transparent visible={open}>
      <Pressable style={styles.sheetBackdrop} onPress={onClose} />
      <View style={styles.sheet}>
        <View style={styles.sheetHandle} />
        <Text style={styles.sheetTitle}>{ticket.productName}</Text>
        <View style={primitiveStyles.divider} />
        <InfoRow label="Durum" value={statusLabel(ticket.status)} />
        <InfoRow label="Oncelik" value={priorityLabel(ticket.priority)} />
        <InfoRow label="Olusturulma" value={formatDate(ticket.createdAt)} />
        <InfoRow label="Ek dosya" value={`${ticket.attachments.length}`} />
        <StatusPill label={ticket.priority === "HIGH" ? "SLA riskli" : "SLA uyumlu"} tone={ticket.priority === "HIGH" ? "danger" : "success"} />
        <SecondaryButton label="Kapat" onPress={onClose} />
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
    alignSelf: "flex-start",
    backgroundColor: colors.surfaceMuted
  },
  composer: {
    backgroundColor: colors.surface,
    borderTopColor: colors.border,
    borderTopWidth: 1,
    gap: spacing.sm,
    padding: spacing.md
  },
  customerBubble: {
    alignSelf: "flex-end",
    backgroundColor: colors.primary
  },
  customerMessageText: {
    color: colors.surface
  },
  infoButton: {
    color: colors.textMuted,
    fontSize: 24,
    fontWeight: "600"
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
  sheetTitle: {
    ...typography.heading,
    color: colors.text
  },
  ticketNumber: {
    ...typography.label,
    color: colors.textMuted,
    textAlign: "center"
  }
});
