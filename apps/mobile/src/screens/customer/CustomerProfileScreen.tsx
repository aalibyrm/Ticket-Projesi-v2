import { useCallback, useEffect, useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import type { NotificationResponse } from "../../api/mobileApiTypes";
import { listNotifications, markNotificationRead } from "../../api/notificationApi";
import { useAuth } from "../../auth/AuthProvider";
import { EmptyState, ErrorState, SecondaryButton, StatusPill } from "../../components/MobilePrimitives";
import { colors, radius, spacing, typography } from "../../theme/tokens";
import { formatDate } from "../../utils/formatters";

export function CustomerProfileScreen() {
  const { signOut, user } = useAuth();
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();

  const loadNotifications = useCallback(async () => {
    setLoading(true);
    setError(undefined);

    try {
      setNotifications(await listNotifications());
    } catch {
      setError("Bildirimler yuklenemedi.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadNotifications();
  }, [loadNotifications]);

  async function markRead(notificationId: string) {
    const updated = await markNotificationRead(notificationId);
    setNotifications((current) =>
      current.map((notification) => notification.id === notificationId ? updated : notification)
    );
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <View style={styles.profileCard}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{(user?.displayName ?? "TM").slice(0, 2).toUpperCase()}</Text>
        </View>
        <View style={styles.profileBody}>
          <Text style={styles.profileName}>{user?.displayName ?? "Kullanici"}</Text>
          <Text style={styles.profileMeta}>{user?.roles.join(", ")}</Text>
        </View>
      </View>

      <Text style={styles.sectionTitle}>Bildirimler</Text>
      {loading ? <EmptyState message="Bildirimler yukleniyor." /> : undefined}
      {error ? <ErrorState message={error} onRetry={loadNotifications} /> : undefined}
      {!loading && !error && notifications.length === 0 ? <EmptyState message="Bildirim yok." /> : undefined}
      {notifications.map((notification) => (
        <Pressable
          accessibilityRole="button"
          key={notification.id}
          onPress={() => !notification.read && void markRead(notification.id)}
          style={styles.notificationRow}
        >
          <View style={styles.notificationBody}>
            <Text style={styles.notificationTitle}>{notification.title}</Text>
            <Text style={styles.notificationMessage}>{notification.message}</Text>
            <Text style={styles.notificationDate}>{formatDate(notification.createdAt)}</Text>
          </View>
          <StatusPill label={notification.read ? "Okundu" : "Yeni"} tone={notification.read ? "neutral" : "danger"} />
        </Pressable>
      ))}

      <SecondaryButton destructive label="Cikis" onPress={() => void signOut()} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  avatar: {
    alignItems: "center",
    backgroundColor: colors.surfaceMuted,
    borderRadius: radius.full,
    height: 56,
    justifyContent: "center",
    width: 56
  },
  avatarText: {
    ...typography.heading,
    color: colors.primary
  },
  container: {
    gap: spacing.lg,
    padding: spacing.md,
    paddingBottom: spacing.xl
  },
  notificationBody: {
    flex: 1,
    gap: spacing.xs
  },
  notificationDate: {
    ...typography.label,
    color: colors.textMuted
  },
  notificationMessage: {
    ...typography.body,
    color: colors.textMuted
  },
  notificationRow: {
    alignItems: "center",
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    paddingVertical: spacing.md
  },
  notificationTitle: {
    ...typography.body,
    color: colors.text,
    fontWeight: "600"
  },
  profileBody: {
    flex: 1,
    gap: spacing.xs
  },
  profileCard: {
    alignItems: "center",
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: radius.md,
    borderWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    padding: spacing.lg
  },
  profileMeta: {
    ...typography.body,
    color: colors.textMuted
  },
  profileName: {
    ...typography.heading,
    color: colors.text
  },
  sectionTitle: {
    ...typography.heading,
    color: colors.text
  }
});
