import type { ReactNode } from "react";
import { Pressable, StyleSheet, Text, TextInput, View, type TextInputProps, type ViewStyle } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import { colors, radius, spacing, typography } from "../theme/tokens";

interface HeaderProps {
  onBack?: () => void;
  right?: ReactNode;
  title: string;
}

interface TabItem<T extends string> {
  icon: string;
  key: T;
  label: string;
}

export function HeaderBar({ onBack, right, title }: HeaderProps) {
  return (
    <View style={styles.header}>
      <Pressable accessibilityRole={onBack ? "button" : undefined} disabled={!onBack} onPress={onBack}>
        <Text style={styles.headerIcon}>{onBack ? "<" : ""}</Text>
      </Pressable>
      <Text numberOfLines={1} style={styles.headerTitle}>{title}</Text>
      <View style={styles.headerRight}>{right ?? <Text style={styles.headerIcon}>...</Text>}</View>
    </View>
  );
}

export function ScreenContainer({ children, style }: { children: ReactNode; style?: ViewStyle }) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.screen, { paddingTop: insets.top }, style]}>
      {children}
    </View>
  );
}

export function ContentBlock({ children, style }: { children: ReactNode; style?: ViewStyle }) {
  return <View style={[styles.contentBlock, style]}>{children}</View>;
}

export function PrimaryButton({
  disabled,
  label,
  onPress
}: {
  disabled?: boolean;
  label: string;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      disabled={disabled}
      onPress={onPress}
      style={({ pressed }) => [
        styles.primaryButton,
        (pressed || disabled) && styles.primaryButtonPressed
      ]}
    >
      <Text style={styles.primaryButtonText}>{label}</Text>
    </Pressable>
  );
}

export function SecondaryButton({
  destructive,
  disabled,
  label,
  onPress
}: {
  destructive?: boolean;
  disabled?: boolean;
  label: string;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      disabled={disabled}
      onPress={onPress}
      style={({ pressed }) => [
        styles.secondaryButton,
        (pressed || disabled) && styles.secondaryButtonPressed
      ]}
    >
      <Text style={[styles.secondaryButtonText, destructive && styles.destructiveText]}>{label}</Text>
    </Pressable>
  );
}

export function UnderlineInput({ label, multiline, style, ...props }: TextInputProps & { label: string }) {
  return (
    <View style={styles.inputWrap}>
      <Text style={styles.inputLabel}>{label}</Text>
      <TextInput
        multiline={multiline}
        placeholderTextColor={colors.textMuted}
        style={[styles.input, multiline && styles.multilineInput, style]}
        {...props}
      />
    </View>
  );
}

export function Chip({
  active,
  danger,
  label,
  onPress
}: {
  active?: boolean;
  danger?: boolean;
  label: string;
  onPress?: () => void;
}) {
  return (
    <Pressable
      accessibilityRole={onPress ? "button" : undefined}
      disabled={!onPress}
      onPress={onPress}
      style={[styles.chip, active && styles.chipActive, danger && styles.chipDanger]}
    >
      <Text style={[styles.chipText, active && styles.chipTextActive, danger && styles.chipTextDanger]}>
        {label}
      </Text>
    </Pressable>
  );
}

export function StatusPill({ label, tone = "neutral" }: { label: string; tone?: "neutral" | "danger" | "success" }) {
  return (
    <View style={[styles.statusPill, tone === "danger" && styles.statusDanger, tone === "success" && styles.statusSuccess]}>
      <Text style={[styles.statusPillText, tone === "danger" && styles.statusDangerText]}>{label}</Text>
    </View>
  );
}

export function EmptyState({ message }: { message: string }) {
  return (
    <View style={styles.stateBox}>
      <Text style={styles.stateText}>{message}</Text>
    </View>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <View style={styles.stateBox}>
      <Text style={styles.errorText}>{message}</Text>
      {onRetry ? <SecondaryButton label="Tekrar dene" onPress={onRetry} /> : undefined}
    </View>
  );
}

export function BottomTabBar<T extends string>({
  activeKey,
  items,
  onChange
}: {
  activeKey: T;
  items: TabItem<T>[];
  onChange: (key: T) => void;
}) {
  const insets = useSafeAreaInsets();

  return (
    <View style={[styles.tabBar, { paddingBottom: Math.max(insets.bottom, spacing.sm) }]}>
      {items.map((item) => {
        const active = item.key === activeKey;

        return (
          <Pressable
            accessibilityRole="button"
            key={item.key}
            onPress={() => onChange(item.key)}
            style={styles.tabButton}
          >
            <Text style={[styles.tabIcon, active && styles.tabActive]}>{item.icon}</Text>
            <Text style={[styles.tabLabel, active && styles.tabActive]}>{item.label}</Text>
          </Pressable>
        );
      })}
    </View>
  );
}

