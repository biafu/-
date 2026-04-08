const REFUNDABLE_ORDER_STATUSES = new Set(['PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY'])

export function buildStudentOrderDetailPath(orderId: number) {
  return `/student/orders/${orderId}`
}

export function getStudentOrderStatusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING_PAYMENT: '待支付',
    PAID: '已支付',
    ACCEPTED: '商家已接单',
    PREPARING: '备餐中',
    WAITING_DELIVERY: '待配送',
    DELIVERING: '配送中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    REFUNDING: '退款中',
    REFUNDED: '已退款',
  }

  return map[status] ?? status
}

export function getStudentOrderStatusClass(status: string) {
  if (status === 'COMPLETED' || status === 'PAID') {
    return 'completed'
  }

  if (status === 'CANCELLED' || status === 'REFUNDED') {
    return 'cancelled'
  }

  return 'processing'
}

export function canRefundStudentOrder(status: string) {
  return REFUNDABLE_ORDER_STATUSES.has(status)
}

export function formatStudentOrderPrice(value?: number) {
  return `${Number(value ?? 0).toFixed(2)}元`
}

export function formatStudentOrderTime(raw: string) {
  if (!raw) {
    return '-'
  }

  return String(raw).replace('T', ' ').slice(0, 16)
}
