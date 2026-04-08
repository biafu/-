import { request } from '@/api/http'
import { ApiError } from '@/api/http'
import type { OrderDetailResponse, ProductViewResponse, StoreSimpleResponse } from '@/api/student'
import { loadSessionFromStorage } from '@/stores/auth'

export type MerchantOverviewResponse = {
  todayOrderCount: number
  totalOrderCount: number
  todayRevenue: number
}

export type MerchantApplyRequest = {
  merchantName: string
  contactName: string
  contactPhone: string
  campusCode: string
  licenseNo: string
}

export type StoreUpdateRequest = {
  storeName: string
  address: string
  deliveryType: number
  deliveryScopeDesc?: string
  deliveryRadiusKm: number
  minOrderAmount: number
  deliveryFee: number
  businessStatus: number
  notice?: string
}

export type StoreBusinessHourResponse = {
  id: number
  dayOfWeek: number
  startTime: string
  endTime: string
  status: number
}

export type StoreBusinessHourItemRequest = {
  dayOfWeek: number
  startTime: string
  endTime: string
  status: number
}

export type StoreBusinessHoursSaveRequest = {
  hours: StoreBusinessHourItemRequest[]
}

export type MerchantTrendPointResponse = {
  statDate: string
  orderCount: number
  revenue: number
  cancelRate: number
}

export type MerchantStatusDistributionResponse = {
  orderStatus: string
  orderCount: number
}

export type MerchantHotProductResponse = {
  spuName: string
  skuName: string
  soldQuantity: number
  gmv: number
}

export type MerchantCategoryResponse = {
  id: number
  categoryName: string
  sortNo: number
}

export type CategorySaveRequest = {
  id?: number
  categoryName: string
  sortNo?: number
}

export type ComboItemSaveRequest = {
  skuId: number
  quantity: number
  sortNo?: number
}

export type ProductSkuSaveRequest = {
  skuName: string
  price: number
  stock: number
}

export type ProductSaveRequest = {
  id?: number
  categoryId: number
  productName: string
  productType: number
  imageUrl?: string
  description?: string
  sortNo?: number
  comboDesc?: string
  skus: ProductSkuSaveRequest[]
  comboItems?: ComboItemSaveRequest[]
}

export type ComboUpdateRequest = {
  comboDesc?: string
  comboItems: ComboItemSaveRequest[]
}

export type ProductEditorUpdateRequest = {
  categoryId: number
  productName: string
  imageUrl?: string
  description?: string
  sortNo?: number
  skuId: number
  skuName: string
  price: number
  stock: number
  skuStatus: number
  comboDesc?: string
  comboItems?: ComboItemSaveRequest[]
}

export type ProductBasicUpdateRequest = {
  categoryId: number
  productName: string
  imageUrl?: string
  description?: string
  sortNo?: number
}

export type ProductSkuUpdateRequest = {
  skuName: string
  price: number
  status: number
}

export type SkuStockUpdateRequest = {
  stock: number
}

export type OrderExceptionResponse = {
  id: number
  orderId: number
  orderNo: number
  orderStatus: string
  payAmount: number
  reason: string
  status: string
  resolvedRemark?: string
  createdAt: string
  updatedAt: string
}

export type MerchantReviewResponse = {
  orderId: number
  orderNo: number
  storeId: number
  storeName: string
  rating: number
  content?: string
  isAnonymous: number
  reviewerName: string
  createdAt: string
}

export function submitMerchantApplication(payload: MerchantApplyRequest) {
  return request<number>('/api/merchant/apply', {
    method: 'POST',
    body: payload,
  })
}

export function fetchMerchantOverview() {
  return request<MerchantOverviewResponse>('/api/merchant/statistics/overview', { auth: true })
}

export function fetchMerchantOrders() {
  return request<OrderDetailResponse[]>('/api/merchant/order/list', { auth: true })
}

export function fetchMerchantExceptionOrders(status?: string) {
  const query = status ? `?status=${encodeURIComponent(status)}` : ''
  return request<OrderExceptionResponse[]>(`/api/merchant/order/exception/list${query}`, { auth: true })
}

export function fetchMerchantReviews() {
  return request<MerchantReviewResponse[]>('/api/merchant/review/list', { auth: true })
}

