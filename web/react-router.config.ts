import type { Config } from "@react-router/dev/config";

export default {
  // SPA mode: no server-side rendering. `react-router build` emits a static
  // build/client/ (index.html + assets); there is no Node server at runtime.
  ssr: false,
} satisfies Config;
