import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { AuthStatus, AuthUser } from "~/features/auth/authTypes";
import type { RootState } from "~/shared/store/store";

export interface AuthState {
  error?: string;
  status: AuthStatus;
  user?: AuthUser;
}

const initialState: AuthState = {
  status: "idle",
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setAuthenticated(state, action: PayloadAction<AuthUser>) {
      state.error = undefined;
      state.status = "authenticated";
      state.user = action.payload;
    },
    setAuthError(state, action: PayloadAction<string>) {
      state.error = action.payload;
      state.status = "error";
      state.user = undefined;
    },
    setAuthLoading(state) {
      state.error = undefined;
      state.status = "loading";
    },
    setUnauthenticated(state) {
      state.error = undefined;
      state.status = "unauthenticated";
      state.user = undefined;
    },
  },
});

export const {
  setAuthenticated,
  setAuthError,
  setAuthLoading,
  setUnauthenticated,
} = authSlice.actions;

export const selectAuthStatus = (state: RootState) => state.auth.status;
export const selectAuthUser = (state: RootState) => state.auth.user;
export const selectAuthError = (state: RootState) => state.auth.error;
export const selectUserRoles = (state: RootState) => state.auth.user?.roles ?? [];

export default authSlice.reducer;
