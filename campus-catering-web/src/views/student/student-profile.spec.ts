import { describe, expect, it } from 'vitest'
import {
  buildOrderDetailTarget,
  buildCouponSections,
  buildStudentProfileLinks,
  buildStudentProfileSummary,
} from '@/views/student/student-profile'

describe('student profile helpers', () => {
  it('exposes stable student destinations', () => {
    expect(buildStudentProfileLinks()).toMatchObject({
      profile: '/student/profile',
      coupons: '/student/coupons',
      orders: '/student/orders',
      addresses: '/student/addresses',
    })
  })

  it('builds summary cards', () => {
    expect(
      buildStudentProfileSummary({
        unpaidOrderCount: 2,
        unreadCount: 5,
        defaultAddressText: '南校区 / 5号宿舍 / 302',
        availableCouponCount: 3,
      }),
    ).toEqual([
      { key: 'orders', value: '2', label: '待支付订单' },
      { key: 'messages', value: '5', label: '未读消息' },
      { key: 'address', value: '南校区 / 5号宿舍 / 302', label: '默认地址' },
      { key: 'coupons', value: '3', label: '可用优惠券' },
    ])
  })

  it('builds coupon sections', () => {
    expect(buildCouponSections({ claimableCount: 2, ownedCount: 4 })).toEqual([
      { key: 'center', label: '可领取优惠券', count: 2 },
      { key: 'mine', label: '我的优惠券', count: 4 },
    ])
  })

  it('finds a target order by route id', () => {
    expect(buildOrderDetailTarget([{ id: 101 }, { id: 102 }], '101')?.id).toBe(101)
    expect(buildOrderDetailTarget([{ id: 101 }, { id: 102 }], '999')).toBeUndefined()
  })
})
