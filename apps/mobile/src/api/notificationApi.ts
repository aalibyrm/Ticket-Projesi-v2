import { apiClient } from "./httpClient";
import type { NotificationResponse } from "./mobileApiTypes";

export async function listNotifications(read?: boolean) {
  const response = await apiClient.get<NotificationResponse[]>("/api/notifications", {
    params: read === undefined ? undefined : { read }
  });
  return response.data;
}

export async function markNotificationRead(notificationId: string) {
  const response = await apiClient.patch<NotificationResponse>(`/api/notifications/${notificationId}/read`);
  return response.data;
}
