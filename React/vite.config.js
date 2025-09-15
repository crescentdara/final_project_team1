import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/land-survey': {
                target: 'http://localhost:8080', // Spring Boot 서버 주소
                changeOrigin: true,
            },
        },
    },
})
