import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
//import vueDevTools from 'vite-plugin-vue-devtools'

// https://vitejs.dev/config/
export default defineConfig({
	base : '/memo/', // /memo directory standard setting
  plugins: [
    vue()
    //vueDevTools()
  ],

  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: true, //0.0.0.0にバインディング → 他のブラウザーやクラウト環境からでも接続可能。
    port: 5173,
    strictPort: true
  }
})