export function reportMerchantException(payload: { orderId: number; reason: string }) {
  return request<void>('/api/merchant/order/exception/report', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function resolveMerchantException(payload: { orderId: number; resolvedRemark: string }) {
  return request<void>('/api/merchant/order/exception/resolve', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function acceptOrder(orderId: number) {
  return request<void>(`/api/merchant/order/accept/${orderId}`, { method: 'POST', auth: true })
}

export function rejectOrder(orderId: number) {
  return request<void>(`/api/merchant/order/reject/${orderId}`, { method: 'POST', auth: true })
}

export function prepareOrder(orderId: number) {
  return request<void>(`/api/merchant/order/prepare/${orderId}`, { method: 'POST', auth: true })
}

export function finishOrder(orderId: number) {
  return request<void>(`/api/merchant/order/finish/${orderId}`, { method: 'POST', auth: true })
}

export function fetchMerchantStoreDetail() {
  return request<StoreSimpleResponse>('/api/merchant/store/detail', { auth: true })
}

export function updateMerchantStore(payload: StoreUpdateRequest) {
  return request<void>('/api/merchant/store/update', {
    method: 'PUT',
    body: payload,
    auth: true,
  })
}

export function fetchStoreBusinessHours() {
  return request<StoreBusinessHourResponse[]>('/api/merchant/store/business-hours', { auth: true })
}

export function saveStoreBusinessHours(payload: StoreBusinessHoursSaveRequest) {
  return request<void>('/api/merchant/store/business-hours', {
    method: 'PUT',
    body: payload,
    auth: true,
  })
}

export async function fetchMerchantProducts(): Promise<ProductViewResponse[]> {
  return request<ProductViewResponse[]>('/api/merchant/product/list', { auth: true })
}

export function fetchMerchantCategories() {
  return request<MerchantCategoryResponse[]>('/api/merchant/category/list', { auth: true })
}

export function saveMerchantCategory(payload: CategorySaveRequest) {
  return request<number>('/api/merchant/category/save', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function saveMerchantProduct(payload: ProductSaveRequest) {
  return request<number>('/api/merchant/product/save', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function updateMerchantCombo(id: number, payload: ComboUpdateRequest) {
  return request<void>(`/api/merchant/product/combo/${id}`, {
    method: 'PUT',
    body: payload,
    auth: true,
  })
}

export function updateMerchantProductEditor(id: number, payload: ProductEditorUpdateRequest) {
  return request<void>(`/api/merchant/product/${id}`, {
    method: 'PUT',
    body: payload,
    auth: true,
  })
}

export function updateMerchantProductBasic(id: number, payload: ProductBasicUpdateRequest) {
  return request<void>(`/api/merchant/product/basic/${id}`, {
    method: 'PUT',
    body: payload,
    auth: true,
  })
}

export function updateMerchantSku(skuId: number, payload: ProductSkuUpdateRequest) {
  return request<void>(`/api/merchant/product/sku/${skuId}`, {
    method: 'PUT',
    body: payload,
    auth: true,
  })
}

export function updateMerchantSkuStock(skuId: number, payload: SkuStockUpdateRequest) {
  return request<void>(`/api/merchant/product/stock/${skuId}`, {
    method: 'PUT',
    body: payload,
    auth: true,
  })
}

export function deleteMerchantProduct(id: number) {
  return request<void>(`/api/merchant/product/${id}`, {
    method: 'DELETE',
    auth: true,
  })
}

export function onShelfMerchantProduct(id: number) {
  return request<void>(`/api/merchant/product/on-shelf/${id}`, {
    method: 'PUT',
    auth: true,
  })
}

export function offShelfMerchantProduct(id: number) {
  return request<void>(`/api/merchant/product/off-shelf/${id}`, {
    method: 'PUT',
    auth: true,
  })
}

export async function uploadMerchantImage(file: File): Promise<string> {
  const session = loadSessionFromStorage()
  const baseUrl = import.meta.env.VITE_API_BASE_URL ?? ''
  const form = new FormData()
  form.append('file', file)
  const response = await fetch(`${baseUrl}/api/merchant/upload/image`, {
    method: 'POST',
    headers: session?.token ? { Authorization: `Bearer ${session.token}` } : undefined,
    body: form,
  })
  const payload = (await response.json()) as { code: number; message: string; data: string }
  if (!response.ok || payload.code !== 0) {
    throw new ApiError(payload.code || response.status, payload.message || '上传失败')
  }
  return payload.data
}

export function fetchMerchantTrend(days = 7) {
  return request<MerchantTrendPointResponse[]>(`/api/merchant/statistics/trend?days=${days}`, { auth: true })
}

export function fetchMerchantStatusDistribution(days = 7) {
  return request<MerchantStatusDistributionResponse[]>(`/api/merchant/statistics/status-distribution?days=${days}`, { auth: true })
}

export function fetchMerchantHotProducts(days = 7, limit = 10) {
  return request<MerchantHotProductResponse[]>(`/api/merchant/statistics/hot-products?days=${days}&limit=${limit}`, { auth: true })
}
