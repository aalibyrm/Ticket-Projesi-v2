import { useMemo, useState } from "react";
import { StyleSheet, Text, View } from "react-native";
import type { AppRole } from "../auth/authTypes";
import { useAuth } from "../auth/AuthProvider";
import { BottomTabBar, Chip, HeaderBar, ScreenContainer } from "../components/MobilePrimitives";
import { AgentTicketDetailScreen } from "./agent/AgentTicketDetailScreen";
import { AgentTicketsScreen } from "./agent/AgentTicketsScreen";
import { CustomerCreateTicketScreen } from "./customer/CustomerCreateTicketScreen";
import { CustomerMessagesScreen } from "./customer/CustomerMessagesScreen";
import { CustomerProfileScreen } from "./customer/CustomerProfileScreen";
import { CustomerTicketDetailScreen } from "./customer/CustomerTicketDetailScreen";
import { CustomerTicketsScreen } from "./customer/CustomerTicketsScreen";
import { ManagerSummaryScreen } from "./manager/ManagerSummaryScreen";
import { colors, spacing, typography } from "../theme/tokens";

type MobileMode = "customer" | "agent" | "manager";
type CustomerTab = "new" | "tickets" | "messages" | "profile";
type AgentTab = "queue" | "assigned" | "stats" | "profile";
type ManagerTab = "summary" | "tickets" | "notifications" | "profile";

const customerTabs = [
  { icon: "+", key: "new", label: "Yeni Talep" },
  { icon: "o", key: "tickets", label: "Taleplerim" },
  { icon: "[]", key: "messages", label: "Mesajlar" },
  { icon: "@", key: "profile", label: "Profil" }
] satisfies { icon: string; key: CustomerTab; label: string }[];

const agentTabs = [
  { icon: "+", key: "queue", label: "Kuyruk" },
  { icon: "[]", key: "assigned", label: "Atanan" },
  { icon: "||", key: "stats", label: "Istatistik" },
  { icon: "@", key: "profile", label: "Profil" }
] satisfies { icon: string; key: AgentTab; label: string }[];

const managerTabs = [
  { icon: "||", key: "summary", label: "Ozet" },
  { icon: "o", key: "tickets", label: "Talepler" },
  { icon: "!", key: "notifications", label: "Bildirim" },
  { icon: "@", key: "profile", label: "Profil" }
] satisfies { icon: string; key: ManagerTab; label: string }[];

export function HomeScreen() {
  const { user } = useAuth();
  const availableModes = useMemo(() => resolveAvailableModes(user?.roles ?? []), [user?.roles]);
  const [mode, setMode] = useState<MobileMode>(availableModes[0] ?? "customer");
  const [customerTab, setCustomerTab] = useState<CustomerTab>("tickets");
  const [agentTab, setAgentTab] = useState<AgentTab>("queue");
  const [managerTab, setManagerTab] = useState<ManagerTab>("summary");
  const [customerTicketId, setCustomerTicketId] = useState<string | undefined>();
  const [agentTicketId, setAgentTicketId] = useState<string | undefined>();

  const activeMode = availableModes.includes(mode) ? mode : availableModes[0] ?? "customer";

  if (customerTicketId) {
    return (
      <CustomerTicketDetailScreen
        onBack={() => setCustomerTicketId(undefined)}
        ticketId={customerTicketId}
      />
    );
  }

  if (agentTicketId) {
    return (
      <AgentTicketDetailScreen
        onBack={() => setAgentTicketId(undefined)}
        ticketId={agentTicketId}
      />
    );
  }

  return (
    <ScreenContainer>
      <HeaderBar
        right={<Text style={styles.roleText}>{user?.displayName ?? "Support"}</Text>}
        title={titleForMode(activeMode, customerTab, agentTab, managerTab)}
      />

      {availableModes.length > 1 ? (
        <View style={styles.modeRow}>
          {availableModes.map((item) => (
            <Chip
              active={activeMode === item}
              key={item}
              label={modeLabel(item)}
              onPress={() => setMode(item)}
            />
          ))}
        </View>
      ) : undefined}

      <View style={styles.content}>
        {activeMode === "customer" ? (
          <CustomerContent
            activeTab={customerTab}
            onCreated={(ticketId) => {
              setCustomerTab("tickets");
              setCustomerTicketId(ticketId);
            }}
            onOpenTicket={setCustomerTicketId}
          />
        ) : undefined}

        {activeMode === "agent" ? (
          <AgentContent
            activeTab={agentTab}
            onOpenTicket={setAgentTicketId}
          />
        ) : undefined}

        {activeMode === "manager" ? (
          <ManagerContent activeTab={managerTab} />
        ) : undefined}
      </View>

      {activeMode === "customer" ? (
        <BottomTabBar activeKey={customerTab} items={customerTabs} onChange={setCustomerTab} />
      ) : undefined}
      {activeMode === "agent" ? (
        <BottomTabBar activeKey={agentTab} items={agentTabs} onChange={setAgentTab} />
      ) : undefined}
      {activeMode === "manager" ? (
        <BottomTabBar activeKey={managerTab} items={managerTabs} onChange={setManagerTab} />
      ) : undefined}
    </ScreenContainer>
  );
}

