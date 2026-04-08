import type { MyCouponResponse, OrderDetailResponse, StudentMessageResponse, UserAddressResponse } from '@/api/student'

export function buildStudentProfileLinks() {
  return {
    profile: '/student/profile',
    coupons: '/student/coupons',
    orders: '/student/orders',
    addresses: '/student/addresses',
    messages: '/student/messages',
  }
}

export function buildStudentProfileSummary(input: {
  unpaidOrderCount: number
  unreadCount: number
  defaultAddressText: string
  availableCouponCount: number
}) {
  return [
    { key: 'orders', value: String(input.unpaidOrderCount), label: '待支付订单' },
    { key: 'messages', value: String(input.unreadCount), label: '未读消息' },
    { key: 'address', value: input.defaultAddressText, label: '默认地址' },
    { key: 'coupons', value: String(input.availableCouponCount), label: '可用优惠券' },
  ]
}

export function buildCouponSections(input: { claimableCount: number; ownedCount: number }) {
  return [
    { key: 'center', label: '可领取优惠券', count: input.claimableCount },
    { key: 'mine', label: '我的优惠券', count: input.ownedCount },
  ]
}

export function buildProfileQuickLinks(links = buildStudentProfileLinks()) {
  return [
    { key: 'orders', title: '我的订单', to: links.orders },
    { key: 'messages', title: '消息中心', to: links.messages },
    { key: 'addresses', title: '地址管理', to: links.addresses },
    { key: 'coupons', title: '优惠券', to: links.coupons },
  ]
}

export function buildStudentProfileActivity(input: {
  orders: OrderDetailResponse[]
  messages: StudentMessageResponse[]
}) {
  const orderActivities = input.orders.slice(0, 2).map((order) => ({
    key: `order-${order.id}`,
    title: order.storeName,
    meta: order.createdAt,
    to: `/student/orders/${order.id}`,
  }))

  const messageActivities = input.messages.slice(0, 2).map((message) => ({
    key: `message-${message.id}`,
    title: message.title,
    meta: message.createdAt,
    to: '/student/messages',
  }))

  return [...orderActivities, ...messageActivities].slice(0, 3)
}

export function buildDefaultAddressText(addresses: UserAddressResponse[]) {
  const target = addresses.find((item) => item.isDefault === 1)
  return target?.fullAddress || '还没有默认地址'
}

export function countAvailableCoupons(coupons: MyCouponResponse[]) {
  return coupons.filter((coupon) => Number(coupon.status) === 1).length
}

export function buildOrderDetailTarget(orders: Array<{ id: number }>, routeId: string) {
  const targetId = Number(routeId)
  return Number.isFinite(targetId) ? orders.find((order) => order.id === targetId) : undefined
}
