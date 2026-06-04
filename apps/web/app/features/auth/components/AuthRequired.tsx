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
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleLogin() {
    setLoginError(undefined);
    setIsSubmitting(true);
    try {
      await login();
    } catch {
      setLoginError("Giris islemi baslatilamadi.");
      setIsSubmitting(false);
    }
  }

  return (
    <Box
      sx={{
        alignItems: "center",
        backgroundColor: "#f0f0f5",
        display: "flex",
        justifyContent: "center",
        minHeight: "100vh",
        px: { xs: 3, sm: 4 },
        py: 4,
      }}
    >
      <Paper
        sx={{
          border: "1px solid",
          borderColor: "#e5e5ea",
          borderRadius: 2,
          boxShadow: "0 4px 24px rgba(0, 0, 0, 0.06)",
          maxWidth: 440,
          p: { xs: 3, sm: 5 },
          width: "100%",
        }}
      >
        <Stack spacing={4}>
          <Stack alignItems="center" spacing={0.5}>
            <Typography
              sx={{
                alignItems: "baseline",
                display: "inline-flex",
                fontFamily: "\"Outfit\", Arial, sans-serif",
                fontSize: 24,
                fontWeight: 600,
                lineHeight: "32px",
              }}
            >
              Ticket
              <Box
                component="span"
                sx={{
                  bgcolor: "primary.light",
                  borderRadius: "50%",
                  display: "inline-block",
                  height: 8,
                  ml: 0.5,
                  width: 8,
                }}
              />
            </Typography>
            <Typography color="text.secondary">Destek Portali</Typography>
          </Stack>

          <Stack spacing={0.5}>
            <Typography
              component="h1"
              sx={{
                fontFamily: "\"Outfit\", Arial, sans-serif",
                fontSize: 24,
                fontWeight: 600,
                lineHeight: "32px",
              }}
            >
              Hos geldiniz
            </Typography>
            <Typography color="text.secondary">
              Devam etmek icin giris yapin
            </Typography>
          </Stack>

          {(error || loginError) && (
            <Typography color="error" role="alert" variant="body2">
              {loginError ?? error}
            </Typography>
          )}

          <Button
            disabled={isSubmitting}
            onClick={handleLogin}
            size="large"
            startIcon={<LoginOutlinedIcon />}
            sx={{
              bgcolor: "primary.light",
              minHeight: 48,
              "&:hover": {
                bgcolor: "primary.main",
              },
            }}
            variant="contained"
          >
            {isSubmitting ? "Yonlendiriliyor" : "Giris Yap"}
          </Button>

          <Box
            sx={{
              alignItems: "center",
              color: "text.secondary",
              display: "flex",
              gap: 2.5,
            }}
          >
            <Box sx={{ bgcolor: "divider", flex: 1, height: 1 }} />
            <Typography variant="body2">veya</Typography>
            <Box sx={{ bgcolor: "divider", flex: 1, height: 1 }} />
          </Box>

          <Typography color="text.secondary" textAlign="center">
            Hesabiniz yok mu?{" "}
            <Box component="span" sx={{ color: "primary.light", fontWeight: 600 }}>
              Kurum yoneticinizle gorusun
            </Box>
          </Typography>
        </Stack>
      </Paper>
    </Box>
  );
}
