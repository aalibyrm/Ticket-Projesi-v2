import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";
import AttachFileOutlinedIcon from "@mui/icons-material/AttachFileOutlined";
import CloseOutlinedIcon from "@mui/icons-material/CloseOutlined";
import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import PersonOutlineOutlinedIcon from "@mui/icons-material/PersonOutlineOutlined";
import SendOutlinedIcon from "@mui/icons-material/SendOutlined";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  Divider,
  Drawer,
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
  useCustomerTicketAgentSummary,
  useCustomerTicket,
  useCustomerTicketConversationReadState,
  useCustomerTicketComments,
  useMarkCustomerTicketConversationRead,
  useUploadTicketAttachment,
} from "~/features/customer/customerQueries";
import type { TicketAgentSummaryResponse, TicketAttachmentResponse, TicketResponse } from "~/features/customer/customerTypes";
import { tmTokens } from "~/shared/theme/tmTokens";
import { actorDisplayName } from "~/shared/userDisplay";

const commentSchema = z.string().trim().min(3).max(5000);
const maxAttachmentBytes = 10 * 1024 * 1024;

export function CustomerTicketDetailPage() {
  const { ticketId = "" } = useParams();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [comment, setComment] = useState("");
  const [fileError, setFileError] = useState<string>();
  const [isAgentDrawerOpen, setAgentDrawerOpen] = useState(false);

  const ticketQuery = useCustomerTicket(ticketId);
  const agentSummaryQuery = useCustomerTicketAgentSummary(ticketId);
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
  const fallbackAgentSummary = buildFallbackAgentSummary(ticket);
  const visibleAgentSummary = agentSummaryQuery.data
    ?? (agentSummaryQuery.isError ? fallbackAgentSummary : undefined);
  const isAgentSummaryError = agentSummaryQuery.isError && !visibleAgentSummary?.assigned;

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
              <AgentSummaryTrigger
                isError={isAgentSummaryError}
                isLoading={agentSummaryQuery.isLoading}
                onOpen={() => setAgentDrawerOpen(true)}
                summary={visibleAgentSummary}
              />
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

      <AgentSummaryDrawer
        isError={isAgentSummaryError}
        isLoading={agentSummaryQuery.isLoading}
        onClose={() => setAgentDrawerOpen(false)}
        open={isAgentDrawerOpen}
        summary={visibleAgentSummary}
      />
    </Stack>
  );
}

function AgentSummaryTrigger({
  isError,
  isLoading,
  onOpen,
  summary,
}: {
  isError: boolean;
  isLoading: boolean;
  onOpen: () => void;
  summary?: TicketAgentSummaryResponse;
}) {
  if (isLoading) {
    return (
      <Stack alignItems="center" direction="row" spacing={1}>
        <CircularProgress size={16} />
        <Typography color="text.secondary" variant="body2">
          Temsilci bilgisi yukleniyor
        </Typography>
      </Stack>
    );
  }

  if (isError) {
    return (
      <Typography color="error" variant="body2">
        Temsilci bilgisi alinamadi
      </Typography>
    );
  }

  if (!summary?.assigned) {
    return (
      <Typography color="text.secondary" variant="body2">
        Temsilci atanmadi
      </Typography>
    );
  }

  return (
    <Button
      color="inherit"
      onClick={onOpen}
      startIcon={<PersonOutlineOutlinedIcon />}
      sx={{ alignSelf: "flex-start", px: 0 }}
    >
      Temsilci: {summary.displayName ?? "Destek Temsilcisi"}
    </Button>
  );
}

function buildFallbackAgentSummary(ticket: TicketResponse): TicketAgentSummaryResponse | undefined {
  if (!ticket.assigneeId) {
    return undefined;
  }

  return {
    agentId: ticket.assigneeId,
    assigned: true,
    assignedTeamId: ticket.assignedTeamId ?? null,
    displayName: actorDisplayName(ticket.assigneeId, undefined, "Destek Temsilcisi"),
    email: null,
    metricsAvailable: false,
    resolvedTicketCount: 0,
    slaBreachedTicketCount: 0,
    slaCompliancePercentage: 0,
    slaMetTicketCount: 0,
  };
}

function AgentSummaryDrawer({
  isError,
  isLoading,
  onClose,
  open,
  summary,
}: {
  isError: boolean;
  isLoading: boolean;
  onClose: () => void;
  open: boolean;
  summary?: TicketAgentSummaryResponse;
}) {
  return (
    <Drawer
      anchor="right"
      onClose={onClose}
      open={open}
      PaperProps={{
        sx: {
          p: 3,
          width: { sm: 420, xs: "100%" },
        },
      }}
    >
      <Stack spacing={3}>
        <Stack alignItems="center" direction="row" justifyContent="space-between">
          <Typography variant="h5">Temsilci detayi</Typography>
          <IconButton aria-label="Temsilci detayini kapat" onClick={onClose}>
            <CloseOutlinedIcon />
          </IconButton>
        </Stack>

        {isLoading && (
          <Stack alignItems="center" direction="row" spacing={1}>
            <CircularProgress size={18} />
            <Typography color="text.secondary">Temsilci bilgisi yukleniyor.</Typography>
          </Stack>
        )}

        {isError && (
          <Alert severity="error" variant="outlined">
            Temsilci detaylari alinamadi.
          </Alert>
        )}

        {!isLoading && !isError && !summary?.assigned && (
          <Alert severity="info" variant="outlined">
            Bu ticket icin henuz temsilci atanmadi.
          </Alert>
        )}

        {!isLoading && !isError && summary?.assigned && (
          <>
            <Stack spacing={0.5}>
              <Typography variant="h6">{summary.displayName ?? "Destek Temsilcisi"}</Typography>
              {summary.email && (
                <Typography color="text.secondary" variant="body2">
                  {summary.email}
                </Typography>
              )}
            </Stack>
            {summary.metricsAvailable === false && (
              <Alert severity="warning" variant="outlined">
                Performans metrikleri su an alinamadi.
              </Alert>
            )}
            <Box
              sx={{
                display: "grid",
                gap: 1.5,
                gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
              }}
            >
              <AgentMetricBox label="SLA uyumu" value={formatPercentValue(summary.slaCompliancePercentage)} />
              <AgentMetricBox label="Cozdugu ticket" value={summary.resolvedTicketCount.toString()} />
              <AgentMetricBox label="Hedefte" value={summary.slaMetTicketCount.toString()} />
              <AgentMetricBox label="Ihlal" value={summary.slaBreachedTicketCount.toString()} />
            </Box>
          </>
        )}
      </Stack>
    </Drawer>
  );
}

function AgentMetricBox({ label, value }: { label: string; value: string }) {
  return (
    <Box
      sx={{
        bgcolor: tmTokens.colors.surfaceLow,
        border: "1px solid",
        borderColor: tmTokens.colors.border,
        borderRadius: tmTokens.radius.md,
        minHeight: 92,
        p: 2,
      }}
    >
      <Typography color="text.secondary" variant="body2">
        {label}
      </Typography>
      <Typography sx={{ mt: 1 }} variant="h5">
        {value}
      </Typography>
    </Box>
  );
}

function formatPercentValue(value: number | string) {
  const parsed = typeof value === "string" ? Number(value) : value;
  if (!Number.isFinite(parsed)) {
    return "0.0%";
  }
  return `${parsed.toFixed(1)}%`;
}
