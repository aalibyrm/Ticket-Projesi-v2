import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";
import AttachFileOutlinedIcon from "@mui/icons-material/AttachFileOutlined";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Alert,
  Box,
  Button,
  FormControl,
  FormControlLabel,
  FormHelperText,
  FormLabel,
  MenuItem,
  Paper,
  Radio,
  RadioGroup,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { Controller, useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { z } from "zod";
import { CustomerErrorState, CustomerLoadingState } from "~/features/customer/components/CustomerState";
import {
  useCreateCustomerTicket,
  useProducts,
  useUploadTicketAttachment,
} from "~/features/customer/customerQueries";
import type { CreateTicketFormValues } from "~/features/customer/customerTypes";

const maxAttachmentBytes = 10 * 1024 * 1024;

const createTicketSchema = z.object({
  attachment: z
    .custom<FileList>()
    .optional()
    .refine((files) => !files || files.length <= 1, "Tek seferde bir dosya eklenebilir.")
    .refine((files) => {
      const file = files?.item(0);
      return !file || file.size <= maxAttachmentBytes;
    }, "Dosya 10 MB'tan kucuk olmali."),
  description: z.string().trim().min(20, "Aciklama en az 20 karakter olmali.").max(5000),
  priority: z.enum(["LOW", "MEDIUM", "HIGH"]),
  productId: z.string().uuid("Kategori secmelisin."),
  summary: z.string().trim().min(5, "Konu en az 5 karakter olmali.").max(180),
});

export function CustomerCreateTicketPage() {
  const navigate = useNavigate();
  const productsQuery = useProducts();
  const createTicket = useCreateCustomerTicket();
  const uploadAttachment = useUploadTicketAttachment();

  const {
    control,
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
    watch,
  } = useForm<CreateTicketFormValues>({
    defaultValues: {
      description: "",
      priority: "MEDIUM",
      productId: "",
      summary: "",
    },
    resolver: zodResolver(createTicketSchema),
  });

  const selectedFile = watch("attachment")?.item(0);

  async function submitForm(values: CreateTicketFormValues) {
    const ticket = await createTicket.mutateAsync({
      description: values.description.trim(),
      priority: values.priority,
      productId: values.productId,
      summary: values.summary.trim(),
    });

    const file = values.attachment?.item(0);
    if (file) {
      await uploadAttachment.mutateAsync({ file, ticketId: ticket.id });
    }

    navigate(`/tickets/${ticket.id}`);
  }

  if (productsQuery.isLoading) {
    return <CustomerLoadingState label="Form verileri yukleniyor" />;
  }

  if (productsQuery.isError) {
    return <CustomerErrorState message="Kategori listesi alinamadi." onRetry={() => void productsQuery.refetch()} />;
  }

  const isBusy = isSubmitting || createTicket.isPending || uploadAttachment.isPending;

  return (
    <Stack spacing={3}>
      <Button
        color="inherit"
        onClick={() => navigate("/tickets")}
        startIcon={<ArrowBackOutlinedIcon />}
        sx={{ alignSelf: "flex-start" }}
      >
        Geri don
      </Button>
      <Paper sx={{ mx: "auto", p: 4, width: "min(100%, 760px)" }}>
        <Stack spacing={3}>
          <Stack spacing={0.75} sx={{ borderBottom: "1px solid", borderColor: "divider", pb: 2 }}>
            <Typography variant="h4">Yeni Destek Talebi</Typography>
            <Typography color="text.secondary">Sorununu detaylica acikla ve gerekiyorsa dosya ekle.</Typography>
          </Stack>

          {(createTicket.isError || uploadAttachment.isError) && (
            <Alert severity="error" variant="outlined">
              Talep kaydedilirken hata olustu. Bilgileri kontrol edip tekrar dene.
            </Alert>
          )}

          <Stack
            component="form"
            noValidate
            onSubmit={(event) => void handleSubmit(submitForm)(event)}
            spacing={3}
          >
            <Controller
              control={control}
              name="summary"
              render={({ field }) => (
                <TextField
                  {...field}
                  error={Boolean(errors.summary)}
                  helperText={errors.summary?.message}
                  label="Konu"
                  placeholder="Kisa bir ozet"
                  variant="standard"
                />
              )}
            />
            <Controller
              control={control}
              name="productId"
              render={({ field }) => (
                <TextField
                  {...field}
                  error={Boolean(errors.productId)}
                  helperText={errors.productId?.message}
                  label="Kategori"
                  select
                  variant="standard"
                >
                  {(productsQuery.data ?? []).map((product) => (
                    <MenuItem key={product.id} value={product.id}>
                      {product.name}
                    </MenuItem>
                  ))}
                </TextField>
              )}
            />
            <Controller
              control={control}
              name="priority"
              render={({ field }) => (
                <FormControl error={Boolean(errors.priority)}>
                  <FormLabel>Oncelik</FormLabel>
                  <RadioGroup row {...field}>
                    <FormControlLabel control={<Radio />} label="Dusuk" value="LOW" />
                    <FormControlLabel control={<Radio />} label="Normal" value="MEDIUM" />
                    <FormControlLabel control={<Radio />} label="Yuksek" value="HIGH" />
                  </RadioGroup>
                  {errors.priority?.message && <FormHelperText>{errors.priority.message}</FormHelperText>}
                </FormControl>
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
                  placeholder="Karsilastigin sorunu adimlariyla belirt..."
                  variant="standard"
                />
              )}
            />
            <Box>
              <Button component="label" startIcon={<AttachFileOutlinedIcon />} variant="outlined">
                Dosya ekle
                <input hidden type="file" {...register("attachment")} />
              </Button>
              <Typography color={errors.attachment ? "error" : "text.secondary"} sx={{ mt: 1 }} variant="body2">
                {errors.attachment?.message ?? selectedFile?.name ?? "Opsiyonel, maksimum 10 MB."}
              </Typography>
            </Box>
            <Button disabled={isBusy} sx={{ alignSelf: "flex-end", minWidth: 160 }} type="submit" variant="contained">
              {isBusy ? "Gonderiliyor" : "Gonder"}
            </Button>
          </Stack>
        </Stack>
      </Paper>
    </Stack>
  );
}
