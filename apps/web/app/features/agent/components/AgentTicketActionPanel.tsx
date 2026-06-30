import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import PersonAddAltOutlinedIcon from "@mui/icons-material/PersonAddAltOutlined";
import SaveOutlinedIcon from "@mui/icons-material/SaveOutlined";
import TimerOutlinedIcon from "@mui/icons-material/TimerOutlined";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Alert,
  Box,
  Button,
  Divider,
  FormControl,
  IconButton,
  FormLabel,
  List,
  ListItem,
  ListItemText,
  MenuItem,
  Select,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import {
  useAddAgentWorklog,
  useAgentAttachmentDownloadUrl,
  useAgentTicketComments,
  useAgentWorklogs,
  useAssignAgentTicket,
  useChangeAgentTicketStatus,
  useSupportTeamMembers,
} from "~/features/agent/agentQueries";
import type {
  TeamMemberResponse,
  TicketAttachmentResponse,
  TicketResponse,
  TicketStatus,
} from "~/features/agent/agentTypes";
import { selectAuthUser } from "~/features/auth/authSlice";
import { formatDate, formatDateTime, formatFileSize } from "~/features/customer/formatters";
import { useAppSelector } from "~/shared/store/hooks";
import { tmTokens } from "~/shared/theme/tmTokens";
import { actorDisplayName } from "~/shared/userDisplay";
import { backendUuidSchema } from "~/shared/validation/uuid";

const statusLabels: Record<TicketStatus, string> = {
  CLOSED: "Kapali",
  IN_PROGRESS: "Islemde",
  NEW: "Acik",
  RESOLVED: "Cozuldu",
  WAITING_FOR_CUSTOMER: "Musteri bekleniyor",
};

const allowedStatusTransitions: Record<TicketStatus, TicketStatus[]> = {
  CLOSED: [],
  IN_PROGRESS: ["WAITING_FOR_CUSTOMER", "RESOLVED"],
  NEW: ["IN_PROGRESS"],
  RESOLVED: ["CLOSED", "IN_PROGRESS"],
  WAITING_FOR_CUSTOMER: ["IN_PROGRESS"],
};

const uuidSchema = backendUuidSchema();
const assignmentSchema = z.object({
  assigneeId: uuidSchema,
});
const worklogSchema = z.object({
  description: z.string().trim().min(3).max(2000),
  durationMinutes: z.coerce.number().int().min(1).max(1440),
  workDate: z.string().min(1),
});

type WorklogFormValues = z.input<typeof worklogSchema>;

const menuProps = {
  PaperProps: {
    sx: {
      border: `1px solid ${tmTokens.colors.border}`,
      boxShadow: "none",
      maxHeight: 280,
      maxWidth: 300,
      mt: 0.5,
      "& .MuiMenuItem-root": {
        minHeight: 44,
      },
    },
  },
};

const panelFieldSx = {
  "& .MuiInputLabel-root": {
    color: tmTokens.colors.secondary,
    ...tmTokens.typography.labelSm,
    textTransform: "uppercase",
  },
  "& .MuiInputLabel-root.Mui-focused": {
    color: tmTokens.colors.secondary,
  },
  "& .MuiOutlinedInput-root": {
    borderRadius: `${tmTokens.radius.md}px`,
    ...tmTokens.typography.bodyMd,
    "& fieldset": {
      borderColor: tmTokens.colors.border,
    },
    "&:hover fieldset": {
      borderColor: tmTokens.colors.border,
    },
    "&.Mui-focused fieldset": {
      borderColor: tmTokens.colors.primaryContainer,
      borderWidth: 1,
    },
  },
} as const;

const assignmentFieldSx = {
  gap: 0.75,
  minWidth: 0,
} as const;

const assignmentLabelSx = {
  color: tmTokens.colors.secondary,
  ...tmTokens.typography.labelSm,
  textTransform: "uppercase",
} as const;

const assignmentSelectSx = {
  borderRadius: `${tmTokens.radius.md}px`,
  ...tmTokens.typography.bodyMd,
  "& .MuiSelect-select": {
    alignItems: "center",
    display: "flex",
    minHeight: 24,
    minWidth: 0,
    py: 1.25,
  },
  "& fieldset": {
    borderColor: tmTokens.colors.border,
  },
  "&:hover fieldset": {
    borderColor: tmTokens.colors.border,
  },
  "&.Mui-focused fieldset": {
    borderColor: tmTokens.colors.primaryContainer,
    borderWidth: 1,
  },
} as const;

function memberName(member: TeamMemberResponse, user: ReturnType<typeof selectAuthUser>) {
  return member.displayName ?? actorDisplayName(member.actorId, user, "Agent");
}

