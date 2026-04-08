import { request } from '@/api/http'

export type StudentSeckillActivityResponse = {
  id: number
  storeId: number
  skuId: number
  activityName: string
  seckillPrice: number
  stock: number
  totalStock?: number
  startTime: string
  endTime: string
  status: number
  createdAt?: string
  updatedAt?: string
}

export type StudentSeckillApplyRequest = {
  activityId: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark?: string
}

export type StudentSeckillApplyResponse = {
  requestId: string
  status: string
  message: string
}

export type StudentSeckillResultResponse = {
  requestId: string
  status: string
  message: string
  orderId?: number
  orderNo?: number
}

export function fetchStudentSeckillActivities(storeId: number) {
  return request<StudentSeckillActivityResponse[]>(`/api/student/seckill/activity/list?storeId=${storeId}`, {
    auth: true,
  })
}

export function applyStudentSeckill(payload: StudentSeckillApplyRequest) {
  return request<StudentSeckillApplyResponse>('/api/student/seckill/apply', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function fetchStudentSeckillResult(requestId: string) {
  return request<StudentSeckillResultResponse>(`/api/student/seckill/result/${encodeURIComponent(requestId)}`, {
    auth: true,
  })
}
