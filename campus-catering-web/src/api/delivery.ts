import { request } from '@/api/http'

export type DeliveryOrderResponse = {
  orderId: number
  orderNo: number
  storeId: number
  storeName?: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  orderStatus: string
  dispatchStatus: number
  dispatchRemark?: string
  payAmount: number
  createdAt: string
  pickupTime?: string
  deliveredTime?: string
}

export function fetchAvailableDeliveryOrders() {
  return request<DeliveryOrderResponse[]>('/api/delivery/order/available', { auth: true })
}

export function fetchDeliveryOrders() {
  return request<DeliveryOrderResponse[]>('/api/delivery/order/list', { auth: true })
}

export function claimDeliveryOrder(orderId: number) {
  return request<void>(`/api/delivery/order/claim/${orderId}`, {
    method: 'POST',
    auth: true,
  })
}

export function pickupDeliveryOrder(orderId: number) {
  return request<void>(`/api/delivery/order/pickup/${orderId}`, {
    method: 'POST',
    auth: true,
  })
}

export function completeDeliveryOrder(orderId: number) {
  return request<void>(`/api/delivery/order/complete/${orderId}`, {
    method: 'POST',
    auth: true,
  })
}
