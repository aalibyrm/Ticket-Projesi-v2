import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";
import AttachFileOutlinedIcon from "@mui/icons-material/AttachFileOutlined";
import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import SendOutlinedIcon from "@mui/icons-material/SendOutlined";
import {
  Alert,
  Box,
  Button,
  Chip,
  Divider,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Paper,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { z } from "zod";
import {
  CustomerErrorState,
  CustomerLoadingState,
} from "~/features/customer/components/CustomerState";
import { PriorityChip, TicketStatusChip } from "~/features/customer/components/StatusChips";
import { formatDateTime, formatFileSize } from "~/features/customer/formatters";
import {
  useAddCustomerTicketComment,
  useAttachmentDownloadUrl,
  useCustomerTicket,
  useCustomerTicketConversationReadState,
  useCustomerTicketComments,
  useMarkCustomerTicketConversationRead,
  useUploadTicketAttachment,
} from "~/features/customer/customerQueries";
import type { TicketAttachmentResponse } from "~/features/customer/customerTypes";

const commentSchema = z.string().trim().min(3).max(5000);
const maxAttachmentBytes = 10 * 1024 * 1024;

export function CustomerTicketDetailPage() {
  const { ticketId = "" } = useParams();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [comment, setComment] = useState("");
  const [fileError, setFileError] = useState<string>();

  const ticketQuery = useCustomerTicket(ticketId);
  const commentsQuery = useCustomerTicketComments(ticketId);
  const readStateQuery = useCustomerTicketConversationReadState(ticketId);
  const addComment = useAddCustomerTicketComment(ticketId);
  const {
    isPending: isMarkingConversationRead,
    mutate: markConversationRead,
  } = useMarkCustomerTicketConversationRead(ticketId);
  const uploadAttachment = useUploadTicketAttachment();
  const downloadUrl = useAttachmentDownloadUrl();
  const comments = commentsQuery.data ?? [];
  const unreadCount = readStateQuery.data?.unreadCount ?? 0;
  const lastMarkReadRequestRef = useRef<string>();

  useEffect(() => {
    const markReadRequestKey = `${ticketId}:${commentsQuery.dataUpdatedAt}:${unreadCount}`;
    if (
      !ticketId
      || !commentsQuery.isSuccess
      || unreadCount <= 0
      || isMarkingConversationRead
      || lastMarkReadRequestRef.current === markReadRequestKey
    ) {
      return;
    }

    lastMarkReadRequestRef.current = markReadRequestKey;
    markConversationRead();
  }, [
    commentsQuery.dataUpdatedAt,
    commentsQuery.isSuccess,
    isMarkingConversationRead,
    markConversationRead,
    ticketId,
    unreadCount,
  ]);

  async function submitComment() {
    const parsed = commentSchema.safeParse(comment);
    if (!parsed.success) {
      return;
    }

    await addComment.mutateAsync(parsed.data);
    setComment("");
  }

  async function handleFileSelected(file?: File) {
    setFileError(undefined);
    if (!file || !ticketId) {
      return;
    }

    if (file.size > maxAttachmentBytes) {
      setFileError("Dosya 10 MB'tan kucuk olmali.");
      return;
    }

    await uploadAttachment.mutateAsync({ file, ticketId });
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  }

  async function downloadAttachment(attachment: TicketAttachmentResponse) {
    const response = await downloadUrl.mutateAsync(attachment.id);
    window.open(response.downloadUrl, "_blank", "noopener,noreferrer");
  }

  if (ticketQuery.isLoading) {
    return <CustomerLoadingState label="Ticket detayi yukleniyor" />;
  }

  if (ticketQuery.isError || !ticketQuery.data) {
    return <CustomerErrorState onRetry={() => void ticketQuery.refetch()} />;
  }

  const ticket = ticketQuery.data;
  const isCommentInvalid = comment.length > 0 && !commentSchema.safeParse(comment).success;

  return (
    <Stack spacing={3}>
      <Button
        color="inherit"
        onClick={() => navigate("/tickets")}
        startIcon={<ArrowBackOutlinedIcon />}
        sx={{ alignSelf: "flex-start" }}
      >
        Taleplere don
      </Button>

      <Paper sx={{ p: 3 }}>
        <Stack spacing={3}>
          <Stack direction={{ md: "row", xs: "column" }} justifyContent="space-between" spacing={2}>
            <Stack spacing={1}>
              <Typography color="text.secondary" variant="body2">
                {ticket.ticketNumber} / {ticket.productName}
              </Typography>
              <Typography variant="h4">{ticket.summary}</Typography>
              <Stack direction="row" spacing={1}>
                <TicketStatusChip status={ticket.status} />
                <PriorityChip priority={ticket.priority} />
              </Stack>
            </Stack>
            <Typography color="text.secondary" variant="body2">
              Guncelleme: {formatDateTime(ticket.updatedAt)}
            </Typography>
          </Stack>
          <Divider />
          <Typography sx={{ whiteSpace: "pre-wrap" }}>{ticket.description}</Typography>
        </Stack>
      </Paper>

      <Stack direction={{ lg: "row", xs: "column" }} spacing={3}>
        <Paper sx={{ flex: 2, p: 3 }}>
          <Stack spacing={2}>
            <Stack alignItems="center" direction="row" justifyContent="space-between" spacing={1}>
              <Typography variant="h6">Mesajlar</Typography>
              {unreadCount > 0 && <Chip color="primary" label={`${unreadCount} yeni mesaj`} size="small" />}
            </Stack>
            {readStateQuery.isError && (
              <Alert severity="warning" variant="outlined">
                Okunma durumu alinamadi.
              </Alert>
            )}
            {commentsQuery.isError && <CustomerErrorState message="Mesajlar alinamadi." onRetry={() => void commentsQuery.refetch()} />}
            {commentsQuery.isLoading && <CustomerLoadingState label="Mesajlar yukleniyor" />}
            {!commentsQuery.isLoading && comments.length === 0 && (
              <Typography color="text.secondary">Bu ticket icin henuz gorunur mesaj yok.</Typography>
            )}
            {comments.map((item) => (
              <Box
                key={item.id}
                sx={{
                  border: "1px solid",
                  borderColor: "divider",
                  borderRadius: 2,
                  p: 2,
                }}
              >
                <Typography color="text.secondary" variant="caption">
                  {formatDateTime(item.createdAt)}
                </Typography>
                <Typography sx={{ mt: 0.75, whiteSpace: "pre-wrap" }}>{item.body}</Typography>
              </Box>
            ))}
            <Stack spacing={1.5}>
              <TextField
                error={isCommentInvalid}
                helperText={isCommentInvalid ? "Mesaj 3-5000 karakter araliginda olmali." : undefined}
                label="Yanit yaz"
                minRows={4}
                multiline
                onChange={(event) => setComment(event.target.value)}
                value={comment}
              />
              {addComment.isError && (
                <Alert severity="error" variant="outlined">
                  Mesaj gonderilemedi.
                </Alert>
              )}
              <Button
                disabled={!commentSchema.safeParse(comment).success || addComment.isPending}
                onClick={() => void submitComment()}
                startIcon={<SendOutlinedIcon />}
                sx={{ alignSelf: "flex-end" }}
                variant="contained"
              >
                Gonder
              </Button>
            </Stack>
          </Stack>
        </Paper>

        <Paper sx={{ flex: 1, p: 3 }}>
          <Stack spacing={2}>
            <Stack direction="row" justifyContent="space-between">
              <Typography variant="h6">Dosyalar</Typography>
              <Button
                component="label"
                size="small"
                startIcon={<AttachFileOutlinedIcon />}
                variant="outlined"
              >
                Ekle
                <input
                  hidden
                  onChange={(event) => void handleFileSelected(event.target.files?.[0])}
                  ref={fileInputRef}
                  type="file"
                />
              </Button>
            </Stack>
            {(fileError || uploadAttachment.isError) && (
              <Alert severity="error" variant="outlined">
                {fileError ?? "Dosya yuklenemedi."}
              </Alert>
            )}
            {ticket.attachments.length === 0 ? (
              <Typography color="text.secondary">Dosya eklenmemis.</Typography>
            ) : (
              <List disablePadding>
                {ticket.attachments.map((attachment) => (
                  <ListItem
                    disableGutters
                    key={attachment.id}
                    secondaryAction={
                      <Tooltip title="Indir">
                        <IconButton
                          aria-label={`${attachment.originalFilename} indir`}
                          edge="end"
                          onClick={() => void downloadAttachment(attachment)}
                        >
                          <DownloadOutlinedIcon />
                        </IconButton>
                      </Tooltip>
                    }
                  >
                    <ListItemText
                      primary={attachment.originalFilename}
                      secondary={`${formatFileSize(attachment.sizeBytes)} / ${attachment.validationStatus}`}
                    />
                  </ListItem>
                ))}
              </List>
            )}
          </Stack>
        </Paper>
      </Stack>
    </Stack>
  );
}
