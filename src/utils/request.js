import axios from 'axios'
import { decodeStoredJson } from '@/utils/storageCodec.js'

function readStoredToken() {
  const raw = localStorage.getItem('token')
  if (!raw) return null
  try {
    return decodeStoredJson(raw)
  } catch {
    return null
  }
}

function bearerFromAuthorizationHeader(value) {
  if (value == null || typeof value !== 'string') return null
  const m = value.match(/^Bearer\s+(\S+)/i)
  return m ? m[1].trim() : null
}

/** 从 axios config 读取本次请求携带的 Bearer（兼容 AxiosHeaders / 普通对象） */
function getSentBearerToken(config) {
  if (!config?.headers) return null
  const h = config.headers
  let raw =
    (typeof h.get === 'function' && h.get('Authorization')) || h.Authorization || h.authorization
  if (Array.isArray(raw)) raw = raw[0]
  return bearerFromAuthorizationHeader(raw)
}

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = readStoredToken()
  // 供 401 时判断是否为「旧 token 晚到」，不依赖从 headers 反解析（multipart 等场景更稳）
  config.__authTokenSnapshot = token || null

  if (token) {
    const h = config.headers
    if (h && typeof h.set === 'function') {
      h.set('Authorization', `Bearer ${token}`)
    } else {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
  }

  // FormData 必须由浏览器自动带 multipart boundary；若保留 application/json 会导致后端收不到文件并易出现异常
  if (config.data instanceof FormData) {
    const h = config.headers
    if (h && typeof h.delete === 'function') {
      h.delete('Content-Type')
      h.delete('content-type')
    } else if (config.headers) {
      delete config.headers['Content-Type']
      delete config.headers['content-type']
    }
  }

  return config
})

request.interceptors.response.use(
  (response) => {
    const p = response.data
    if (p && typeof p.code === 'number' && p.code !== 0) {
      return Promise.reject(p.message || '请求失败')
    }
    return p?.data
  },
  (error) => {
    const msg = error.response?.data?.message || error.message || '网络错误'

    if (error.response?.status === 401) {
      if (error.config?.skipAuthInvalid) {
        return Promise.reject(msg)
      }
      // 避免启动时旧的 /auth/me 晚于新登录返回 401，误清掉刚写入的会话
      const sent = error.config?.__authTokenSnapshot ?? getSentBearerToken(error.config)
      const current = readStoredToken()
      if (sent && current && sent !== current) {
        return Promise.reject(msg)
      }
      try {
        window.dispatchEvent(new Event('auth-invalid'))
      } catch {}
    }
    return Promise.reject(msg)
  }
)

export default request
