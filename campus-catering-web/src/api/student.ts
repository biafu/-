import { request } from '@/api/http'

export type StoreSimpleResponse = {
  id: number
  merchantId: number
  storeName: string
  address: string
  deliveryScopeDesc?: string
  deliveryRadiusKm?: number
  minOrderAmount: number
  deliveryFee: number
  businessStatus: number
  notice?: string
}

export type SkuView = {
  skuId: number
  skuName: string
  price: number
  stock: number
  status?: number
}

export type ComboItemView = {
  skuId: number
  spuName: string
  skuName: string
  quantity: number
  sortNo: number
}

export type ProductViewResponse = {
  spuId: number
  categoryId: number
  productName: string
  productType: number
  imageUrl?: string
  description?: string
  comboDesc?: string
  saleStatus: number
  sortNo?: number
  skus: SkuView[]
  comboItems?: ComboItemView[]
}

export type OrderItemVO = {
  skuId: number
  spuName: string
  skuName: string
  price: number
  quantity: number
  totalAmount: number
}

export type OrderDetailResponse = {
  id: number
  orderNo: string
  orderStatus: string
  storeId: number
  storeName: string
  payAmount: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark?: string
  createdAt: string
  items: OrderItemVO[]
}

export type OrderReviewRequest = {
  orderId: number
  rating: number
  content?: string
  isAnonymous?: number
}

export type OrderReviewResponse = {
  orderId: number
  rating: number
  content?: string
  isAnonymous: number
  createdAt: string
}

export type CartItemResponse = {
  id: number
  storeId: number
  skuId: number
  productName: string
  imageUrl?: string
  skuName: string
  price: number
  quantity: number
  totalAmount: number
}

export type CartQuantityUpdateRequest = {
  quantity: number
}

export type OrderItemRequest = {
  skuId: number
  quantity: number
}

export type OrderPreviewRequest = {
  storeId: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  userCouponId?: number
  remark?: string
  items: OrderItemRequest[]
}

export type OrderPreviewItemResponse = {
  skuId: number
  productName: string
  skuName: string
  quantity: number
  price: number
  totalAmount: number
}

export type OrderPreviewResponse = {
  goodsAmount: number
  deliveryFee: number
  discountAmount: number
  payAmount: number
  items: OrderPreviewItemResponse[]
}

export type OrderCreateResponse = {
  orderId: number
  orderNo: string
  orderStatus: string
  payAmount: number
}

export type CouponCenterResponse = {
  couponId: number
  couponName: string
  discountAmount: number
  thresholdAmount: number
  storeId?: number
  startTime: string
  endTime: string
  totalCount: number
  receiveCount: number
  remainingCount: number
}

export type MyCouponResponse = {
  userCouponId: number
  couponId: number
  couponName: string
  discountAmount: number
  thresholdAmount: number
  storeId?: number
  startTime: string
  endTime: string
  status: number
}

export type UserAddressResponse = {
  id: number
  campusName: string
  buildingName: string
  roomNo: string
  detailAddress?: string
  contactName: string
  contactPhone: string
  isDefault: number
  fullAddress: string
  updatedAt: string
}

export type UserAddressSaveRequest = {
  id?: number
  campusName: string
  buildingName: string
  roomNo: string
  detailAddress?: string
  contactName: string
  contactPhone: string
  isDefault: number
}

export type StudentMessageResponse = {
  id: number
  title: string
  content: string
  messageType: string
  bizType?: string
  bizId?: number
  isRead: number
  createdAt: string
}

export function fetchStoreList() {
  return request<StoreSimpleResponse[]>('/api/student/store/list')
}

export function fetchStoreDetail(id: number) {
  return request<StoreSimpleResponse>(`/api/student/store/${id}`)
}

export function fetchProductList(storeId: number) {
  return request<ProductViewResponse[]>(`/api/student/product/list?storeId=${storeId}`)
}

export function fetchStudentOrders() {
  return request<OrderDetailResponse[]>('/api/student/order/list', { auth: true })
}

export function fetchCartList() {
  return request<CartItemResponse[]>('/api/student/cart', { auth: true })
}

