import AddCircleOutlineIcon from "@mui/icons-material/AddCircleOutline";
import AssessmentOutlinedIcon from "@mui/icons-material/AssessmentOutlined";
import ConfirmationNumberOutlinedIcon from "@mui/icons-material/ConfirmationNumberOutlined";
import DashboardCustomizeOutlinedIcon from "@mui/icons-material/DashboardCustomizeOutlined";
import LogoutOutlinedIcon from "@mui/icons-material/LogoutOutlined";
import {
  Box,
  Button,
  Divider,
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
    roles: ["CUSTOMER", "AGENT", "MANAGER", "ADMIN"],
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
];

export function RoleAwareShell({ children }: { children: ReactNode }) {
  const user = useAppSelector(selectAuthUser);
  const roles = useAppSelector(selectUserRoles);
  const visibleItems = navigationItems.filter((item) =>
    item.roles.some((role) => roles.includes(role)),
  );

  return (
    <Box sx={{ backgroundColor: "background.default", minHeight: "100vh" }}>
      <Box
        component="aside"
        sx={{
          alignItems: "center",
          backgroundColor: "background.paper",
          borderRight: "1px solid",
          borderColor: "divider",
          display: "flex",
          flexDirection: "column",
          gap: 2,
          height: "100vh",
          left: 0,
          position: "fixed",
          py: 2,
          top: 0,
          width: 72,
          zIndex: 10,
        }}
      >
        <Box
          aria-label="Ticket Management"
          sx={{
            alignItems: "center",
            border: "1px solid",
            borderColor: "divider",
            borderRadius: 2,
            color: "primary.main",
            display: "flex",
            fontFamily: "\"Outfit\", Arial, sans-serif",
            fontWeight: 600,
            height: 40,
            justifyContent: "center",
            width: 40,
          }}
        >
          TM
        </Box>
        <Divider flexItem />
        <Stack alignItems="center" component="nav" spacing={1}>
          {visibleItems.map((item) => (
            <Tooltip key={item.path} placement="right" title={item.label}>
              <IconButton
                aria-label={item.label}
                component={NavLink}
                to={item.path}
                sx={{
                  color: "text.secondary",
                  height: 44,
                  width: 44,
                  "&.active": {
                    backgroundColor: "primary.main",
                    color: "primary.contrastText",
                  },
                }}
              >
                {item.icon}
              </IconButton>
            </Tooltip>
          ))}
        </Stack>
      </Box>
      <Box sx={{ pl: "72px" }}>
        <Box
          component="header"
          sx={{
            alignItems: "center",
            backgroundColor: "background.default",
            borderBottom: "1px solid",
            borderColor: "divider",
            display: "flex",
            justifyContent: "space-between",
            minHeight: 72,
            px: 4,
          }}
        >
          <Stack spacing={0.25}>
            <Typography variant="overline">Operasyon paneli</Typography>
            <Typography variant="h6">{user?.displayName}</Typography>
          </Stack>
          <Stack alignItems="center" direction="row" spacing={2}>
            <Typography color="text.secondary" variant="body2">
              {roles.join(", ")}
            </Typography>
            <Button
              color="inherit"
              onClick={() => void logout()}
              startIcon={<LogoutOutlinedIcon />}
              variant="outlined"
            >
              Cikis
            </Button>
          </Stack>
        </Box>
        <Box component="main" sx={{ p: 4 }}>{children}</Box>
      </Box>
    </Box>
  );
}
