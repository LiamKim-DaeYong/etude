import { fileURLToPath, URL } from 'node:url';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig } from 'vite';
import {
  securityHeaders,
  staticHeadersFile,
} from '../../packages/config/security.js';

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    {
      name: 'static-host-security-headers',
      generateBundle() {
        this.emitFile({
          type: 'asset',
          fileName: '_headers',
          source: staticHeadersFile(),
        });
      },
    },
  ],
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  server: { host: '127.0.0.1', port: 5173, strictPort: true },
  preview: {
    host: '127.0.0.1',
    port: 4173,
    strictPort: true,
    headers: securityHeaders,
  },
  build: { sourcemap: false },
});
