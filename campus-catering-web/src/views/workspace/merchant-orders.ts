import type { OrderExceptionResponse } from '@/api/merchant'
import type { OrderDetailResponse } from '@/api/student'

export type MerchantOrderBoardKey = 'all' | 'paid' | 'preparing' | 'exception'

export type MerchantOrderBoard = {
  key: MerchantOrderBoardKey
  label: string
  count: number
  active: boolean
}

const ORDER_STATUS_PRIORITY: Record<string, number> = {
  PAID: 0,
  ACCEPTED: 1,
  PREPARING: 2,
  WAITING_DELIVERY: 3,
  DELIVERING: 4,
  COMPLETED: 5,
  CANCELLED: 6,
}

const ORDER_STATUS_LABELS: Record<string, string> = {
  PENDING_PAYMENT: '待支付',
  PAID: '待接单',
  ACCEPTED: '已接单',
  PREPARING: '备餐中',
  WAITING_DELIVERY: '待配送',
  DELIVERING: '配送中',
  COMPLETED: '已完成',
  CANCELLED: '已取消',
}

const ORDER_STATUS_TYPES: Record<string, 'success' | 'danger' | 'primary' | 'warning'> = {
  COMPLETED: 'success',
  CANCELLED: 'danger',
  PAID: 'primary',
  ACCEPTED: 'primary',
  PREPARING: 'primary',
}

const PREPARING_STATUSES = new Set(['ACCEPTED', 'PREPARING'])

export function buildMerchantOrderBoards(
  orders: OrderDetailResponse[],
  exceptions: OrderExceptionResponse[],
  activeBoard: MerchantOrderBoardKey,
): MerchantOrderBoard[] {
  return [
    { key: 'all', label: '全部订单', count: orders.length, active: activeBoard === 'all' },
    {
      key: 'paid',
      label: '待接单',
      count: orders.filter((order) => order.orderStatus === 'PAID').length,
      active: activeBoard === 'paid',
    },
    {
      key: 'preparing',
      label: '备餐中',
      count: orders.filter((order) => PREPARING_STATUSES.has(order.orderStatus)).length,
      active: activeBoard === 'preparing',
    },
    {
      key: 'exception',
      label: '异常单',
      count: exceptions.length,
      active: activeBoard === 'exception',
    },
  ]
}

export function sortMerchantOrdersByPriority(orders: OrderDetailResponse[]): OrderDetailResponse[] {
  return [...orders].sort((left, right) => {
    const leftPriority = ORDER_STATUS_PRIORITY[left.orderStatus] ?? Number.MAX_SAFE_INTEGER
    const rightPriority = ORDER_STATUS_PRIORITY[right.orderStatus] ?? Number.MAX_SAFE_INTEGER
    if (leftPriority !== rightPriority) {
      return leftPriority - rightPriority
    }

    const leftTime = Date.parse(left.createdAt)
    const rightTime = Date.parse(right.createdAt)
    const leftTimeValid = !Number.isNaN(leftTime)
    const rightTimeValid = !Number.isNaN(rightTime)

    if (leftTimeValid && rightTimeValid) {
      if (leftTime !== rightTime) {
        return rightTime - leftTime
      }
    } else if (leftTimeValid !== rightTimeValid) {
      return leftTimeValid ? -1 : 1
    }

    return right.id - left.id
  })
}

export function filterMerchantOrdersByBoard(
  orders: OrderDetailResponse[],
  activeBoard: MerchantOrderBoardKey,
): OrderDetailResponse[] {
  if (activeBoard === 'paid') {
    return orders.filter((order) => order.orderStatus === 'PAID')
  }
  if (activeBoard === 'preparing') {
    return orders.filter((order) => PREPARING_STATUSES.has(order.orderStatus))
  }
  if (activeBoard === 'exception') {
    return []
  }
  return [...orders]
}

export function getMerchantOrderEmptyDescription(activeBoard: MerchantOrderBoardKey): string {
  if (activeBoard === 'paid') {
    return '当前没有待接单订单'
  }
  if (activeBoard === 'preparing') {
    return '当前没有备餐中订单'
  }
  if (activeBoard === 'exception') {
    return '当前没有异常订单'
  }
  return '暂无订单'
}

export function getMerchantOrderStatusLabel(status: string) {
  return ORDER_STATUS_LABELS[status] ?? status
}

export function getMerchantOrderStatusType(status: string) {
  return ORDER_STATUS_TYPES[status] ?? 'warning'
}

export function formatMerchantOrderPrice(value: number | string | null | undefined) {
  return `${Number(value ?? 0).toFixed(2)}元`
}

export function formatMerchantOrderTime(raw: string | null | undefined) {
  const value = String(raw || '').trim()
  return value ? value.replace('T', ' ').slice(0, 16) : '--'
}