export function updateCartQuantity(id: number, quantity: number) {
  return request<void>(`/api/student/cart/${id}/quantity`, {
    method: 'PATCH',
    body: { quantity } satisfies CartQuantityUpdateRequest,
    auth: true,
  })
}

export function deleteCartItem(id: number) {
  return request<void>(`/api/student/cart/${id}`, {
    method: 'DELETE',
    auth: true,
  })
}

export function clearStoreCart(storeId: number) {
  return request<void>(`/api/student/cart/store/${storeId}`, {
    method: 'DELETE',
    auth: true,
  })
}

export function previewStudentOrder(payload: OrderPreviewRequest) {
  return request<OrderPreviewResponse>('/api/student/order/preview', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function createStudentOrder(payload: OrderPreviewRequest) {
  return request<OrderCreateResponse>('/api/student/order/create', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function payStudentOrder(orderId: number) {
  return request<void>(`/api/student/order/pay/${orderId}`, {
    method: 'POST',
    auth: true,
  })
}

export function refundStudentOrder(orderId: number) {
  return request<void>(`/api/student/order/refund/${orderId}`, {
    method: 'POST',
    auth: true,
  })
}

export function cancelStudentOrder(orderId: number) {
  return request<void>(`/api/student/order/cancel/${orderId}`, {
    method: 'POST',
    auth: true,
  })
}

export function reorderStudentOrder(orderId: number) {
  return request<OrderCreateResponse>(`/api/student/order/reorder/${orderId}`, {
    method: 'POST',
    auth: true,
  })
}

export function urgeStudentOrder(orderId: number) {
  return request<void>(`/api/student/order/urge/${orderId}`, {
    method: 'POST',
    auth: true,
  })
}

export function submitOrderReview(payload: OrderReviewRequest) {
  return request<void>('/api/student/order/review', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function fetchOrderReview(orderId: number) {
  return request<OrderReviewResponse | null>(`/api/student/order/review/${orderId}`, { auth: true })
}

export function addToCart(payload: { storeId: number; skuId: number; quantity: number }) {
  return request<void>('/api/student/cart/add', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function fetchCouponCenter(storeId?: number) {
  const query = typeof storeId === 'number' ? `?storeId=${storeId}` : ''
  return request<CouponCenterResponse[]>(`/api/student/coupon/center${query}`, { auth: true })
}

export function claimCoupon(couponId: number) {
  return request<void>(`/api/student/coupon/claim/${couponId}`, {
    method: 'POST',
    auth: true,
  })
}

export function fetchMyCoupons(storeId?: number) {
  const query = typeof storeId === 'number' ? `?storeId=${storeId}` : ''
  return request<MyCouponResponse[]>(`/api/student/coupon/my${query}`, { auth: true })
}

export function fetchAddressList() {
  return request<UserAddressResponse[]>('/api/student/address/list', { auth: true })
}

export function saveAddress(payload: UserAddressSaveRequest) {
  return request<number>('/api/student/address/save', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function setDefaultAddress(id: number) {
  return request<void>(`/api/student/address/default/${id}`, {
    method: 'POST',
    auth: true,
  })
}

export function deleteAddress(id: number) {
  return request<void>(`/api/student/address/${id}`, {
    method: 'DELETE',
    auth: true,
  })
}

export function fetchStudentMessages(params?: { onlyUnread?: boolean; limit?: number }) {
  const query = new URLSearchParams()
  if (typeof params?.onlyUnread === 'boolean') {
    query.set('onlyUnread', String(params.onlyUnread))
  }
  if (typeof params?.limit === 'number') {
    query.set('limit', String(params.limit))
  }
  const suffix = query.toString() ? `?${query.toString()}` : ''
  return request<StudentMessageResponse[]>(`/api/student/message/list${suffix}`, { auth: true })
}

export function fetchStudentUnreadCount() {
  return request<number>('/api/student/message/unread-count', { auth: true })
}

export function markStudentMessageRead(id: number) {
  return request<void>(`/api/student/message/read/${id}`, {
    method: 'POST',
    auth: true,
  })
}

export function markAllStudentMessagesRead() {
  return request<void>('/api/student/message/read-all', {
    method: 'POST',
    auth: true,
  })
}
