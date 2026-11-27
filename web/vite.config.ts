import { reactRouter } from "@react-router/dev/vite";
import tailwindcss from "@tailwindcss/vite";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [tailwindcss(), reactRouter()],
  resolve: {
    tsconfigPaths: true,
  },
  server: {
    // Forward API calls to the Spring Boot app (`./gradlew bootRun` in api/).
    // The path is kept as-is (the API's routes already start with /api).
    // `vite preview` (npm start) reuses this proxy config.
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        // Pass the browser's Host through so the API builds its HAL links
        // against this dev server (e.g. localhost:5173) rather than :8080;
        // following links then stays same-origin, through the proxy.
        configure: (proxy) => {
          proxy.on("proxyReq", (proxyReq, req) => {
            if (req.headers.host) {
              proxyReq.setHeader("host", req.headers.host);
            }
          });
        },
      },
    },
  },
});
