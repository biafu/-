import { defineStore } from 'pinia'
import type { LoginResponse, UserRole } from '@/api/auth'

const AUTH_STORAGE_KEY = 'cc-auth-session'

export type AuthSession = {
  token: string
  userId: number
  merchantId?: number
  username: string
  role: UserRole
}

function parseSession(value: string | null): AuthSession | null {
  if (!value) {
    return null
  }
  try {
    return JSON.parse(value) as AuthSession
  } catch {
    return null
  }
}

export function loadSessionFromStorage() {
  return parseSession(localStorage.getItem(AUTH_STORAGE_KEY))
}

function saveSession(session: AuthSession | null) {
  if (!session) {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    return
  }
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session))
}

export const useAuthStore = defineStore('auth', {
  state: () => {
    const session = loadSessionFromStorage()
    return {
      token: session?.token ?? '',
      userId: session?.userId ?? 0,
      merchantId: session?.merchantId,
      username: session?.username ?? '',
      role: session?.role as UserRole | undefined,
    }
  },
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
  },
  actions: {
    setSession(login: LoginResponse) {
      this.token = login.token
      this.userId = login.userId
      this.merchantId = login.merchantId
      this.username = login.username
      this.role = login.role

      saveSession({
        token: this.token,
        userId: this.userId,
        merchantId: this.merchantId,
        username: this.username,
        role: login.role,
      })
    },
    logout() {
      this.token = ''
      this.userId = 0
      this.merchantId = undefined
      this.username = ''
      this.role = undefined
      saveSession(null)
    },
  },
})
