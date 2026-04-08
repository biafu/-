import { request } from '@/api/http'

export type AdminDashboardResponse = {
  orderCount: number
  totalRevenue: number
  merchantCount: number
  activeUserCount: number
}

export type MerchantApplicationResponse = {
  id: number
  merchantName: string
  contactName: string
  contactPhone: string
  campusCode: string
  licenseNo: string
  status: number
  auditRemark?: string
  createdAt: string
  updatedAt: string
}

export type MerchantAuditRequest = {
  applyId: number
  approved: boolean
  auditRemark?: string
}

export type DailyStatisticsResponse = {
  statDate: string
  orderCount: number
  completedOrderCount: number
  cancelledOrderCount: number
  gmv: number
  activeUserCount: number
  activeMerchantCount: number
  cancelRate: number
  avgFulfillmentMinutes: number
}

export type MerchantRankResponse = {
  merchantId: number
  merchantName: string
  orderCount: number
  gmv: number
}

export type SeckillActivitySaveRequest = {
  id?: number
  storeId: number
  skuId: number
  activityName: string
  seckillPrice: number
  stock: number
  startTime: string
  endTime: string
  status: number
}

export type SeckillActivityResponse = {
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

export type AdminDispatchRequest = {
  orderId: number
  deliveryUserId: number
  dispatchRemark?: string
}

export function fetchAdminDashboard() {
  return request<AdminDashboardResponse>('/api/admin/statistics/dashboard', { auth: true })
}

export function fetchMerchantApplications() {
  return request<MerchantApplicationResponse[]>('/api/admin/merchant/apply/list', { auth: true })
}

export function auditMerchantApplication(payload: MerchantAuditRequest) {
  return request<void>('/api/admin/merchant/apply/audit', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function fetchAdminDailyStatistics(days = 7) {
  return request<DailyStatisticsResponse[]>(`/api/admin/statistics/daily?days=${days}`, { auth: true })
}

export function rebuildAdminDailyStatistics(statDate: string) {
  return request<void>(`/api/admin/statistics/daily/rebuild?statDate=${encodeURIComponent(statDate)}`, {
    method: 'POST',
    auth: true,
  })
}

export function fetchAdminMerchantRank(days = 7, limit = 10) {
  return request<MerchantRankResponse[]>(
    `/api/admin/statistics/merchant-rank?days=${days}&limit=${limit}`,
    { auth: true },
  )
}

export function enableAdminStore(storeId: number) {
  return request<void>(`/api/admin/store/enable/${storeId}`, {
    method: 'POST',
    auth: true,
  })
}

export function disableAdminStore(storeId: number) {
  return request<void>(`/api/admin/store/disable/${storeId}`, {
    method: 'POST',
    auth: true,
  })
}

export function dispatchAdminDelivery(payload: AdminDispatchRequest) {
  return request<void>('/api/admin/delivery/dispatch', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function saveAdminSeckillActivity(payload: SeckillActivitySaveRequest) {
  return request<number>('/api/admin/seckill/activity/save', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function fetchAdminSeckillActivities() {
  return request<SeckillActivityResponse[]>('/api/admin/seckill/activity/list', { auth: true })
}

export function deleteAdminSeckillActivity(activityId: number) {
  return request<void>(`/api/admin/seckill/activity/${activityId}`, {
    method: 'DELETE',
    auth: true,
  })
}
