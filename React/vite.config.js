import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {

            '/web': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            },

            // '/web/building': {
            //     target: 'http://localhost:8080',
            //     changeOrigin: true,
            //     secure: false,
            // },

            // '/web/dashboard': {  // ✅ 추가
            //     target: 'http://localhost:8080',
            //     changeOrigin: true,
            //     secure: false,
            // },

            // '/web/api': {
            //     target: 'http://localhost:8080',
            //     changeOrigin: true,
            //     secure: false,
            // }

        }
    }
})