export const primitiveStyles = StyleSheet.create({
  divider: {
    backgroundColor: colors.border,
    height: 1
  },
  rowBetween: {
    alignItems: "center",
    flexDirection: "row",
    justifyContent: "space-between"
  },
  sectionTitle: {
    ...typography.heading,
    color: colors.text
  },
  smallMuted: {
    ...typography.label,
    color: colors.textMuted
  }
});

const styles = StyleSheet.create({
  chip: {
    borderColor: colors.border,
    borderRadius: radius.full,
    borderWidth: 1,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm
  },
  chipActive: {
    borderColor: colors.text
  },
  chipDanger: {
    backgroundColor: "#ffdad6",
    borderColor: "#ffdad6"
  },
  chipText: {
    ...typography.label,
    color: colors.textMuted
  },
  chipTextActive: {
    color: colors.text
  },
  chipTextDanger: {
    color: colors.primary
  },
  contentBlock: {
    gap: spacing.md,
    padding: spacing.md
  },
  destructiveText: {
    color: colors.primary
  },
  errorText: {
    ...typography.body,
    color: colors.error,
    textAlign: "center"
  },
  header: {
    alignItems: "center",
    backgroundColor: colors.background,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    minHeight: 56,
    paddingHorizontal: spacing.md
  },
  headerIcon: {
    color: colors.text,
    fontSize: 24,
    width: 44
  },
  headerRight: {
    alignItems: "flex-end",
    minWidth: 44
  },
  headerTitle: {
    ...typography.heading,
    color: colors.text,
    flex: 1,
    textAlign: "center"
  },
  input: {
    ...typography.body,
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    color: colors.text,
    minHeight: 44,
    paddingHorizontal: 0,
    paddingVertical: spacing.sm
  },
  inputLabel: {
    ...typography.label,
    color: colors.textMuted,
    textTransform: "uppercase"
  },
  inputWrap: {
    gap: spacing.xs
  },
  multilineInput: {
    minHeight: 120,
    textAlignVertical: "top"
  },
  primaryButton: {
    alignItems: "center",
    backgroundColor: colors.primary,
    borderRadius: radius.md,
    justifyContent: "center",
    minHeight: 52
  },
  primaryButtonPressed: {
    opacity: 0.82
  },
  primaryButtonText: {
    ...typography.label,
    color: colors.surface
  },
  screen: {
    backgroundColor: colors.background,
    flex: 1
  },
  secondaryButton: {
    alignItems: "center",
    borderColor: colors.border,
    borderRadius: radius.md,
    borderWidth: 1,
    justifyContent: "center",
    minHeight: 48,
    paddingHorizontal: spacing.md
  },
  secondaryButtonPressed: {
    backgroundColor: colors.surfaceMuted
  },
  secondaryButtonText: {
    ...typography.label,
    color: colors.text
  },
  stateBox: {
    alignItems: "center",
    gap: spacing.md,
    padding: spacing.lg
  },
  stateText: {
    ...typography.body,
    color: colors.textMuted,
    textAlign: "center"
  },
  statusDanger: {
    backgroundColor: "#ffdad6"
  },
  statusDangerText: {
    color: colors.primary
  },
  statusPill: {
    backgroundColor: colors.surfaceMuted,
    borderRadius: radius.md,
    paddingHorizontal: spacing.sm,
    paddingVertical: spacing.xs
  },
  statusPillText: {
    ...typography.label,
    color: colors.text
  },
  statusSuccess: {
    backgroundColor: "#dff5e7"
  },
  tabActive: {
    color: colors.text,
    fontWeight: "600"
  },
  tabBar: {
    backgroundColor: colors.surface,
    borderTopColor: colors.border,
    borderTopWidth: 1,
    flexDirection: "row",
    paddingTop: spacing.sm
  },
  tabButton: {
    alignItems: "center",
    flex: 1,
    gap: spacing.xs
  },
  tabIcon: {
    color: colors.textMuted,
    fontSize: 22
  },
  tabLabel: {
    ...typography.label,
    color: colors.textMuted,
    fontSize: 11
  }
});
