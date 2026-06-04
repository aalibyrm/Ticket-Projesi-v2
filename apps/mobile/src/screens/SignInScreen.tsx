import { Pressable, StyleSheet, Text, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { useAuth } from "../auth/AuthProvider";
import { colors, radius, spacing, typography } from "../theme/tokens";

export function SignInScreen() {
  const { error, signIn, status } = useAuth();
  const insets = useSafeAreaInsets();
  const isBusy = status === "loading";

  return (
    <View
      style={[
        styles.container,
        {
          paddingBottom: insets.bottom + spacing.lg,
          paddingTop: insets.top + spacing.lg
        }
      ]}
    >
      <View style={styles.panel}>
        <View style={styles.brandBlock}>
          <Text style={styles.brand}>
            Ticket<Text style={styles.brandDot}>.</Text>
          </Text>
          <Text style={styles.brandSubtitle}>Destek Portali</Text>
        </View>

        <View style={styles.headingBlock}>
          <Text style={styles.title}>Hos geldiniz</Text>
          <Text style={styles.description}>Devam etmek icin giris yapin</Text>
        </View>

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
          <Text style={styles.primaryButtonText}>{isBusy ? "Yonlendiriliyor" : "Giris Yap"}</Text>
        </Pressable>

        <View style={styles.divider}>
          <View style={styles.dividerLine} />
          <Text style={styles.dividerText}>veya</Text>
          <View style={styles.dividerLine} />
        </View>

        <Text style={styles.registerText}>
          Hesabiniz yok mu? <Text style={styles.registerLink}>Kurum yoneticinizle gorusun</Text>
        </Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  brand: {
    ...typography.heading,
    color: colors.text,
    lineHeight: 28
  },
  brandBlock: {
    alignItems: "center",
    gap: spacing.xs
  },
  brandDot: {
    color: colors.primary
  },
  brandSubtitle: {
    ...typography.label,
    color: colors.textMuted
  },
  container: {
    alignItems: "center",
    backgroundColor: "#f0f0f5",
    flex: 1,
    justifyContent: "center",
    paddingHorizontal: 31
  },
  description: {
    ...typography.body,
    color: colors.textMuted
  },
  divider: {
    alignItems: "center",
    flexDirection: "row",
    gap: 14,
    marginTop: spacing.sm
  },
  dividerLine: {
    backgroundColor: colors.border,
    flex: 1,
    height: 1
  },
  dividerText: {
    ...typography.label,
    color: colors.textMuted,
    fontSize: 11
  },
  errorText: {
    ...typography.label,
    color: colors.error
  },
  headingBlock: {
    gap: spacing.xs
  },
  panel: {
    backgroundColor: colors.surface,
    borderColor: colors.border,
    borderRadius: radius.md,
    borderWidth: 1,
    gap: spacing.lg,
    maxWidth: 342,
    padding: spacing.lg,
    width: "100%"
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
  registerLink: {
    color: colors.primary,
    fontWeight: "500"
  },
  registerText: {
    ...typography.label,
    color: colors.textMuted,
    textAlign: "center"
  },
  title: {
    ...typography.title,
    color: colors.text
  }
});
