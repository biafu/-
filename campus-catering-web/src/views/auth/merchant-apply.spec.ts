import { describe, expect, it } from 'vitest'
import {
  buildMerchantApplyPayload,
  createMerchantApplyFormState,
  getMerchantApplyValidationError,
} from '@/views/auth/merchant-apply'

describe('merchant apply helpers', () => {
  it('creates an empty merchant application form', () => {
    expect(createMerchantApplyFormState()).toEqual({
      merchantName: '',
      contactName: '',
      contactPhone: '',
      campusCode: '',
      licenseNo: '',
    })
  })

  it('validates required fields and phone format', () => {
    expect(getMerchantApplyValidationError(createMerchantApplyFormState())).toBe('请填写商户名称')
    expect(
      getMerchantApplyValidationError({
        merchantName: '食堂一楼轻食铺',
        contactName: '李店长',
        contactPhone: '123',
        campusCode: 'SOUTH',
        licenseNo: 'LIC-2026-001',
      }),
    ).toBe('请填写正确的联系电话')
  })

  it('builds a trimmed payload when the form is complete', () => {
    expect(
      buildMerchantApplyPayload({
        merchantName: '  食堂一楼轻食铺 ',
        contactName: ' 李店长 ',
        contactPhone: ' 13800000001 ',
        campusCode: ' SOUTH ',
        licenseNo: ' LIC-2026-001 ',
      }),
    ).toEqual({
      merchantName: '食堂一楼轻食铺',
      contactName: '李店长',
      contactPhone: '13800000001',
      campusCode: 'SOUTH',
      licenseNo: 'LIC-2026-001',
    })
  })
})
