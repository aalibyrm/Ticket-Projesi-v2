import { zodResolver } from "@hookform/resolvers/zod";
import {
  Button,
  MenuItem,
  Paper,
  Stack,
  TextField,
} from "@mui/material";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";

const createTicketSchema = z.object({
  description: z.string().min(20, "Aciklama en az 20 karakter olmali."),
  priority: z.enum(["LOW", "MEDIUM", "HIGH"]),
  subject: z.string().min(5, "Konu en az 5 karakter olmali.").max(120),
});

type CreateTicketFormValues = z.infer<typeof createTicketSchema>;

export function NewTicketForm() {
  const {
    control,
    formState: { errors, isSubmitting },
    handleSubmit,
  } = useForm<CreateTicketFormValues>({
    defaultValues: {
      description: "",
      priority: "MEDIUM",
      subject: "",
    },
    resolver: zodResolver(createTicketSchema),
  });

  function submitForm(values: CreateTicketFormValues) {
    void values;
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Stack
        component="form"
        noValidate
        onSubmit={(event) => void handleSubmit(submitForm)(event)}
        spacing={2}
      >
        <Controller
          control={control}
          name="subject"
          render={({ field }) => (
            <TextField
              {...field}
              error={Boolean(errors.subject)}
              helperText={errors.subject?.message}
              label="Konu"
            />
          )}
        />
        <Controller
          control={control}
          name="priority"
          render={({ field }) => (
            <TextField {...field} label="Oncelik" select>
              <MenuItem value="LOW">Dusuk</MenuItem>
              <MenuItem value="MEDIUM">Orta</MenuItem>
              <MenuItem value="HIGH">Yuksek</MenuItem>
            </TextField>
          )}
        />
        <Controller
          control={control}
          name="description"
          render={({ field }) => (
            <TextField
              {...field}
              error={Boolean(errors.description)}
              helperText={errors.description?.message}
              label="Aciklama"
              minRows={6}
              multiline
            />
          )}
        />
        <Button disabled={isSubmitting} type="submit" variant="contained">
          Talebi hazirla
        </Button>
      </Stack>
    </Paper>
  );
}
