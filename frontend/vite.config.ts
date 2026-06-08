import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

const BACKEND_PATHS = [
  '/api',
  '/login',
  '/logout',
  '/upload-asta',
  '/images',
  '/seon.owl',
]

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const base = env.VITE_BASE_URL ?? '/'
  const target = env.VITE_BACKEND_URL ?? 'http://localhost:8080'

  const proxy = Object.fromEntries(
    BACKEND_PATHS.map((p) => [p, { target, changeOrigin: true }]),
  )

  return {
    base,
    plugins: [react()],
    server: { port: 5173, proxy },
    build: { outDir: 'dist' },
  }
})
