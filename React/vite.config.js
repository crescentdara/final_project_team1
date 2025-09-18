import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/web/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            },
            '/web/building': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            },
            '/web/dashboard': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            },
            '/web/report': {   // ✅ 추가
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            }
        }
    }
})
