import { useCallback, useEffect, useMemo, useState } from "react";
import * as DocumentPicker from "expo-document-picker";
import { ScrollView, StyleSheet, Text, View } from "react-native";
import type { TicketPriority, ProductResponse, TicketTopicResponse, MobilePickedFile } from "../../api/mobileApiTypes";
import { uploadTicketAttachment } from "../../api/fileApi";
import { createCustomerTicket, listProducts, listTicketTopics } from "../../api/ticketApi";
import { Chip, EmptyState, ErrorState, PrimaryButton, SecondaryButton, UnderlineInput } from "../../components/MobilePrimitives";
import { colors, spacing, typography } from "../../theme/tokens";
import { priorityLabel } from "../../utils/formatters";

const priorities: TicketPriority[] = ["LOW", "MEDIUM", "HIGH"];
const maxAttachmentSizeBytes = 10_485_760;

export function CustomerCreateTicketScreen({ onCreated }: { onCreated: (ticketId: string) => void }) {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [topics, setTopics] = useState<TicketTopicResponse[]>([]);
  const [summary, setSummary] = useState("");
  const [description, setDescription] = useState("");
  const [productId, setProductId] = useState("");
  const [topicCode, setTopicCode] = useState("");
  const [priority, setPriority] = useState<TicketPriority>("MEDIUM");
  const [attachment, setAttachment] = useState<MobilePickedFile | undefined>();
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | undefined>();

  const loadCatalogs = useCallback(async () => {
    setLoading(true);
    setError(undefined);

    try {
      const [nextProducts, nextTopics] = await Promise.all([listProducts(), listTicketTopics()]);
      setProducts(nextProducts);
      setTopics(nextTopics);
      setProductId((current) => current || nextProducts[0]?.id || "");
      setTopicCode((current) => current || nextTopics[0]?.code || "");
    } catch {
      setError("Talep kataloglari yuklenemedi.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadCatalogs();
  }, [loadCatalogs]);

  const formValid = useMemo(
    () => summary.trim().length >= 3 && description.trim().length >= 10 && productId && topicCode,
    [description, productId, summary, topicCode]
  );

  async function submitTicket() {
    if (!formValid) {
      setError("Konu, kategori, urun ve aciklama alanlari tamamlanmali.");
      return;
    }

    setSubmitting(true);
    setError(undefined);

    try {
      const ticket = await createCustomerTicket({
        description: description.trim(),
        priority,
        productId,
        summary: summary.trim(),
        topicCode
      });

      if (attachment) {
        try {
          await uploadTicketAttachment(ticket.id, attachment);
        } catch {
          setError("Talep olustu fakat dosya yuklenemedi.");
          return;
        }
      }

      setSummary("");
      setDescription("");
      setPriority("MEDIUM");
      setAttachment(undefined);
      onCreated(ticket.id);
    } catch {
      setError("Talep olusturulamadi.");
    } finally {
      setSubmitting(false);
    }
  }

  async function pickAttachment() {
    const result = await DocumentPicker.getDocumentAsync({
      copyToCacheDirectory: true,
      multiple: false,
      type: ["text/plain", "image/png", "image/jpeg", "application/pdf"]
    });

    if (result.canceled || result.assets.length === 0) {
      return;
    }

    const asset = result.assets[0];

    if (!asset.size || asset.size < 1) {
      setError("Secilen dosya boyutu okunamadi.");
      return;
    }

    if (asset.size > maxAttachmentSizeBytes) {
      setError("Dosya boyutu 10MB sinirini asiyor.");
      return;
    }

    setAttachment({
      mimeType: asset.mimeType,
      name: asset.name,
      size: asset.size,
      uri: asset.uri
    });
  }

  if (loading) {
    return <EmptyState message="Form yukleniyor." />;
  }

  return (
    <ScrollView contentContainerStyle={styles.container}>
      {error ? <ErrorState message={error} onRetry={loadCatalogs} /> : undefined}

      <UnderlineInput
        label="Konu"
        onChangeText={setSummary}
        placeholder="Talep konusunu giriniz"
        value={summary}
      />

      <View style={styles.fieldGroup}>
        <Text style={styles.label}>Kategori</Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          <View style={styles.chipRow}>
            {topics.map((topic) => (
              <Chip
                active={topicCode === topic.code}
                key={topic.id}
                label={topic.name}
                onPress={() => setTopicCode(topic.code)}
              />
            ))}
          </View>
        </ScrollView>
      </View>

      <View style={styles.fieldGroup}>
        <Text style={styles.label}>Urun</Text>
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          <View style={styles.chipRow}>
            {products.map((product) => (
              <Chip
                active={productId === product.id}
                key={product.id}
                label={product.name}
                onPress={() => setProductId(product.id)}
              />
            ))}
          </View>
        </ScrollView>
      </View>

      <View style={styles.fieldGroup}>
        <Text style={styles.label}>Oncelik</Text>
        <View style={styles.chipRow}>
          {priorities.map((item) => (
            <Chip
              active={priority === item}
              danger={item === "HIGH"}
              key={item}
              label={priorityLabel(item)}
              onPress={() => setPriority(item)}
            />
          ))}
        </View>
      </View>

      <UnderlineInput
        label="Aciklama"
        multiline
        onChangeText={setDescription}
        placeholder="Detayli bilgi veriniz..."
        value={description}
      />

      <SecondaryButton
        label={attachment ? attachment.name : "Dosya ekle"}
        onPress={() => void pickAttachment()}
      />
      <PrimaryButton disabled={submitting} label={submitting ? "Gonderiliyor" : "Gonder"} onPress={submitTicket} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  chipRow: {
    flexDirection: "row",
    gap: spacing.sm
  },
  container: {
    gap: spacing.lg,
    padding: spacing.md,
    paddingBottom: spacing.xl
  },
  fieldGroup: {
    gap: spacing.sm
  },
  label: {
    ...typography.label,
    color: colors.textMuted,
    textTransform: "uppercase"
  }
});
