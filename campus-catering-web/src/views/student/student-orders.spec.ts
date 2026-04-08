import { describe, expect, it } from 'vitest'
import {
  buildStudentOrderDetailPath,
  canRefundStudentOrder,
  formatStudentOrderPrice,
  getStudentOrderStatusClass,
  getStudentOrderStatusLabel,
} from '@/views/student/student-orders'

describe('student order helpers', () => {
  it('builds the detail page path for an order', () => {
    expect(buildStudentOrderDetailPath(88)).toBe('/student/orders/88')
  })

  it('maps order status labels and classes', () => {
    expect(getStudentOrderStatusLabel('PENDING_PAYMENT')).toBe('待支付')
    expect(getStudentOrderStatusLabel('UNKNOWN_STATUS')).toBe('UNKNOWN_STATUS')
    expect(getStudentOrderStatusClass('COMPLETED')).toBe('completed')
    expect(getStudentOrderStatusClass('REFUNDED')).toBe('cancelled')
    expect(getStudentOrderStatusClass('PREPARING')).toBe('processing')
  })

  it('formats price values with two decimals', () => {
    expect(formatStudentOrderPrice(18)).toBe('18.00元')
    expect(formatStudentOrderPrice(undefined)).toBe('0.00元')
  })

  it('identifies which order statuses can request a refund', () => {
    expect(canRefundStudentOrder('PAID')).toBe(true)
    expect(canRefundStudentOrder('ACCEPTED')).toBe(true)
    expect(canRefundStudentOrder('PREPARING')).toBe(true)
    expect(canRefundStudentOrder('WAITING_DELIVERY')).toBe(true)
    expect(canRefundStudentOrder('PENDING_PAYMENT')).toBe(false)
    expect(canRefundStudentOrder('DELIVERING')).toBe(false)
    expect(canRefundStudentOrder('COMPLETED')).toBe(false)
    expect(canRefundStudentOrder('CANCELLED')).toBe(false)
    expect(canRefundStudentOrder('REFUNDED')).toBe(false)
  })
})
