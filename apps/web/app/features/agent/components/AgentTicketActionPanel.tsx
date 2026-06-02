import DownloadOutlinedIcon from "@mui/icons-material/DownloadOutlined";
import PersonAddAltOutlinedIcon from "@mui/icons-material/PersonAddAltOutlined";
import SaveOutlinedIcon from "@mui/icons-material/SaveOutlined";
import TimerOutlinedIcon from "@mui/icons-material/TimerOutlined";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Alert,
  Button,
  Divider,
  IconButton,
  List,
  ListItem,
  ListItemText,
  MenuItem,
  Paper,
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
  useAgentWorklogs,
  useAssignAgentTicket,
  useChangeAgentTicketStatus,
  useSupportTeamMembers,
  useSupportTeams,
} from "~/features/agent/agentQueries";
import type {
  TicketAttachmentResponse,
  TicketResponse,
  TicketStatus,
} from "~/features/agent/agentTypes";
import { selectAuthUser } from "~/features/auth/authSlice";
import { formatDate, formatDateTime, formatFileSize } from "~/features/customer/formatters";
import { useAppSelector } from "~/shared/store/hooks";
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
  assignedTeamId: uuidSchema,
  assigneeId: uuidSchema,
});
const worklogSchema = z.object({
  description: z.string().trim().min(3).max(2000),
  durationMinutes: z.coerce.number().int().min(1).max(1440),
  workDate: z.string().min(1),
});

type WorklogFormValues = z.input<typeof worklogSchema>;