function SectionTitle({ children }: { children: string }) {
  return (
    <Typography sx={{ color: tmTokens.colors.onSurface, ...tmTokens.typography.headlineSm }}>
      {children}
    </Typography>
  );
}

function StatusValue({ label, value }: { label: string; value: string }) {
  return (
    <Box
      sx={{
        border: `1px solid ${tmTokens.colors.border}`,
        borderRadius: `${tmTokens.radius.md}px`,
        px: 1.5,
        py: 1.25,
      }}
    >
      <Typography
        sx={{
          color: tmTokens.colors.secondary,
          textTransform: "uppercase",
          ...tmTokens.typography.labelSm,
        }}
      >
        {label}
      </Typography>
      <Typography sx={{ color: tmTokens.colors.onSurface, mt: 0.5, ...tmTokens.typography.bodyMdBold }}>
        {value}
      </Typography>
    </Box>
  );
}

function MenuItemContent({ primary, secondary }: { primary: string; secondary?: string }) {
  return (
    <Stack sx={{ minWidth: 0, width: "100%" }}>
      <Typography noWrap sx={{ color: tmTokens.colors.onSurface, ...tmTokens.typography.bodyMd }}>
        {primary}
      </Typography>
      {secondary && (
        <Typography noWrap sx={{ color: tmTokens.colors.secondary, ...tmTokens.typography.labelSm }}>
          {secondary}
        </Typography>
      )}
    </Stack>
  );
}

function SelectValueText({ muted = false, value }: { muted?: boolean; value: string }) {
  return (
    <Typography
      component="span"
      noWrap
      sx={{
        color: muted ? tmTokens.colors.secondary : tmTokens.colors.onSurface,
        minWidth: 0,
        ...tmTokens.typography.bodyMd,
      }}
    >
      {value}
    </Typography>
  );
}

