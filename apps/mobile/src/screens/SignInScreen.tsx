import { Pressable, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAuth } from "../auth/AuthProvider";
import { colors, radius, spacing, typography } from "../theme/tokens";

export function SignInScreen() {
  const { error, signIn, status } = useAuth();
  const insets = useSafeAreaInsets();
  const isBusy = status === "loading";

  return (
    <View style={[styles.container, { paddingTop: insets.top + spacing.lg }]}>
      <View style={styles.header}>
        <Text style={styles.brand}>Support System</Text>
        <Text style={styles.title}>Mobil destek deneyimi</Text>
        <Text style={styles.description}>
          Ticket listeleri, mesajlar ve dosya akislarini gateway uzerinden guvenli
          oturumla kullan.
        </Text>
      </View>

      <View style={styles.panel}>
        <Text style={styles.panelTitle}>Kurumsal oturum</Text>
        <Text style={styles.panelText}>
          Keycloak OIDC ve PKCE ile oturum acilir. Token mobil cihazda SecureStore
          icinde saklanir.
        </Text>

        {error ? <Text style={styles.errorText}>{error}</Text> : undefined}

        <Pressable
          accessibilityRole="button"
          disabled={isBusy}
          onPress={() => void signIn()}
          style={({ pressed }) => [
            styles.primaryButton,
            (pressed || isBusy) && styles.primaryButtonPressed
          ]}
        >
          <Text style={styles.primaryButtonText}>{isBusy ? "Bekleyin" : "Oturum ac"}</Text>
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  brand: {
    ...typography.heading,
    color: colors.primary
  },
  container: {
    backgroundColor: colors.background,
    flex: 1,
    justifyContent: "space-between",
    padding: spacing.lg
  },
  description: {
    ...typography.body,
    color: colors.textMuted
  },
  errorText: {
    ...typography.label,
    color: colors.error
  },
  header: {
    gap: spacing.md
  },
  panel: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: radius.md,
    borderWidth: 1,
    gap: spacing.md,
    padding: spacing.lg
  },
  panelText: {
    ...typography.body,
    color: colors.textMuted
  },
  panelTitle: {
    ...typography.heading,
    color: colors.text
  },
  primaryButton: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: radius.md,
    minHeight: 52,
    justifyContent: "center"
  },
  primaryButtonPressed: {
    opacity: 0.86
  },
  primaryButtonText: {
    ...typography.label,
    color: colors.surface
  },
  title: {
    ...typography.title,
    color: colors.text
  }
});
