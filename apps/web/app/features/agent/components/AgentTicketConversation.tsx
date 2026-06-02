import NoteAddOutlinedIcon from "@mui/icons-material/NoteAddOutlined";
import SendOutlinedIcon from "@mui/icons-material/SendOutlined";
import {
  Alert,
  Box,
  Button,
  Chip,
  Divider,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { z } from "zod";
import {
  useAddAgentExternalComment,
  useAddAgentInternalNote,
  useAgentTicketComments,
} from "~/features/agent/agentQueries";
import { AgentErrorState, AgentLoadingState } from "~/features/agent/components/AgentState";
import type { TicketResponse } from "~/features/agent/agentTypes";
import { formatDateTime } from "~/features/customer/formatters";

const messageSchema = z.string().trim().min(3).max(5000);

export function AgentTicketConversation({ ticket }: { ticket: TicketResponse }) {
  const commentsQuery = useAgentTicketComments(ticket.id);
  const externalComment = useAddAgentExternalComment(ticket.id);
  const internalNote = useAddAgentInternalNote(ticket.id);
  const [reply, setReply] = useState("");
  const [note, setNote] = useState("");

  async function submitReply() {
    const parsed = messageSchema.safeParse(reply);
    if (!parsed.success) {
      return;
    }
    await externalComment.mutateAsync(parsed.data);
    setReply("");
  }

  async function submitInternalNote() {
    const parsed = messageSchema.safeParse(note);
    if (!parsed.success) {
      return;
    }
    await internalNote.mutateAsync(parsed.data);
    setNote("");
  }

  const comments = commentsQuery.data ?? [];
  const isReplyInvalid = reply.length > 0 && !messageSchema.safeParse(reply).success;
  const isNoteInvalid = note.length > 0 && !messageSchema.safeParse(note).success;

  return (
    <Stack
      sx={{
        bgcolor: "background.default",
        flex: 1,
        minHeight: 640,
      }}
    >
      <Stack spacing={2} sx={{ flex: 1, overflowY: "auto", p: 3 }}>
        <Box sx={{ display: "flex", justifyContent: "center" }}>
          <Chip label={`Ticket acildi / ${formatDateTime(ticket.createdAt)}`} size="small" />
        </Box>
        <MessageBubble
          align="left"
          author="Musteri"
          body={ticket.description}
          createdAt={ticket.createdAt}
          visibility="EXTERNAL"
        />
        {commentsQuery.isLoading && <AgentLoadingState label="Mesajlar yukleniyor" />}
        {commentsQuery.isError && (
          <AgentErrorState message="Mesajlar alinamadi." onRetry={() => void commentsQuery.refetch()} />
        )}
        {comments.map((comment) => (
          <MessageBubble
            align={comment.authorId === ticket.customerId ? "left" : "right"}
            author={comment.authorId === ticket.customerId ? "Musteri" : "Destek"}
            body={comment.body}
            createdAt={comment.createdAt}
            key={comment.id}
            visibility={comment.visibility}
          />
        ))}
      </Stack>
      <Divider />
      <Stack spacing={2} sx={{ bgcolor: "background.paper", p: 3 }}>
        {(externalComment.isError || internalNote.isError) && (
          <Alert severity="error" variant="outlined">
            Mesaj kaydedilemedi.
          </Alert>
        )}
        <TextField
          error={isReplyInvalid}
          helperText={isReplyInvalid ? "Yanit 3-5000 karakter araliginda olmali." : undefined}
          label="Musteriye yanit"
          minRows={3}
          multiline
          onChange={(event) => setReply(event.target.value)}
          value={reply}
          variant="standard"
        />
        <Stack direction="row" justifyContent="flex-end">
          <Button
            disabled={!messageSchema.safeParse(reply).success || externalComment.isPending}
            onClick={() => void submitReply()}
            startIcon={<SendOutlinedIcon />}
            variant="contained"
          >
            Yanitla
          </Button>
        </Stack>
        <TextField
          error={isNoteInvalid}
          helperText={isNoteInvalid ? "Not 3-5000 karakter araliginda olmali." : undefined}
          label="Ic not"
          minRows={2}
          multiline
          onChange={(event) => setNote(event.target.value)}
          value={note}
          variant="standard"
        />
        <Stack direction="row" justifyContent="flex-end">
          <Button
            disabled={!messageSchema.safeParse(note).success || internalNote.isPending}
            onClick={() => void submitInternalNote()}
            startIcon={<NoteAddOutlinedIcon />}
            variant="outlined"
          >
            Ic not ekle
          </Button>
        </Stack>
      </Stack>
    </Stack>
  );
}

function MessageBubble({
  align,
  author,
  body,
  createdAt,
  visibility,
}: {
  align: "left" | "right";
  author: string;
  body: string;
  createdAt: string;
  visibility: "EXTERNAL" | "INTERNAL";
}) {
  const isInternal = visibility === "INTERNAL";

  return (
    <Stack
      alignItems={align === "right" ? "flex-end" : "flex-start"}
      spacing={0.75}
      sx={{ alignSelf: align === "right" ? "flex-end" : "flex-start", maxWidth: "78%", width: "100%" }}
    >
      <Stack direction="row" spacing={1}>
        <Typography color="text.secondary" variant="caption">
          {author} / {formatDateTime(createdAt)}
        </Typography>
        {isInternal && <Chip label="Ic not" size="small" variant="outlined" />}
      </Stack>
      <Box
        sx={{
          bgcolor: isInternal ? "rgba(127, 9, 0, 0.05)" : "background.paper",
          border: "1px solid",
          borderColor: isInternal ? "primary.main" : "divider",
          borderRadius: 2,
          p: 2,
          whiteSpace: "pre-wrap",
        }}
      >
        <Typography>{body}</Typography>
      </Box>
    </Stack>
  );
}
