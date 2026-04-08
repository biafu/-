import { describe, expect, it } from 'vitest'
import type { StoreSimpleResponse } from '@/api/student'
import {
  buildMerchantStoreSnapshot,
  buildMerchantStoreUpdatePayload,
  createMerchantStoreFormState,
  hasMerchantStoreRequiredFields,
} from '@/views/workspace/merchant-store'

const sampleStore: StoreSimpleResponse = {
  id: 7,
  merchantId: 3,
  storeName: '南苑食堂',
  address: '教学楼东侧',
  deliveryScopeDesc: '南区宿舍',
  deliveryRadiusKm: 1.5,
  minOrderAmount: 12,
  deliveryFee: 2,
  businessStatus: 1,
  notice: '晚高峰请耐心等待',
}

describe('merchant store helpers', () => {
  it('creates a normalized snapshot from api detail', () => {
    expect(buildMerchantStoreSnapshot(sampleStore)).toEqual({
      storeName: '南苑食堂',
      address: '教学楼东侧',
      minOrderAmount: 12,
      deliveryFee: 2,
      notice: '晚高峰请耐心等待',
      deliveryType: 2,
      deliveryScopeDesc: '南区宿舍',
      deliveryRadiusKm: 1.5,
      businessStatus: 1,
    })
  })

  it('builds a trimmed update payload from form state', () => {
    const form = createMerchantStoreFormState()
    Object.assign(form, {
      storeName: '  南苑食堂  ',
      address: '  教学楼东侧 ',
      deliveryScopeDesc: ' 南区宿舍 ',
      deliveryRadiusKm: 2,
      minOrderAmount: 15,
      deliveryFee: 3,
      businessStatus: 0,
      notice: '  今晚延迟出餐 ',
    })

    expect(buildMerchantStoreUpdatePayload(form)).toEqual({
      storeName: '南苑食堂',
      address: '教学楼东侧',
      deliveryType: 2,
      deliveryScopeDesc: '南区宿舍',
      deliveryRadiusKm: 2,
      minOrderAmount: 15,
      deliveryFee: 3,
      businessStatus: 0,
      notice: '今晚延迟出餐',
    })
  })

  it('checks required fields before submit', () => {
    const form = createMerchantStoreFormState()
    expect(hasMerchantStoreRequiredFields(form)).toBe(false)
    form.storeName = '南苑食堂'
    form.address = '教学楼东侧'
    expect(hasMerchantStoreRequiredFields(form)).toBe(true)
  })
})
