import { defineConfig, loadEnv, type ProxyOptions } from 'vite'
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

  const proxy: Record<string, ProxyOptions> = Object.fromEntries(
    BACKEND_PATHS.map((p) => [p, { target, changeOrigin: true }]),
  )

  // `/login` is both a Spring Security endpoint (the form POST) and an SPA route. In
  // production SpaController forwards the GET to index.html; do the same here, otherwise
  // opening http://localhost:5173/login directly returns the backend response instead of
  // the login page.
  proxy['/login'] = {
    target,
    changeOrigin: true,
    bypass: (req) => (req.method === 'GET' ? '/index.html' : undefined),
  }

  return {
    base,
    plugins: [react()],
    server: { port: 5173, proxy },
    build: { outDir: 'dist' },
  }
})
