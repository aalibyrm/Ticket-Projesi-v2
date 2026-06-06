import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import AssessmentOutlinedIcon from "@mui/icons-material/AssessmentOutlined";
import ConfirmationNumberOutlinedIcon from "@mui/icons-material/ConfirmationNumberOutlined";
import DashboardCustomizeOutlinedIcon from "@mui/icons-material/DashboardCustomizeOutlined";
import HelpOutlineOutlinedIcon from "@mui/icons-material/HelpOutlineOutlined";
import LogoutOutlinedIcon from "@mui/icons-material/LogoutOutlined";
import NotificationsNoneOutlinedIcon from "@mui/icons-material/NotificationsNoneOutlined";
import {
  Box,
  IconButton,
  Stack,
  Tooltip,
  Typography,
} from "@mui/material";
import type { ReactNode } from "react";
import { NavLink } from "react-router";
import type { AppRole } from "~/features/auth/authTypes";
import { logout } from "~/features/auth/authService";
import {
  selectAuthUser,
  selectUserRoles,
} from "~/features/auth/authSlice";
import { useAppSelector } from "~/shared/store/hooks";
import { tmTokens } from "~/shared/theme/tmTokens";

interface NavigationItem {
  icon: ReactNode;
  label: string;
  path: string;
  roles: AppRole[];
}

const navigationItems: NavigationItem[] = [
  {
    icon: <ConfirmationNumberOutlinedIcon />,
    label: "Taleplerim",
    path: "/tickets",
    roles: ["CUSTOMER", "ADMIN"],
  },
  {
    icon: <AddCircleOutlineIcon />,
    label: "Yeni talep",
    path: "/tickets/new",
    roles: ["CUSTOMER", "ADMIN"],
  },
  {
    icon: <DashboardCustomizeOutlinedIcon />,
    label: "Temsilci paneli",
    path: "/agent/inbox",
    roles: ["AGENT", "ADMIN"],
  },
  {
    icon: <AssessmentOutlinedIcon />,
    label: "Raporlar",
    path: "/reports",
    roles: ["MANAGER", "ADMIN"],
  },
  {
    icon: <NotificationsNoneOutlinedIcon />,
    label: "Bildirimler",
    path: "/notifications",
    roles: ["CUSTOMER", "AGENT", "MANAGER", "ADMIN"],
  },
];

export function RoleAwareShell({ children }: { children: ReactNode }) {
  const user = useAppSelector(selectAuthUser);
  const roles = useAppSelector(selectUserRoles);
  const visibleItems = navigationItems.filter((item) =>
    item.roles.some((role) => roles.includes(role)),
  );

  return (
    <Box sx={{ backgroundColor: tmTokens.colors.background, minHeight: "100vh" }}>
      <Box
        component="aside"
        sx={{
          alignItems: "center",
          backgroundColor: tmTokens.colors.surfaceLowest,
          borderRight: `1px solid ${tmTokens.colors.border}`,
          display: "flex",
          flexDirection: "column",
          height: "100vh",
          left: 0,
          position: "fixed",
          py: 1,
          top: 0,
          width: tmTokens.layout.sidebarWidth,
          zIndex: 10,
        }}
      >
        <Box
          aria-label="Ticket Management"
          sx={{
            alignItems: "center",
            color: tmTokens.colors.primaryContainer,
            display: "flex",
            height: 56,
            justifyContent: "center",
            mb: 3,
            mt: 1,
            ...tmTokens.typography.headlineSm,
          }}
        >
          SH
        </Box>
        <Stack alignItems="center" component="nav" spacing={2} sx={{ width: "100%" }}>
          {visibleItems.map((item) => (
            <Tooltip key={item.path} placement="right" title={item.label}>
              <IconButton
                aria-label={item.label}
                component={NavLink}
                to={item.path}
                sx={{
                  borderRadius: 0,
                  color: tmTokens.colors.secondary,
                  height: 40,
                  position: "relative",
                  width: "100%",
                  "&.active": {
                    backgroundColor: "transparent",
                    color: tmTokens.colors.primaryContainer,
                    "&::before": {
                      backgroundColor: tmTokens.colors.primaryContainer,
                      borderRadius: "0 999px 999px 0",
                      bottom: 8,
                      content: "\"\"",
                      left: 0,
                      position: "absolute",
                      top: 8,
                      width: 4,
                    },
                  },
                  "& svg": {
                    fontSize: 23,
                  },
                }}
              >
                {item.icon}
              </IconButton>
            </Tooltip>
          ))}
        </Stack>
      </Box>
      <Box sx={{ pl: `${tmTokens.layout.sidebarWidth}px` }}>
        <Box
          component="header"
          sx={{
            alignItems: "center",
            backgroundColor: tmTokens.colors.surfaceLowest,
            borderBottom: `1px solid ${tmTokens.colors.border}`,
            display: "flex",
            height: tmTokens.layout.topbarHeight,
            justifyContent: "space-between",
            position: "fixed",
            px: `${tmTokens.layout.pageMargin}px`,
            right: 0,
            top: 0,
            width: `calc(100% - ${tmTokens.layout.sidebarWidth}px)`,
            zIndex: 9,
          }}
        >
          <Typography
            sx={{
              color: tmTokens.colors.primaryContainer,
              ...tmTokens.typography.headlineMd,
            }}
          >
            SupportHub
          </Typography>
          <Stack alignItems="center" direction="row" spacing={1.5}>
            <Tooltip title="Bildirimler">
              <IconButton aria-label="Bildirimler">
                <NotificationsNoneOutlinedIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title="Yardim">
              <IconButton aria-label="Yardim">
                <HelpOutlineOutlinedIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title="Cikis">
              <IconButton aria-label="Cikis" onClick={() => void logout()}>
                <LogoutOutlinedIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title={`${user?.displayName ?? "Kullanici"} - ${roles.join(", ")}`}>
              <Box
                aria-label={`Kullanici: ${user?.displayName ?? "Bilinmeyen"}`}
                sx={{
                  alignItems: "center",
                  backgroundColor: tmTokens.colors.surfaceHigh,
                  borderRadius: "50%",
                  color: tmTokens.colors.onSurface,
                  display: "flex",
                  height: 32,
                  justifyContent: "center",
                  ml: 1,
                  width: 32,
                  ...tmTokens.typography.labelSm,
                }}
              >
                {user?.displayName?.slice(0, 1).toUpperCase() ?? "U"}
              </Box>
            </Tooltip>
          </Stack>
        </Box>
        <Box
          component="main"
          sx={{
            maxWidth: tmTokens.layout.maxContentWidth,
            mx: "auto",
            px: `${tmTokens.layout.pageMargin}px`,
            py: `${tmTokens.layout.pageMargin}px`,
            pt: `calc(${tmTokens.layout.topbarHeight}px + ${tmTokens.layout.pageMargin}px)`,
            width: "100%",
          }}
        >
          {children}
        </Box>
      </Box>
    </Box>
  );
}
