import { describe, expect, it } from 'vitest'
import type { OrderExceptionResponse } from '@/api/merchant'
import type { OrderDetailResponse } from '@/api/student'
import {
  buildMerchantOrderBoards,
  filterMerchantOrdersByBoard,
  getMerchantOrderEmptyDescription,
  getMerchantOrderStatusLabel,
  getMerchantOrderStatusType,
  formatMerchantOrderPrice,
  formatMerchantOrderTime,
  sortMerchantOrdersByPriority,
} from '@/views/workspace/merchant-orders'

const buildOrder = (overrides: Partial<OrderDetailResponse>): OrderDetailResponse =>
  ({
    id: 1,
    orderNo: '202404070001',
    orderStatus: 'PAID',
    storeId: 10,
    storeName: '测试门店',
    payAmount: 25.5,
    receiverName: '张三',
    receiverPhone: '13800000000',
    receiverAddress: '校区 1 号楼',
    createdAt: '2024-04-07T10:00:00.000Z',
    items: [],
    ...overrides,
  }) satisfies OrderDetailResponse

const buildException = (overrides: Partial<OrderExceptionResponse>): OrderExceptionResponse =>
  ({
    id: 1,
    orderId: 1,
    orderNo: 202404070001,
    orderStatus: 'PAID',
    payAmount: 25.5,
    reason: '地址有误',
    status: 'PENDING',
    createdAt: '2024-04-07T10:00:00.000Z',
    updatedAt: '2024-04-07T10:00:00.000Z',
    ...overrides,
  }) satisfies OrderExceptionResponse

