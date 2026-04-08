import { loadSessionFromStorage } from '@/stores/auth'

export type ApiResponse<T> = {
  code: number
  message: string
  data: T
}

export class ApiError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.code = code
  }
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

type RequestOptions = {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
  auth?: boolean
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', body, auth = false } = options

  const headers: Record<string, string> = {
    Accept: 'application/json',
  }

  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  if (auth) {
    const session = loadSessionFromStorage()
    if (session?.token) {
      headers.Authorization = `Bearer ${session.token}`
    }
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  let payload: ApiResponse<T> | null = null
  try {
    payload = (await response.json()) as ApiResponse<T>
  } catch {
    if (!response.ok) {
      throw new ApiError(response.status, `请求失败(${response.status})`)
    }
    throw new ApiError(5000, '服务返回数据格式异常')
  }

  if (!response.ok) {
    throw new ApiError(payload.code || response.status, payload.message || `请求失败(${response.status})`)
  }

  if (payload.code !== 0) {
    throw new ApiError(payload.code, payload.message || '请求失败')
  }

  return payload.data
}
