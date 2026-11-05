import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// In development the Vue server (5173) passes API calls to Spring Boot (8080).
export default defineConfig({
  plugins: [vue()],
  server: { proxy: { '/api': 'http://localhost:8080' } },
})
