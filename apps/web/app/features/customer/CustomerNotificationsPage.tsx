import DoneAllOutlinedIcon from "@mui/icons-material/DoneAllOutlined";
import {
  Button,
  Chip,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router";
import type { AppRole } from "~/features/auth/authTypes";
import { selectUserRoles } from "~/features/auth/authSlice";
import {
  CustomerEmptyState,
  CustomerErrorState,
  CustomerLoadingState,
} from "~/features/customer/components/CustomerState";
import { formatDateTime } from "~/features/customer/formatters";
import { useMarkNotificationRead, useNotifications } from "~/features/customer/customerQueries";
import type { NotificationResponse } from "~/features/customer/customerTypes";
import { useAppSelector } from "~/shared/store/hooks";

export function CustomerNotificationsPage() {
  const notifications = useNotifications();
  const markRead = useMarkNotificationRead();
  const navigate = useNavigate();
  const roles = useAppSelector(selectUserRoles);

  function openNotification(item: NotificationResponse) {
    if (!item.read) {
      markRead.mutate(item.id);
    }
    const ticketPath = notificationTicketPath(item, roles);
    if (ticketPath) {
      void navigate(ticketPath);
    }
  }

  if (notifications.isLoading) {
    return <CustomerLoadingState label="Bildirimler yukleniyor" />;
  }

  if (notifications.isError) {
    return <CustomerErrorState onRetry={() => void notifications.refetch()} />;
  }

  const items = notifications.data ?? [];

  return (
    <Stack spacing={3}>
      <Stack spacing={0.75}>
        <Typography variant="overline">Musteri portali</Typography>
        <Typography variant="h4">Bildirimler</Typography>
      </Stack>
      {items.length === 0 ? (
        <CustomerEmptyState message="Bildirim bulunmuyor." />
      ) : (
        <Paper sx={{ p: 2 }}>
          <List disablePadding>
            {items.map((item) => (
              <ListItem
                disablePadding
                divider
                key={item.id}
                secondaryAction={
                  item.read ? (
                    <Chip label="Okundu" size="small" />
                  ) : (
                    <Button
                      disabled={markRead.isPending}
                      onClick={() => markRead.mutate(item.id)}
                      size="small"
                      startIcon={<DoneAllOutlinedIcon />}
                      variant="outlined"
                    >
                      Okundu yap
                    </Button>
                  )
                }
              >
                <ListItemButton
                  disabled={markRead.isPending && !item.read}
                  onClick={() => openNotification(item)}
                  sx={{ minHeight: 72, pr: 18 }}
                >
                  <ListItemText
                    primary={item.title}
                    secondary={`${item.message} / ${formatDateTime(item.createdAt)}`}
                  />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
        </Paper>
      )}
    </Stack>
  );
}

function notificationTicketPath(item: NotificationResponse, roles: AppRole[]) {
  if (!item.ticketId) {
    return undefined;
  }
  if (roles.includes("AGENT") || roles.includes("ADMIN")) {
    return `/agent/tickets/${item.ticketId}`;
  }
  if (roles.includes("CUSTOMER")) {
    return `/tickets/${item.ticketId}`;
  }
  return undefined;
}
