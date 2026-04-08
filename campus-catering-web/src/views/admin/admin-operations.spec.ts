import { describe, expect, it } from 'vitest'
import {
  buildAdminDispatchPayload,
  createAdminDispatchForm,
} from '@/views/admin/admin-operations'

describe('admin operations helpers', () => {
  it('creates an empty dispatch form state', () => {
    expect(createAdminDispatchForm()).toEqual({
      orderId: undefined,
      deliveryUserId: undefined,
      dispatchRemark: '',
    })
  })

  it('returns null when the dispatch form is incomplete', () => {
    expect(buildAdminDispatchPayload(createAdminDispatchForm())).toBeNull()
    expect(buildAdminDispatchPayload({ orderId: 18, deliveryUserId: undefined, dispatchRemark: '' })).toBeNull()
    expect(buildAdminDispatchPayload({ orderId: 0, deliveryUserId: 1, dispatchRemark: '' })).toBeNull()
  })

  it('builds a trimmed dispatch payload when the required ids exist', () => {
    expect(
      buildAdminDispatchPayload({
        orderId: 18,
        deliveryUserId: 1,
        dispatchRemark: '  请优先配送到图书馆门口  ',
      }),
    ).toEqual({
      orderId: 18,
      deliveryUserId: 1,
      dispatchRemark: '请优先配送到图书馆门口',
    })
  })
})
