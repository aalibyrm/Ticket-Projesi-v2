import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";
import AttachFileOutlinedIcon from "@mui/icons-material/AttachFileOutlined";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Alert,
  Box,
  Button,
  ButtonBase,
  FormControl,
  FormHelperText,
  FormLabel,
  Paper,
  Select,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { z } from "zod";
import { CustomerErrorState, CustomerLoadingState } from "~/features/customer/components/CustomerState";
import {
  useCreateCustomerTicket,
  useProducts,
  useTicketTopics,
  useUploadTicketAttachment,
} from "~/features/customer/customerQueries";
import type { CreateTicketFormValues } from "~/features/customer/customerTypes";
import { tmTokens } from "~/shared/theme/tmTokens";
import { backendUuidSchema } from "~/shared/validation/uuid";

const maxAttachmentBytes = 10 * 1024 * 1024;
const fieldLabelSx = {
  ...tmTokens.typography.labelSm,
  color: tmTokens.colors.secondary,
  mb: 1,
  textTransform: "uppercase",
} as const;
const selectSx = {
  "& .MuiNativeSelect-select": {
    minHeight: 32,
    py: 1,
  },
} as const;

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
  productId: backendUuidSchema("Kategori secmelisin."),
  summary: z.string().trim().min(5, "Konu en az 5 karakter olmali.").max(180),
  topicCode: z.string().trim().min(1, "Talep tipi secmelisin."),
});

