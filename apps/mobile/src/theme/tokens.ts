export const colors = {
  background: "#fcfcfc",
  surface: "#ffffff",
  surfaceMuted: "#f4f4f5",
  border: "#e5e5ea",
  text: "#111111",
  textMuted: "#737373",
  primary: "#aa1101",
  primaryDark: "#7f0900",
  error: "#ba1a1a",
  success: "#246b3f"
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32
} as const;

export const radius = {
  sm: 4,
  md: 8,
  lg: 16,
  full: 999
} as const;

export const typography = {
  title: {
    fontSize: 24,
    fontWeight: "600" as const,
    lineHeight: 30,
    letterSpacing: 0
  },
  heading: {
    fontSize: 20,
    fontWeight: "600" as const,
    lineHeight: 26,
    letterSpacing: 0
  },
  body: {
    fontSize: 15,
    fontWeight: "400" as const,
    lineHeight: 23,
    letterSpacing: 0
  },
  label: {
    fontSize: 13,
    fontWeight: "500" as const,
    lineHeight: 18,
    letterSpacing: 0
  }
} as const;
