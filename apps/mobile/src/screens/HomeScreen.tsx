import { useState } from "react";
import { Pressable, ScrollView, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { fetchGatewayHealth } from "../api/gatewayHealth";
import { useAuth } from "../auth/AuthProvider";
import { colors, radius, spacing, typography } from "../theme/tokens";

type HealthState = "idle" | "loading" | "healthy" | "error";

export function HomeScreen() {
  const insets = useSafeAreaInsets();
  const { signOut } = useAuth();
  const [healthState, setHealthState] = useState<HealthState>("idle");
  const [healthMessage, setHealthMessage] = useState("Gateway kontrol edilmedi.");

  async function checkGateway() {
    setHealthState("loading");
    setHealthMessage("Gateway kontrol ediliyor...");

    try {
      const health = await fetchGatewayHealth();
      setHealthState("healthy");
      setHealthMessage(`Gateway durumu: ${health.status}`);
    } catch {
      setHealthState("error");
      setHealthMessage("Gateway'e erisilemedi. API base URL ve token ayarlarini kontrol edin.");
    }
  }

  return (
    <ScrollView
      contentContainerStyle={[
        styles.container,
        { paddingTop: insets.top + spacing.md, paddingBottom: insets.bottom + spacing.lg }
      ]}
    >
      <View style={styles.topBar}>
        <Text style={styles.brand}>Support System</Text>
        <Pressable accessibilityRole="button" onPress={() => void signOut()}>
          <Text style={styles.linkButton}>Cikis</Text>
        </Pressable>
      </View>

      <View style={styles.hero}>
        <Text style={styles.kicker}>#54 Scaffold</Text>
        <Text style={styles.title}>Mobil uygulama temeli hazir</Text>
        <Text style={styles.description}>
          #55 kapsaminda MobilTasarim referanslarindaki Yeni Talep, Taleplerim,
          Mesajlar, Agent Kuyrugu ve Ozet ekranlari bu temelin uzerine tasinacak.
        </Text>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Gateway baglantisi</Text>
        <Text style={styles.cardText}>{healthMessage}</Text>
        <Pressable
          accessibilityRole="button"
          disabled={healthState === "loading"}
          onPress={() => void checkGateway()}
          style={({ pressed }) => [
            styles.secondaryButton,
            (pressed || healthState === "loading") && styles.secondaryButtonPressed
          ]}
        >
          <Text style={styles.secondaryButtonText}>
            {healthState === "loading" ? "Kontrol ediliyor" : "Gateway'i kontrol et"}
          </Text>
        </Pressable>
      </View>

      <View style={styles.card}>
        <Text style={styles.cardTitle}>Mobil tasarim kurallari</Text>
        <View style={styles.ruleRow}>
          <Text style={styles.ruleLabel}>Yuzey</Text>
          <Text style={styles.ruleValue}>Beyaz, 1px border, golge yok</Text>
        </View>
        <View style={styles.ruleRow}>
          <Text style={styles.ruleLabel}>Aksiyon</Text>
          <Text style={styles.ruleValue}>Kirmizi sadece kritik vurgu</Text>
        </View>
        <View style={styles.ruleRow}>
          <Text style={styles.ruleLabel}>Navigasyon</Text>
          <Text style={styles.ruleValue}>Role gore tab ve stack</Text>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  brand: {
    ...typography.heading,
    color: colors.primary
  },
  card: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: radius.md,
    borderWidth: 1,
    gap: spacing.md,
    padding: spacing.lg
  },
  cardText: {
    ...typography.body,
    color: colors.textMuted
  },
  cardTitle: {
    ...typography.heading,
    color: colors.text
  },
  container: {
    backgroundColor: colors.background,
    gap: spacing.lg,
    minHeight: "100%",
    paddingHorizontal: spacing.lg
  },
  description: {
    ...typography.body,
    color: colors.textMuted
  },
  hero: {
    gap: spacing.sm,
    paddingVertical: spacing.md
  },
  kicker: {
    ...typography.label,
    color: colors.primary,
    textTransform: "uppercase"
  },
  linkButton: {
    ...typography.label,
    color: colors.primary
  },
  ruleLabel: {
    ...typography.label,
    color: colors.textMuted,
    minWidth: 90
  },
  ruleRow: {
    borderTopColor: colors.border,
    borderTopWidth: 1,
    flexDirection: "row",
    gap: spacing.md,
    paddingTop: spacing.md
  },
  ruleValue: {
    ...typography.label,
    color: colors.text,
    flex: 1
  },
  secondaryButton: {
    alignItems: "center",
    borderColor: colors.border,
    borderRadius: radius.md,
    borderWidth: 1,
    minHeight: 48,
    justifyContent: "center"
  },
  secondaryButtonPressed: {
    backgroundColor: colors.surfaceMuted
  },
  secondaryButtonText: {
    ...typography.label,
    color: colors.text
  },
  title: {
    ...typography.title,
    color: colors.text
  },
  topBar: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between"
  }
});
