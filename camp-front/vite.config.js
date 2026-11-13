import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// Vite 配置文件
// 开发环境下将 /api 前缀的请求代理到后端 SpringBoot 服务
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    port: 5173,
    open: true,
    // 忽略编辑器/工具写入时产生的临时文件，避免文件监视器 EBUSY 崩溃
    watch: {
      ignored: ['**/.*.tmpdir/**', '**/*.tmp', '**/.*.tmp']
    },
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
