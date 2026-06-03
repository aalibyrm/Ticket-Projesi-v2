import { apiClient } from "./httpClient";
import type { MobilePickedFile, UploadUrlResponse } from "./mobileApiTypes";

export async function uploadTicketAttachment(ticketId: string, file: MobilePickedFile) {
  const uploadUrl = await createUploadUrl(ticketId, file);
  const fileResponse = await fetch(file.uri);

  if (!fileResponse.ok) {
    throw new Error("Secilen dosya okunamadi.");
  }

  const fileBody = await fileResponse.blob();
  const uploadHeaders = new Headers(uploadUrl.requiredHeaders);

  if (!uploadHeaders.has("Content-Type")) {
    uploadHeaders.set("Content-Type", file.mimeType || "application/octet-stream");
  }

  const uploadResponse = await fetch(uploadUrl.uploadUrl, {
    body: fileBody,
    headers: uploadHeaders,
    method: uploadUrl.method
  });

  if (!uploadResponse.ok) {
    throw new Error("Dosya object storage alanina yuklenemedi.");
  }

  await apiClient.post(`/api/files/uploads/${uploadUrl.fileId}/complete`);
  return uploadUrl.fileId;
}

async function createUploadUrl(ticketId: string, file: MobilePickedFile) {
  const response = await apiClient.post<UploadUrlResponse>("/api/files/uploads", {
    contentType: file.mimeType || "application/octet-stream",
    originalFilename: file.name,
    sizeBytes: file.size,
    ticketId
  });

  return response.data;
}
