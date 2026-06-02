import { z } from "zod";

const canonicalUuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function backendUuidSchema(message = "Gecerli UUID degeri secmelisin.") {
  return z.string().regex(canonicalUuidPattern, message);
}