export function CustomerCreateTicketPage() {
  const navigate = useNavigate();
  const [isFormHydrated, setFormHydrated] = useState(false);
  const productsQuery = useProducts();
  const topicsQuery = useTicketTopics();
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
      topicCode: "",
    },
    resolver: zodResolver(createTicketSchema),
  });

  const selectedFile = watch("attachment")?.item(0);
  const selectedTopic = (topicsQuery.data ?? []).find((topic) => topic.code === watch("topicCode"));

  useEffect(() => {
    setFormHydrated(true);
  }, []);

  async function submitForm(values: CreateTicketFormValues) {
    const ticket = await createTicket.mutateAsync({
      description: values.description.trim(),
      priority: values.priority,
      productId: values.productId,
      summary: values.summary.trim(),
      topicCode: values.topicCode,
    });

    const file = values.attachment?.item(0);
    if (file) {
      await uploadAttachment.mutateAsync({ file, ticketId: ticket.id });
    }

    const detailPath = `/tickets/${ticket.id}`;
    void navigate(detailPath);
    window.setTimeout(() => {
      if (window.location.pathname !== detailPath) {
        window.location.assign(detailPath);
      }
    }, 250);
  }

  if (productsQuery.isLoading || topicsQuery.isLoading) {
    return <CustomerLoadingState label="Form verileri yukleniyor" />;
  }

  if (productsQuery.isError || topicsQuery.isError) {
    return (
      <CustomerErrorState
        message="Form kataloglari alinamadi."
        onRetry={() => {
          void productsQuery.refetch();
          void topicsQuery.refetch();
        }}
      />
    );
  }

  const isBusy = isSubmitting || createTicket.isPending || uploadAttachment.isPending;

  return (
    <Box
      sx={{
        alignItems: { md: "center", xs: "stretch" },
        display: "flex",
        justifyContent: "center",
        minHeight: { md: `calc(100vh - ${tmTokens.layout.pageMargin * 2}px)`, xs: "auto" },
        width: "100%",
      }}
    >
      <Stack spacing={{ md: 1.5, xs: 2 }} sx={{ mx: "auto", width: "min(100%, 820px)" }}>
        <Button
          color="inherit"
          onClick={() => navigate("/tickets")}
          startIcon={<ArrowBackOutlinedIcon />}
          sx={{ alignSelf: "flex-start" }}
        >
          Geri don
        </Button>
        <Paper sx={{ p: { md: 4, xs: 3 }, width: "100%" }}>
          <Stack spacing={{ md: 2.25, xs: 3 }}>
            <Stack spacing={0.75} sx={{ borderBottom: "1px solid", borderColor: "divider", pb: { md: 1.75, xs: 2.5 } }}>
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
            data-e2e-ready={isFormHydrated ? "true" : "false"}
            data-testid="create-ticket-form"
            noValidate
            onSubmit={handleSubmit(submitForm)}
            spacing={{ md: 2, xs: 3 }}
          >
            <Controller
              control={control}
              name="summary"
              render={({ field }) => (
                <FormControl error={Boolean(errors.summary)} fullWidth>
                  <FormLabel htmlFor="ticket-summary" sx={fieldLabelSx}>
                    Konu
                  </FormLabel>
                  <TextField
                    {...field}
                    error={Boolean(errors.summary)}
                    hiddenLabel
                    id="ticket-summary"
                    placeholder="Kisa bir ozet"
                    variant="standard"
                  />
                  {errors.summary?.message && <FormHelperText>{errors.summary.message}</FormHelperText>}
                </FormControl>
              )}
            />
            <Controller
              control={control}
              name="productId"
              render={({ field }) => (
                <FormControl error={Boolean(errors.productId)} fullWidth variant="standard">
                  <FormLabel htmlFor="ticket-product" sx={fieldLabelSx}>
                    Kategori
                  </FormLabel>
                  <Select
                    {...field}
                    displayEmpty
                    inputProps={{ id: "ticket-product" }}
                    native
                    sx={selectSx}
                  >
                    <option value="">Kategori sec</option>
                    {(productsQuery.data ?? []).map((product) => (
                      <option key={product.id} value={product.id}>
                        {product.name}
                      </option>
                    ))}
                  </Select>
                  {errors.productId?.message && <FormHelperText>{errors.productId.message}</FormHelperText>}
                </FormControl>
              )}
            />
            <Controller
              control={control}
              name="topicCode"
              render={({ field }) => (
                <FormControl error={Boolean(errors.topicCode)} fullWidth variant="standard">
                  <FormLabel htmlFor="ticket-topic" sx={fieldLabelSx}>
                    Talep tipi
                  </FormLabel>
                  <Select
                    {...field}
                    displayEmpty
                    inputProps={{ id: "ticket-topic" }}
                    native
                    sx={selectSx}
                  >
                    <option value="">Talep tipi sec</option>
                    {(topicsQuery.data ?? []).map((topic) => (
                      <option key={topic.code} value={topic.code}>
                        {topic.name}
                      </option>
                    ))}
                  </Select>
                  {(errors.topicCode?.message ?? selectedTopic?.description) && (
                    <FormHelperText>{errors.topicCode?.message ?? selectedTopic?.description}</FormHelperText>
                  )}
                </FormControl>
              )}
            />
            <Controller
              control={control}
              name="priority"
              render={({ field }) => (
                <FormControl error={Boolean(errors.priority)}>
                  <FormLabel sx={fieldLabelSx}>Oncelik</FormLabel>
                  <Stack direction="row" flexWrap="wrap" gap={2} sx={{ mt: 1 }}>
                    {[
                      { label: "Dusuk", value: "LOW" },
                      { label: "Normal", value: "MEDIUM" },
                      { label: "Yuksek", value: "HIGH" },
                    ].map((option) => {
                      const isSelected = field.value === option.value;
                      const isHigh = option.value === "HIGH";
                      return (
                        <ButtonBase
                          aria-pressed={isSelected}
                          key={option.value}
                          onClick={() => field.onChange(option.value)}
                          sx={{
                            bgcolor: isSelected
                              ? isHigh
                                ? tmTokens.colors.primary
                                : tmTokens.colors.surfaceHigh
                              : tmTokens.colors.surfaceLow,
                            border: "1px solid",
                            borderColor: isHigh ? tmTokens.colors.primaryContainer : tmTokens.colors.border,
                            borderRadius: tmTokens.radius.md,
                            color: isSelected && isHigh ? "#ffffff" : isHigh ? tmTokens.colors.primaryContainer : tmTokens.colors.onSurface,
                            minHeight: 48,
                            minWidth: 112,
                            px: 3,
                            ...tmTokens.typography.bodyMdBold,
                            "&:focus-visible": {
                              outline: `2px solid ${tmTokens.colors.primaryContainer}`,
                              outlineOffset: 2,
                            },
                          }}
                        >
                          {option.label}
                        </ButtonBase>
                      );
                    })}
                  </Stack>
                  {errors.priority?.message && <FormHelperText>{errors.priority.message}</FormHelperText>}
                </FormControl>
              )}
            />
            <Controller
              control={control}
              name="description"
              render={({ field }) => (
                <FormControl error={Boolean(errors.description)} fullWidth>
                  <FormLabel htmlFor="ticket-description" sx={fieldLabelSx}>
                    Aciklama
                  </FormLabel>
                  <TextField
                    {...field}
                    error={Boolean(errors.description)}
                    hiddenLabel
                    id="ticket-description"
                    minRows={4}
                    multiline
                    placeholder="Karsilastigin sorunu adimlariyla belirt..."
                    variant="standard"
                  />
                  {errors.description?.message && <FormHelperText>{errors.description.message}</FormHelperText>}
                </FormControl>
              )}
            />
            <Stack alignItems={{ md: "flex-end", xs: "stretch" }} direction={{ md: "row", xs: "column" }} spacing={2}>
              <Box sx={{ flex: 1, minWidth: 0 }}>
                <Button
                  component="label"
                  startIcon={<AttachFileOutlinedIcon />}
                  sx={{
                    borderStyle: "dashed",
                    borderRadius: tmTokens.radius.md,
                    height: 52,
                    justifyContent: "center",
                    width: "100%",
                  }}
                  variant="outlined"
                >
                  Dosya ekle
                  <input hidden type="file" {...register("attachment")} />
                </Button>
                <Typography color={errors.attachment ? "error" : "text.secondary"} sx={{ mt: 0.75 }} variant="body2">
                  {errors.attachment?.message ?? selectedFile?.name ?? "Opsiyonel, maksimum 10 MB."}
                </Typography>
              </Box>
              <Button disabled={isBusy} sx={{ minHeight: 48, minWidth: { md: 200, xs: "100%" } }} type="submit" variant="contained">
                {isBusy ? "Gonderiliyor" : "Gonder"}
              </Button>
            </Stack>
          </Stack>
        </Stack>
        </Paper>
      </Stack>
    </Box>
  );
}
