import { describe, expect, it } from 'vitest'
import type { StudentMessageResponse, StoreSimpleResponse } from '@/api/student'
import {
  deriveStoreCategory,
  filterAndSortStores,
  parseTargetOrderQuery,
  resolveMessageAction,
  type StudentHomeStore,
} from '@/views/student/student-touchpoints'

const baseStore = (overrides: Partial<StudentHomeStore> = {}): StudentHomeStore => ({
  id: 1,
  merchantId: 1,
  storeName: '川湘小馆',
  address: '一食堂旁',
  minOrderAmount: 20,
  deliveryFee: 3,
  businessStatus: 1,
  notice: '热辣套餐',
  score: 4.8,
  deliveryTime: '25分钟',
  distance: '420m',
  tags: ['校园配送'],
  monthlySales: 1000,
  avgPrice: 25,
  cover: 'linear-gradient(#fff,#eee)',
  ...overrides,
})

describe('student touchpoint helpers', () => {
  it('derives category from store content', () => {
    expect(deriveStoreCategory(baseStore({ storeName: '轻食沙拉', notice: '低脂午餐' }))).toBe('轻食')
    expect(deriveStoreCategory(baseStore({ storeName: '奶茶铺', notice: '第二杯半价' }))).toBe('饮品')
  })

  it('filters by keyword and category then sorts by delivery fee', () => {
    const stores = [
      baseStore({ id: 1, storeName: '轻食能量碗', deliveryFee: 5, notice: '轻食优选' }),
      baseStore({ id: 2, storeName: '炸鸡小站', deliveryFee: 2, notice: '酥脆套餐' }),
      baseStore({ id: 3, storeName: '轻食研究所', deliveryFee: 1, notice: '健身轻食' }),
    ]

    expect(
      filterAndSortStores(stores, {
        keyword: '轻食',
        category: '轻食',
        sortMode: 'deliveryFee',
      }).map((item) => item.id),
    ).toEqual([3, 1])
  })

  it('resolves order and store message actions', () => {
    const orderMessage: StudentMessageResponse = {
      id: 1,
      title: '订单已接单',
      content: '请查看最新状态',
      messageType: 'ORDER_PROGRESS',
      bizType: 'ORDER',
      bizId: 88,
      isRead: 0,
      createdAt: '2026-04-06T10:00:00',
    }
    const storeMessage: StudentMessageResponse = {
      ...orderMessage,
      id: 2,
      title: '门店活动',
      messageType: 'NOTICE',
      bizType: 'STORE',
      bizId: 12,
    }

    expect(resolveMessageAction(orderMessage)).toEqual({
      type: 'order',
      to: {
        path: '/student/orders/88',
      },
    })
    expect(resolveMessageAction(storeMessage)).toEqual({
      type: 'store',
      to: {
        path: '/student/store/12',
      },
    })
  })

  it('parses target order ids from route query', () => {
    expect(parseTargetOrderQuery({ expandOrderId: '23', highlightOrderId: '23' })).toEqual({
      expandOrderId: 23,
      highlightOrderId: 23,
    })
    expect(parseTargetOrderQuery({})).toEqual({
      expandOrderId: null,
      highlightOrderId: null,
    })
  })
})
