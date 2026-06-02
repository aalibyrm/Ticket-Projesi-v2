import DoneAllOutlinedIcon from "@mui/icons-material/DoneAllOutlined";
import {
  Button,
  Chip,
  List,
  ListItem,
  ListItemText,
  Paper,
  Stack,
  Typography,
} from "@mui/material";
import {
  CustomerEmptyState,
  CustomerErrorState,
  CustomerLoadingState,
} from "~/features/customer/components/CustomerState";
import { formatDateTime } from "~/features/customer/formatters";
import { useMarkNotificationRead, useNotifications } from "~/features/customer/customerQueries";

export function CustomerNotificationsPage() {
  const notifications = useNotifications();
  const markRead = useMarkNotificationRead();

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
                <ListItemText
                  primary={item.title}
                  secondary={`${item.message} / ${formatDateTime(item.createdAt)}`}
                />
              </ListItem>
            ))}
          </List>
        </Paper>
      )}
    </Stack>
  );
}
