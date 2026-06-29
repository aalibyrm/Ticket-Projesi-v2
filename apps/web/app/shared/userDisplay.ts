import type { AuthUser } from "~/features/auth/authTypes";

interface KnownUserProfile {
  displayName: string;
  email: string;
}

const knownUserProfiles: Record<string, KnownUserProfile> = {
  "80000000-0000-0000-0000-000000000001": {
    displayName: "Ayse Yilmaz",
    email: "ayse.yilmaz@example.local",
  },
  "80000000-0000-0000-0000-000000000004": {
    displayName: "Mehmet Demir",
    email: "mehmet.demir@example.local",
  },
  "80000000-0000-0000-0000-000000000005": {
    displayName: "Zeynep Kaya",
    email: "zeynep.kaya@example.local",
  },
  "80000000-0000-0000-0000-000000000006": {
    displayName: "Emre Arslan",
    email: "emre.arslan@example.local",
  },
  "80000000-0000-0000-0000-000000000007": {
    displayName: "Ceren Aksoy",
    email: "ceren.aksoy@example.local",
  },
  "30000000-0000-0000-0000-000000000001": {
    displayName: "Irem Gunes",
    email: "irem.gunes@example.local",
  },
  "30000000-0000-0000-0000-000000000002": {
    displayName: "Cem Arslan",
    email: "cem.arslan@example.local",
  },
  "30000000-0000-0000-0000-000000000003": {
    displayName: "Seda Yildirim",
    email: "seda.yildirim@example.local",
  },
  "30000000-0000-0000-0000-000000000004": {
    displayName: "Okan Demir",
    email: "okan.demir@example.local",
  },
  "30000000-0000-0000-0000-000000000005": {
    displayName: "Derya Korkmaz",
    email: "derya.korkmaz@example.local",
  },
  "30000000-0000-0000-0000-000000000006": {
    displayName: "Alp Kaya",
    email: "alp.kaya@example.local",
  },
  "30000000-0000-0000-0000-000000000007": {
    displayName: "Melis Acar",
    email: "melis.acar@example.local",
  },
  "30000000-0000-0000-0000-000000000008": {
    displayName: "Bora Yalcin",
    email: "bora.yalcin@example.local",
  },
  "40000000-0000-0000-0000-000000000001": {
    displayName: "Elif Aydin",
    email: "elif.aydin@example.local",
  },
  "40000000-0000-0000-0000-000000000002": {
    displayName: "Mert Kaya",
    email: "mert.kaya@example.local",
  },
  "40000000-0000-0000-0000-000000000003": {
    displayName: "Deniz Arslan",
    email: "deniz.arslan@example.local",
  },
  "40000000-0000-0000-0000-000000000004": {
    displayName: "Selin Demir",
    email: "selin.demir@example.local",
  },
  "40000000-0000-0000-0000-000000000005": {
    displayName: "Baran Yilmaz",
    email: "baran.yilmaz@example.local",
  },
  "40000000-0000-0000-0000-000000000006": {
    displayName: "Ece Sahin",
    email: "ece.sahin@example.local",
  },
  "40000000-0000-0000-0000-000000000007": {
    displayName: "Onur Demir",
    email: "onur.demir@example.local",
  },
  "40000000-0000-0000-0000-000000000008": {
    displayName: "Zeynep Ozturk",
    email: "zeynep.ozturk@example.local",
  },
  "80000000-0000-0000-0000-000000000002": {
    displayName: "Deniz Karaca",
    email: "deniz.karaca@example.local",
  },
  "80000000-0000-0000-0000-000000000003": {
    displayName: "Burak Ozkan",
    email: "burak.ozkan@example.local",
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
