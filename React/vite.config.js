import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
<<<<<<< HEAD
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

            '/web/dashboard': {  // ✅ 추가
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false,
            }

        }
    }
})
=======
  plugins: [react()],
  server: {
    proxy: {
      '/web/api': {
        target: 'http://localhost:8080', // 스프링 서버
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
>>>>>>> origin/web/his/TotalSurveyList