describe('merchant order helpers', () => {
  it('formats merchant order status labels for known and unknown statuses', () => {
    expect(getMerchantOrderStatusLabel('PENDING_PAYMENT')).toBe('待支付')
    expect(getMerchantOrderStatusLabel('PAID')).toBe('待接单')
    expect(getMerchantOrderStatusLabel('ACCEPTED')).toBe('已接单')
    expect(getMerchantOrderStatusLabel('PREPARING')).toBe('备餐中')
    expect(getMerchantOrderStatusLabel('WAITING_DELIVERY')).toBe('待配送')
    expect(getMerchantOrderStatusLabel('DELIVERING')).toBe('配送中')
    expect(getMerchantOrderStatusLabel('COMPLETED')).toBe('已完成')
    expect(getMerchantOrderStatusLabel('CANCELLED')).toBe('已取消')
    expect(getMerchantOrderStatusLabel('ARCHIVED')).toBe('ARCHIVED')
  })

  it('maps merchant order statuses to UI tag types', () => {
    expect(getMerchantOrderStatusType('COMPLETED')).toBe('success')
    expect(getMerchantOrderStatusType('CANCELLED')).toBe('danger')
    expect(getMerchantOrderStatusType('PAID')).toBe('primary')
    expect(getMerchantOrderStatusType('ACCEPTED')).toBe('primary')
    expect(getMerchantOrderStatusType('PREPARING')).toBe('primary')
    expect(getMerchantOrderStatusType('WAITING_DELIVERY')).toBe('warning')
    expect(getMerchantOrderStatusType('ARCHIVED')).toBe('warning')
  })

  it('formats merchant order prices and timestamps for display', () => {
    expect(formatMerchantOrderPrice(25.5)).toBe('25.50元')
    expect(formatMerchantOrderPrice(undefined)).toBe('0.00元')
    expect(formatMerchantOrderTime('2024-04-07T10:15:30.000Z')).toBe('2024-04-07 10:15')
    expect(formatMerchantOrderTime('2024/04/07 10:15:30')).toBe('2024/04/07 10:15')
    expect(formatMerchantOrderTime('')).toBe('--')
  })

  it('buildMerchantOrderBoards counts orders and exceptions for every board', () => {
    const orders = [
      buildOrder({ id: 1, orderStatus: 'PAID' }),
      buildOrder({ id: 2, orderStatus: 'ACCEPTED' }),
      buildOrder({ id: 3, orderStatus: 'PREPARING' }),
      buildOrder({ id: 4, orderStatus: 'WAITING_DELIVERY' }),
      buildOrder({ id: 5, orderStatus: 'COMPLETED' }),
      buildOrder({ id: 6, orderStatus: 'CANCELLED' }),
    ]
    const exceptions = [buildException({ id: 11 }), buildException({ id: 12 })]

    expect(buildMerchantOrderBoards(orders, exceptions, 'preparing')).toEqual([
      { key: 'all', label: '全部订单', count: 6, active: false },
      { key: 'paid', label: '待接单', count: 1, active: false },
      { key: 'preparing', label: '备餐中', count: 2, active: true },
      { key: 'exception', label: '异常单', count: 2, active: false },
    ])
  })

  it('filterMerchantOrdersByBoard keeps all, paid, and preparing boards separated', () => {
    const orders = [
      buildOrder({ id: 1, orderStatus: 'PAID' }),
      buildOrder({ id: 2, orderStatus: 'ACCEPTED' }),
      buildOrder({ id: 3, orderStatus: 'PREPARING' }),
      buildOrder({ id: 4, orderStatus: 'DELIVERING' }),
    ]

    expect(filterMerchantOrdersByBoard(orders, 'all')).toEqual(orders)
    expect(filterMerchantOrdersByBoard(orders, 'paid')).toEqual([orders[0]])
    expect(filterMerchantOrdersByBoard(orders, 'preparing')).toEqual([orders[1], orders[2]])
  })

  it('sortMerchantOrdersByPriority orders by status priority then newest first', () => {
    const orders = [
      buildOrder({ id: 1, orderStatus: 'COMPLETED', createdAt: '2024-04-07T10:00:00.000Z' }),
      buildOrder({ id: 2, orderStatus: 'PREPARING', createdAt: '2024-04-07T10:10:00.000Z' }),
      buildOrder({ id: 3, orderStatus: 'PAID', createdAt: '2024-04-07T10:05:00.000Z' }),
      buildOrder({ id: 4, orderStatus: 'PAID', createdAt: '2024-04-07T10:20:00.000Z' }),
      buildOrder({ id: 5, orderStatus: 'CANCELLED', createdAt: '2024-04-07T10:30:00.000Z' }),
      buildOrder({ id: 6, orderStatus: 'ACCEPTED', createdAt: '2024-04-07T10:15:00.000Z' }),
      buildOrder({ id: 7, orderStatus: 'WAITING_DELIVERY', createdAt: '2024-04-07T10:25:00.000Z' }),
      buildOrder({ id: 8, orderStatus: 'DELIVERING', createdAt: '2024-04-07T10:35:00.000Z' }),
    ]

    expect(sortMerchantOrdersByPriority(orders).map((order) => order.id)).toEqual([4, 3, 6, 2, 7, 8, 1, 5])
  })

  it('sortMerchantOrdersByPriority uses deterministic fallback ordering when timestamps are invalid', () => {
    const orders = [
      buildOrder({ id: 11, orderStatus: 'PAID', createdAt: 'invalid-time' }),
      buildOrder({ id: 22, orderStatus: 'PAID', createdAt: '2024-04-07T10:20:00.000Z' }),
      buildOrder({ id: 33, orderStatus: 'PAID', createdAt: '2024-04-07T10:05:00.000Z' }),
      buildOrder({ id: 44, orderStatus: 'PAID', createdAt: 'still-invalid' }),
    ]

    expect(sortMerchantOrdersByPriority(orders).map((order) => order.id)).toEqual([22, 33, 44, 11])
  })

  it('sortMerchantOrdersByPriority places unknown statuses behind known priorities', () => {
    const orders = [
      buildOrder({ id: 1, orderStatus: 'COMPLETED', createdAt: '2024-04-07T10:00:00.000Z' }),
      buildOrder({ id: 2, orderStatus: 'ARCHIVED', createdAt: '2024-04-07T10:10:00.000Z' }),
      buildOrder({ id: 3, orderStatus: 'PAID', createdAt: '2024-04-07T10:20:00.000Z' }),
    ]

    expect(sortMerchantOrdersByPriority(orders).map((order) => order.id)).toEqual([3, 1, 2])
  })

  it('getMerchantOrderEmptyDescription returns the board-specific empty copy', () => {
    expect(getMerchantOrderEmptyDescription('all')).toBe('暂无订单')
    expect(getMerchantOrderEmptyDescription('paid')).toBe('当前没有待接单订单')
    expect(getMerchantOrderEmptyDescription('preparing')).toBe('当前没有备餐中订单')
    expect(getMerchantOrderEmptyDescription('exception')).toBe('当前没有异常订单')
  })
})
