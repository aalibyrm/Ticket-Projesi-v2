import type { AuthUser } from "~/features/auth/authTypes";

interface KnownUserProfile {
  displayName: string;
  email: string;
}

const knownUserProfiles: Record<string, KnownUserProfile> = {
  "80000000-0000-0000-0000-000000000001": {
    displayName: "Demo Customer",
    email: "customer.user@example.local",
  },
  "30000000-0000-0000-0000-000000000001": {
    displayName: "Identity Lead",
    email: "lead.identity@example.local",
  },
  "30000000-0000-0000-0000-000000000002": {
    displayName: "Permission Lead",
    email: "lead.permission@example.local",
  },
  "30000000-0000-0000-0000-000000000003": {
    displayName: "Web Lead",
    email: "lead.web@example.local",
  },
  "30000000-0000-0000-0000-000000000004": {
    displayName: "Core Lead",
    email: "lead.core@example.local",
  },
  "30000000-0000-0000-0000-000000000005": {
    displayName: "Network Lead",
    email: "lead.network@example.local",
  },
  "30000000-0000-0000-0000-000000000006": {
    displayName: "Platform Lead",
    email: "lead.platform@example.local",
  },
  "30000000-0000-0000-0000-000000000007": {
    displayName: "Billing Lead",
    email: "lead.billing@example.local",
  },
  "30000000-0000-0000-0000-000000000008": {
    displayName: "Payment Lead",
    email: "lead.payment@example.local",
  },
  "40000000-0000-0000-0000-000000000001": {
    displayName: "Identity Agent",
    email: "agent.identity@example.local",
  },
  "40000000-0000-0000-0000-000000000002": {
    displayName: "Permission Agent",
    email: "agent.permission@example.local",
  },
  "40000000-0000-0000-0000-000000000003": {
    displayName: "Web Agent",
    email: "agent.web@example.local",
  },
  "40000000-0000-0000-0000-000000000004": {
    displayName: "Core Agent",
    email: "agent.core@example.local",
  },
  "40000000-0000-0000-0000-000000000005": {
    displayName: "Network Agent",
    email: "agent.network@example.local",
  },
  "40000000-0000-0000-0000-000000000006": {
    displayName: "Platform Agent",
    email: "agent.platform@example.local",
  },
  "40000000-0000-0000-0000-000000000007": {
    displayName: "Billing Agent",
    email: "agent.billing@example.local",
  },
  "40000000-0000-0000-0000-000000000008": {
    displayName: "Payment Agent",
    email: "agent.payment@example.local",
  },
  "80000000-0000-0000-0000-000000000002": {
    displayName: "Demo Manager",
    email: "manager.user@example.local",
  },
  "80000000-0000-0000-0000-000000000003": {
    displayName: "Demo Admin",
    email: "admin.user@example.local",
  },
};

export function actorDisplayName(actorId: string | null | undefined, currentUser?: AuthUser, fallbackLabel = "Kullanici") {
  if (!actorId) {
    return fallbackLabel;
  }
  if (currentUser?.id === actorId) {
    return currentUser.displayName;
  }
  return knownUserProfiles[actorId]?.displayName ?? `${fallbackLabel} ${shortActorId(actorId)}`;
}

export function shortActorId(actorId: string) {
  return actorId.slice(0, 8);
}
