import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  // caminhos relativos: o Electron empacotado carrega dist/index.html via file://
  base: './',
  server: {
    port: 5173,
    strictPort: true
  }
})
