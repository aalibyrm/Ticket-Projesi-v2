import * as SecureStore from "expo-secure-store";
import type { AuthTokens } from "./authTypes";

const TOKEN_STORAGE_KEY = "ticket.mobile.auth.tokens";

export async function loadStoredTokens() {
  const rawTokens = await SecureStore.getItemAsync(TOKEN_STORAGE_KEY);

  if (!rawTokens) {
    return undefined;
  }

  try {
    return JSON.parse(rawTokens) as AuthTokens;
  } catch {
    await clearStoredTokens();
    return undefined;
  }
}

export async function storeTokens(tokens: AuthTokens) {
  await SecureStore.setItemAsync(TOKEN_STORAGE_KEY, JSON.stringify(tokens));
}

export async function clearStoredTokens() {
  await SecureStore.deleteItemAsync(TOKEN_STORAGE_KEY);
}
