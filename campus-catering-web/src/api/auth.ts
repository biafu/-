import { request } from '@/api/http'

export type UserRole = 'STUDENT' | 'MERCHANT' | 'ADMIN' | 'DELIVERY'

export type LoginPayload = {
  username: string
  password: string
}

export type LoginResponse = {
  userId: number
  merchantId?: number
  username: string
  role: UserRole
  token: string
}

export type CurrentUserResponse = {
  userId: number
  merchantId?: number
  username: string
  role: UserRole
}

export async function loginByRole(role: 'STUDENT' | 'MERCHANT' | 'ADMIN' | 'DELIVERY', payload: LoginPayload) {
  const pathMap = {
    STUDENT: '/api/auth/student/login',
    MERCHANT: '/api/auth/merchant/login',
    ADMIN: '/api/auth/admin/login',
    DELIVERY: '/api/auth/delivery/login',
  } as const

  return request<LoginResponse>(pathMap[role], {
    method: 'POST',
    body: payload,
  })
}

export function fetchCurrentUser() {
  return request<CurrentUserResponse>('/api/auth/me', { auth: true })
}