export function AgentTicketActionPanel({ ticket }: { ticket: TicketResponse }) {
  const user = useAppSelector(selectAuthUser);
  const isCurrentAssignee = Boolean(user?.id && ticket.assigneeId === user.id);
  const statusMutation = useChangeAgentTicketStatus(ticket.id);
  const assignMutation = useAssignAgentTicket(ticket.id);
  const commentsQuery = useAgentTicketComments(ticket.id);
  const worklogsQuery = useAgentWorklogs(ticket.id, isCurrentAssignee);
  const addWorklog = useAddAgentWorklog(ticket.id);
  const downloadUrl = useAgentAttachmentDownloadUrl();
  const [assigneeId, setAssigneeId] = useState(ticket.assigneeId ?? "");
  const [assignmentError, setAssignmentError] = useState<string>();
  const assignedTeamId = ticket.assignedTeamId ?? "";
  const teamMembersQuery = useSupportTeamMembers(assignedTeamId);

  const {
    formState: { errors },
    handleSubmit,
    register,
    reset,
  } = useForm<WorklogFormValues>({
    defaultValues: {
      description: "",
      durationMinutes: 30,
      workDate: new Date().toISOString().slice(0, 10),
    },
    resolver: zodResolver(worklogSchema),
  });

  useEffect(() => {
    setAssigneeId(ticket.assigneeId ?? "");
  }, [ticket.assigneeId, ticket.id]);

  useEffect(() => {
    if (!teamMembersQuery.data || !assigneeId) {
      return;
    }
    if (!teamMembersQuery.data.some((member) => member.actorId === assigneeId)) {
      setAssigneeId("");
    }
  }, [assigneeId, teamMembersQuery.data]);

  async function changeStatus(status: TicketStatus) {
    await statusMutation.mutateAsync({ status });
  }

  async function submitAssignment() {
    setAssignmentError(undefined);
    if (!assignedTeamId) {
      setAssignmentError("Ticket icin atanmis ekip bulunamadi.");
      return;
    }
    const parsed = assignmentSchema.safeParse({ assigneeId });
    if (!parsed.success) {
      setAssignmentError("Agent secmelisin.");
      return;
    }

    await assignMutation.mutateAsync({
      assigneeId: parsed.data.assigneeId,
      assignedTeamId,
    });
  }

  async function assignToMe() {
    setAssignmentError(undefined);
    if (!user || !uuidSchema.safeParse(user.id).success) {
      setAssignmentError("Oturum kimligi assignment icin UUID formatinda degil.");
      return;
    }
    const teamId = ticket.assignedTeamId;
    if (!teamId) {
      setAssignmentError("Ticket icin atanmis ekip bulunamadi.");
      return;
    }

    await assignMutation.mutateAsync({
      assignedTeamId: teamId,
      assigneeId: user.id,
    });
    setAssigneeId(user.id);
  }

  async function submitWorklog(values: WorklogFormValues) {
    const parsed = worklogSchema.parse(values);
    await addWorklog.mutateAsync(parsed);
    reset({
      description: "",
      durationMinutes: 30,
      workDate: new Date().toISOString().slice(0, 10),
    });
  }

  async function downloadAttachment(attachment: TicketAttachmentResponse) {
    const response = await downloadUrl.mutateAsync(attachment.id);
    window.open(response.downloadUrl, "_blank", "noopener,noreferrer");
  }

  const worklogs = worklogsQuery.data ?? [];
  const teamMembers = teamMembersQuery.data ?? [];
  const isAdmin = Boolean(user?.roles.some((role) => role.toUpperCase() === "ADMIN"));
  const selectedAssigneeInMembers = teamMembers.some((member) => member.actorId === assigneeId);
  const selectedAssignee = teamMembers.find((member) => member.actorId === assigneeId);
  const currentMemberInSelectedTeam = teamMembers.find((member) => member.actorId === user?.id);
  const canAssignOthers = Boolean(
    isAdmin || currentMemberInSelectedTeam?.teamLead,
  );
  const assignedTeamLabel = teamMembers[0]?.teamCode ?? (assignedTeamId ? "Atanmis ekip" : "Ekip yok");
  const canShowAssignment = !ticket.assigneeId;
  const waitingSince = new Date(ticket.updatedAt).getTime();
  const hasCustomerExternalReply = (commentsQuery.data ?? []).some(
    (comment) =>
      comment.authorId === ticket.customerId
      && comment.visibility === "EXTERNAL"
      && new Date(comment.createdAt).getTime() > waitingSince,
  );
  const statusOptions = allowedStatusTransitions[ticket.status].filter(
    (status) => ticket.status !== "WAITING_FOR_CUSTOMER" || status !== "IN_PROGRESS" || hasCustomerExternalReply,
  );

  return (
    <Stack
      component="aside"
      spacing={2.25}
      sx={{
        bgcolor: "background.paper",
        borderLeft: "1px solid",
        borderColor: "divider",
        flex: "0 0 320px",
        minHeight: 0,
        msOverflowStyle: "none",
        overflowY: "auto",
        px: 2,
        py: 2.5,
        scrollbarWidth: "none",
        "&::-webkit-scrollbar": {
          display: "none",
        },
      }}
    >
      {isCurrentAssignee && (
        <>
          <Stack spacing={1.5}>
            <SectionTitle>Aksiyonlar</SectionTitle>
            {statusMutation.isError && (
              <Alert severity="error" variant="outlined">
                Aksiyon kaydedilemedi.
              </Alert>
            )}
            <StatusValue label="Mevcut status" value={statusLabels[ticket.status]} />
            {commentsQuery.isError && ticket.status === "WAITING_FOR_CUSTOMER" && (
              <Alert severity="warning" variant="outlined">
                Musteri yanit durumu kontrol edilemedi.
              </Alert>
            )}
            {statusOptions.length === 0 ? (
              <Typography color="text.secondary" sx={tmTokens.typography.bodyMd}>
                Uygun statu gecisi yok.
              </Typography>
            ) : (
              statusOptions.map((status, index) => (
                <Button
                  disabled={statusMutation.isPending}
                  fullWidth
                  key={status}
                  onClick={() => void changeStatus(status)}
                  startIcon={<SaveOutlinedIcon />}
                  variant={index === 0 ? "contained" : "outlined"}
                >
                  {statusLabels[status]} yap
                </Button>
              ))
            )}
          </Stack>

          <Divider />
        </>
      )}

      {canShowAssignment && (
        <>
          {(assignMutation.isError || assignmentError || teamMembersQuery.isError) && (
            <Alert severity="error" variant="outlined">
              {assignmentError ?? (teamMembersQuery.isError ? "Ekip uyeleri alinamadi." : "Atama kaydedilemedi.")}
            </Alert>
          )}

          <Stack spacing={1.5}>
            <SectionTitle>Atama</SectionTitle>
            <Button
              disabled={assignMutation.isPending}
              fullWidth
              onClick={() => void assignToMe()}
              startIcon={<PersonAddAltOutlinedIcon />}
              variant="outlined"
            >
              Bana ata
            </Button>
            {canAssignOthers ? (
              <>
                <FormControl disabled={!assignedTeamId || teamMembersQuery.isLoading} fullWidth size="small" sx={assignmentFieldSx}>
                  <FormLabel id="agent-assignee-label" sx={assignmentLabelSx}>
                    Agent
                  </FormLabel>
                  <Select
                    displayEmpty
                    labelId="agent-assignee-label"
                    MenuProps={menuProps}
                    onChange={(event) => setAssigneeId(event.target.value)}
                    renderValue={(value) => {
                      if (!value) {
                        if (!assignedTeamId) {
                          return <SelectValueText muted value="Atanmis ekip yok" />;
                        }
                        return (
                          <SelectValueText
                            muted
                            value={teamMembersQuery.isLoading ? "Agentlar yukleniyor" : "Agent sec"}
                          />
                        );
                      }
                      return (
                        <SelectValueText
                          value={selectedAssignee ? memberName(selectedAssignee, user) : actorDisplayName(value, user, "Agent")}
                        />
                      );
                    }}
                    sx={assignmentSelectSx}
                    value={assigneeId}
                  >
                    <MenuItem value="">
                      {!assignedTeamId
                        ? "Atanmis ekip yok"
                        : teamMembersQuery.isLoading
                          ? "Agentlar yukleniyor"
                          : "Agent sec"}
                    </MenuItem>
                    {assigneeId && !selectedAssigneeInMembers && (
                      <MenuItem value={assigneeId}>
                        <MenuItemContent primary={actorDisplayName(assigneeId, user, "Agent")} />
                      </MenuItem>
                    )}
                    {teamMembers.map((member) => (
                      <MenuItem key={member.actorId} value={member.actorId}>
                        <MenuItemContent
                          primary={memberName(member, user)}
                          secondary={member.teamLead ? "Lead" : member.email ?? member.teamCode}
                        />
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <Button disabled={assignMutation.isPending} fullWidth onClick={() => void submitAssignment()} variant="outlined">
                  Atamayi kaydet
                </Button>
              </>
            ) : (
              <Stack spacing={1}>
                <StatusValue
                  label="Ekip"
                  value={teamMembersQuery.isLoading ? "Ekip yukleniyor" : assignedTeamLabel}
                />
                <Typography color="text.secondary" sx={tmTokens.typography.bodyMd}>
                  Lead olmayan agentlar yalniz kendi adina atama yapabilir.
                </Typography>
              </Stack>
            )}
          </Stack>

          <Divider />
        </>
      )}

      {isCurrentAssignee && (
        <>
          <Stack component="form" onSubmit={handleSubmit((values) => void submitWorklog(values))} spacing={1.5}>
            <SectionTitle>Worklog</SectionTitle>
            {addWorklog.isError && (
              <Alert severity="error" variant="outlined">
                Worklog kaydedilemedi.
              </Alert>
            )}
            <TextField
              error={Boolean(errors.workDate)}
              helperText={errors.workDate?.message}
              label="Tarih"
              size="small"
              type="date"
              {...register("workDate")}
              InputLabelProps={{ shrink: true }}
              sx={panelFieldSx}
              variant="outlined"
            />
            <TextField
              error={Boolean(errors.durationMinutes)}
              helperText={errors.durationMinutes?.message}
              label="Sure (dk)"
              size="small"
              type="number"
              {...register("durationMinutes")}
              sx={panelFieldSx}
              variant="outlined"
            />
            <TextField
              error={Boolean(errors.description)}
              helperText={errors.description?.message}
              label="Aciklama"
              minRows={2}
              multiline
              size="small"
              {...register("description")}
              sx={panelFieldSx}
              variant="outlined"
            />
            <Button disabled={addWorklog.isPending} fullWidth startIcon={<TimerOutlinedIcon />} type="submit" variant="outlined">
              Worklog ekle
            </Button>
          </Stack>

          <Stack spacing={1}>
            {worklogsQuery.isLoading && <Typography color="text.secondary">Worklog yukleniyor.</Typography>}
            {worklogs.length === 0 && !worklogsQuery.isLoading ? (
              <Typography color="text.secondary">Worklog yok.</Typography>
            ) : (
              <List disablePadding>
                {worklogs.map((worklog) => (
                  <ListItem disableGutters key={worklog.id}>
                    <ListItemText
                      primary={`${worklog.durationMinutes} dk / ${formatDate(worklog.workDate)}`}
                      secondary={worklog.description}
                    />
                  </ListItem>
                ))}
              </List>
            )}
          </Stack>

          <Divider />
        </>
      )}

      <Stack spacing={1.5}>
        <SectionTitle>Dosyalar</SectionTitle>
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
                  secondary={`${formatFileSize(attachment.sizeBytes)} / ${attachment.validationStatus} / ${formatDateTime(attachment.createdAt)}`}
                />
              </ListItem>
            ))}
          </List>
        )}
      </Stack>
    </Stack>
  );
}
