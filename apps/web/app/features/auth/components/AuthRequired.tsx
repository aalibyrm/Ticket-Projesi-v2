import LoginOutlinedIcon from "@mui/icons-material/LoginOutlined";
import {
  Box,
  Button,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { login } from "~/features/auth/authService";

export function AuthRequired({ error }: { error?: string }) {
  const [loginError, setLoginError] = useState<string>();

  async function handleLogin() {
    setLoginError(undefined);
    try {
      await login();
    } catch {
      setLoginError("Keycloak girisi baslatilamadi.");
    }
  }

  return (
    <Box
      sx={{
        alignItems: "center",
        backgroundColor: "background.default",
        display: "flex",
        minHeight: "100vh",
        p: 4,
      }}
    >
      <Paper
        sx={{
          border: "1px solid",
          borderColor: "divider",
          boxShadow: "none",
          maxWidth: 440,
          p: 4,
          width: "100%",
        }}
      >
        <Stack spacing={3}>
          <Stack spacing={0.75}>
            <Typography variant="overline">Ticket Management</Typography>
            <Typography variant="h4">Giris gerekli</Typography>
            <Typography color="text.secondary">
              Web paneline devam etmek icin Keycloak ile oturum acmalisin.
            </Typography>
          </Stack>
          {(error || loginError) && (
            <Typography color="error" role="alert" variant="body2">
              {loginError ?? error}
            </Typography>
          )}
          <Button
            onClick={handleLogin}
            size="large"
            startIcon={<LoginOutlinedIcon />}
            variant="contained"
          >
            Keycloak ile giris yap
          </Button>
        </Stack>
      </Paper>
    </Box>
  );
}