export function AgentTicketActionPanel({ ticket }: { ticket: TicketResponse }) {
  const user = useAppSelector(selectAuthUser);
  const statusMutation = useChangeAgentTicketStatus(ticket.id);
  const assignMutation = useAssignAgentTicket(ticket.id);
  const worklogsQuery = useAgentWorklogs(ticket.id);
  const addWorklog = useAddAgentWorklog(ticket.id);
  const downloadUrl = useAgentAttachmentDownloadUrl();
  const statusOptions = allowedStatusTransitions[ticket.status];
  const teamsQuery = useSupportTeams();
  const [targetStatus, setTargetStatus] = useState<TicketStatus | "">(statusOptions[0] ?? "");
  const [assigneeId, setAssigneeId] = useState(ticket.assigneeId ?? "");
  const [assignedTeamId, setAssignedTeamId] = useState(ticket.assignedTeamId ?? "");
  const [assignmentError, setAssignmentError] = useState<string>();
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
    setTargetStatus(allowedStatusTransitions[ticket.status][0] ?? "");
    setAssigneeId(ticket.assigneeId ?? "");
    setAssignedTeamId(ticket.assignedTeamId ?? "");
  }, [ticket.assignedTeamId, ticket.assigneeId, ticket.id, ticket.status]);

  useEffect(() => {
    if (!teamMembersQuery.data || !assigneeId) {
      return;
    }
    if (!teamMembersQuery.data.some((member) => member.actorId === assigneeId)) {
      setAssigneeId("");
    }
  }, [assigneeId, teamMembersQuery.data]);

  async function changeStatus() {
    if (!targetStatus) {
      return;
    }
    await statusMutation.mutateAsync({ status: targetStatus });
  }

  async function submitAssignment() {
    setAssignmentError(undefined);
    const parsed = assignmentSchema.safeParse({ assignedTeamId, assigneeId });
    if (!parsed.success) {
      setAssignmentError("Ekip ve agent secmelisin.");
      return;
    }

    await assignMutation.mutateAsync({
      assigneeId: parsed.data.assigneeId,
      assignedTeamId: parsed.data.assignedTeamId || null,
    });
  }

  async function assignToMe() {
    setAssignmentError(undefined);
    if (!user || !uuidSchema.safeParse(user.id).success) {
      setAssignmentError("Oturum kimligi assignment icin UUID formatinda degil.");
      return;
    }
    const teamId = assignedTeamId || ticket.assignedTeamId;
    if (!teamId) {
      setAssignmentError("Bana atamak icin once ekip secmelisin.");
      return;
    }

    await assignMutation.mutateAsync({
      assignedTeamId: teamId,
      assigneeId: user.id,
    });
    setAssignedTeamId(teamId);
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
  const teams = teamsQuery.data ?? [];
  const teamMembers = teamMembersQuery.data ?? [];
  const selectedTeamInTeams = teams.some((team) => team.id === assignedTeamId);
  const selectedAssigneeInMembers = teamMembers.some((member) => member.actorId === assigneeId);

  return (
    <Stack
      component="aside"
      spacing={2}
      sx={{
        bgcolor: "background.paper",
        borderLeft: "1px solid",
        borderColor: "divider",
        flex: "0 0 360px",
        overflowY: "auto",
        p: 2.5,
      }}
    >
      <Paper variant="outlined" sx={{ p: 2 }}>
        <Stack spacing={2}>
          <Typography variant="h6">Aksiyonlar</Typography>
          {(statusMutation.isError || assignMutation.isError || assignmentError) && (
            <Alert severity="error" variant="outlined">
              {assignmentError ?? "Aksiyon kaydedilemedi."}
            </Alert>
          )}
          {(teamsQuery.isError || teamMembersQuery.isError) && (
            <Alert severity="error" variant="outlined">
              Ekip katalogu alinamadi.
            </Alert>
          )}
          <TextField
            label="Sonraki status"
            onChange={(event) => setTargetStatus(event.target.value as TicketStatus)}
            select
            size="small"
            value={targetStatus}
          >
            {statusOptions.length === 0 ? (
              <MenuItem value="">Gecis yok</MenuItem>
            ) : (
              statusOptions.map((status) => (
                <MenuItem key={status} value={status}>
                  {statusLabels[status]}
                </MenuItem>
              ))
            )}
          </TextField>
          <Button
            disabled={!targetStatus || statusMutation.isPending}
            onClick={() => void changeStatus()}
            startIcon={<SaveOutlinedIcon />}
            variant="contained"
          >
            Status guncelle
          </Button>
          <Divider />
          <Button
            disabled={assignMutation.isPending}
            onClick={() => void assignToMe()}
            startIcon={<PersonAddAltOutlinedIcon />}
            variant="outlined"
          >
            Bana ata
          </Button>
          <TextField
            disabled={teamsQuery.isLoading}
            label="Ekip"
            onChange={(event) => {
              setAssignedTeamId(event.target.value);
              setAssigneeId("");
            }}
            SelectProps={{ native: true }}
            select
            size="small"
            value={assignedTeamId}
            variant="standard"
          >
            <option value="">{teamsQuery.isLoading ? "Ekipler yukleniyor" : "Ekip sec"}</option>
            {assignedTeamId && !selectedTeamInTeams && (
              <option value={assignedTeamId}>{assignedTeamId}</option>
            )}
            {teams.map((team) => (
              <option key={team.id} value={team.id}>
                {team.name} / {team.departmentCode} / {team.code}
              </option>
            ))}
          </TextField>
          <TextField
            disabled={!assignedTeamId || teamMembersQuery.isLoading}
            label="Agent"
            onChange={(event) => setAssigneeId(event.target.value)}
            SelectProps={{ native: true }}
            select
            size="small"
            value={assigneeId}
            variant="standard"
          >
            <option value="">
              {!assignedTeamId
                ? "Once ekip sec"
                : teamMembersQuery.isLoading
                  ? "Agentlar yukleniyor"
                  : "Agent sec"}
            </option>
            {assigneeId && !selectedAssigneeInMembers && (
              <option value={assigneeId}>{assigneeId}</option>
            )}
            {teamMembers.map((member) => (
              <option key={member.actorId} value={member.actorId}>
                {member.actorId}
                {member.teamLead ? " / Lead" : ""}
              </option>
            ))}
          </TextField>
          <Button disabled={assignMutation.isPending} onClick={() => void submitAssignment()} variant="outlined">
            Atamayi kaydet
          </Button>
        </Stack>
      </Paper>

      <Paper variant="outlined" sx={{ p: 2 }}>
        <Stack component="form" onSubmit={handleSubmit((values) => void submitWorklog(values))} spacing={2}>
          <Typography variant="h6">Worklog</Typography>
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
          />
          <TextField
            error={Boolean(errors.durationMinutes)}
            helperText={errors.durationMinutes?.message}
            label="Sure"
            size="small"
            type="number"
            {...register("durationMinutes")}
          />
          <TextField
            error={Boolean(errors.description)}
            helperText={errors.description?.message}
            label="Aciklama"
            minRows={3}
            multiline
            size="small"
            {...register("description")}
          />
          <Button disabled={addWorklog.isPending} startIcon={<TimerOutlinedIcon />} type="submit" variant="outlined">
            Worklog ekle
          </Button>
        </Stack>
        <Divider sx={{ my: 2 }} />
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
      </Paper>

      <Paper variant="outlined" sx={{ p: 2 }}>
        <Stack spacing={1.5}>
          <Typography variant="h6">Dosyalar</Typography>
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
      </Paper>
    </Stack>
  );
}