function CustomerContent({
  activeTab,
  onCreated,
  onOpenTicket
}: {
  activeTab: CustomerTab;
  onCreated: (ticketId: string) => void;
  onOpenTicket: (ticketId: string) => void;
}) {
  if (activeTab === "new") {
    return <CustomerCreateTicketScreen onCreated={onCreated} />;
  }

  if (activeTab === "messages") {
    return <CustomerMessagesScreen onOpenTicket={onOpenTicket} />;
  }

  if (activeTab === "profile") {
    return <CustomerProfileScreen />;
  }

  return <CustomerTicketsScreen onOpenTicket={onOpenTicket} />;
}

function AgentContent({
  activeTab,
  onOpenTicket
}: {
  activeTab: AgentTab;
  onOpenTicket: (ticketId: string) => void;
}) {
  if (activeTab === "assigned") {
    return <AgentTicketsScreen mode="assigned" onOpenTicket={onOpenTicket} />;
  }

  if (activeTab === "stats") {
    return <ManagerSummaryScreen />;
  }

  if (activeTab === "profile") {
    return <CustomerProfileScreen />;
  }

  return <AgentTicketsScreen mode="queue" onOpenTicket={onOpenTicket} />;
}

function ManagerContent({ activeTab }: { activeTab: ManagerTab }) {
  if (activeTab === "tickets") {
    return <CustomerTicketsScreen onOpenTicket={() => undefined} />;
  }

  if (activeTab === "notifications" || activeTab === "profile") {
    return <CustomerProfileScreen />;
  }

  return <ManagerSummaryScreen />;
}

function resolveAvailableModes(roles: AppRole[]): MobileMode[] {
  const modes: MobileMode[] = [];

  if (roles.includes("CUSTOMER") || roles.includes("ADMIN")) {
    modes.push("customer");
  }

  if (roles.includes("AGENT") || roles.includes("ADMIN")) {
    modes.push("agent");
  }

  if (roles.includes("MANAGER") || roles.includes("ADMIN")) {
    modes.push("manager");
  }

  return modes.length > 0 ? modes : ["customer"];
}

function modeLabel(mode: MobileMode) {
  return {
    agent: "Agent",
    customer: "Customer",
    manager: "Manager"
  }[mode];
}

function titleForMode(
  mode: MobileMode,
  customerTab: CustomerTab,
  agentTab: AgentTab,
  managerTab: ManagerTab
) {
  if (mode === "agent") {
    return agentTab === "assigned" ? "Atanan Biletler" : agentTab === "stats" ? "Istatistik" : "Bilet Kuyrugu";
  }

  if (mode === "manager") {
    return managerTab === "summary" ? "Ozet" : "Yonetici";
  }

  return {
    messages: "Mesajlar",
    new: "Support System",
    profile: "Profil",
    tickets: "Taleplerim"
  }[customerTab];
}

const styles = StyleSheet.create({
  content: {
    flex: 1
  },
  modeRow: {
    borderBottomColor: colors.border,
    borderBottomWidth: 1,
    flexDirection: "row",
    gap: spacing.sm,
    padding: spacing.md
  },
  roleText: {
    ...typography.label,
    color: colors.textMuted,
    maxWidth: 104
  }
});
