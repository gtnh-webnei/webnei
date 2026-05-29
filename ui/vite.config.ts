import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'node:path'
import fs from 'node:fs'

function assetsMiddleware(): Plugin {
  return {
    name: 'webnei-assets-middleware',
    configureServer(server) {
      const dir = process.env.WEBNEI_ASSETS_DIR
      if (!dir) {
        server.config.logger.info(
          '[webnei] WEBNEI_ASSETS_DIR not set — /assets/** will 404 in dev',
        )
        return
      }
      const root = path.resolve(dir)
      if (!fs.existsSync(root)) {
        server.config.logger.warn(`[webnei] WEBNEI_ASSETS_DIR=${root} does not exist`)
        return
      }
      server.config.logger.info(`[webnei] serving /assets/** from ${root}`)
      const mimeMap: Record<string, string> = {
        '.png': 'image/png',
        '.jpg': 'image/jpeg',
        '.jpeg': 'image/jpeg',
        '.gif': 'image/gif',
        '.webp': 'image/webp',
        '.svg': 'image/svg+xml',
        '.json': 'application/json; charset=utf-8',
        '.lang': 'text/plain; charset=utf-8',
      }
      server.middlewares.use('/assets', (req, res, next) => {
        if (req.method !== 'GET' && req.method !== 'HEAD') {
          next()
          return
        }
        const url = (req.url ?? '/').split('?')[0]
        const decoded = decodeURIComponent(url)
        const resolved = path.resolve(root, '.' + decoded)
        if (!resolved.startsWith(root + path.sep) && resolved !== root) {
          res.statusCode = 403
          res.end('forbidden')
          return
        }
        fs.stat(resolved, (err, stat) => {
          if (err || !stat.isFile()) {
            next()
            return
          }
          const ext = path.extname(resolved).toLowerCase()
          res.setHeader('Content-Type', mimeMap[ext] ?? 'application/octet-stream')
          res.setHeader('Content-Length', String(stat.size))
          res.setHeader('Cache-Control', 'public, max-age=31536000, immutable')
          if (req.method === 'HEAD') {
            res.end()
            return
          }
          fs.createReadStream(resolved).pipe(res)
        })
      })
    },
  }
}

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
    assetsMiddleware(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  build: {
    assetsDir: 'static',
  },
  server: {
    host: '127.0.0.1',
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
