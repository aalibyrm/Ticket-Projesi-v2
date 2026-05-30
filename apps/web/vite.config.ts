import { reactRouter } from "@react-router/dev/vite";
import react from "@vitejs/plugin-react";
import { fileURLToPath } from "node:url";
import { defineConfig } from "vitest/config";

const appPath = fileURLToPath(new URL("./app", import.meta.url));

export default defineConfig(({ mode }) => ({
  plugins: [mode === "test" ? react() : reactRouter()],
  resolve: {
    alias: {
      "~": appPath,
    },
  },
  test: {
    css: true,
    environment: "jsdom",
    globals: true,
    setupFiles: ["./app/test/setup.ts"],
  },
}));
