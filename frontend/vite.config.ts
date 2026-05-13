import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 配置：开发服务器 + 路径别名 + 代理
//   1. server.port=5173 是 Vite 的默认开发端口
//   2. resolve.alias 让我们可以用 "@/..." 代替 "src/..."
//   3. server.proxy 把 /api 的请求转发到本地后端，避免开发时跨域问题
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
