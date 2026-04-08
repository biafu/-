import { describe, expect, it } from 'vitest'
import type { StoreBusinessHourResponse } from '@/api/merchant'
import {
  buildMerchantBusinessHoursPayload,
  buildMerchantBusinessHoursRows,
  getMerchantBusinessHoursValidationError,
} from '@/views/workspace/merchant-business-hours'

const buildHour = (overrides: Partial<StoreBusinessHourResponse>): StoreBusinessHourResponse =>
  ({
    id: 1,
    dayOfWeek: 1,
    startTime: '07:00:00',
    endTime: '21:00:00',
    status: 1,
    ...overrides,
  }) satisfies StoreBusinessHourResponse

describe('merchant business hours helpers', () => {
  it('builds a full seven-day form model and fills missing days with closed defaults', () => {
    const rows = buildMerchantBusinessHoursRows([
      buildHour({ id: 11, dayOfWeek: 3, startTime: '08:30:00', endTime: '20:00:00', status: 1 }),
      buildHour({ id: 12, dayOfWeek: 6, startTime: '10:00:00', endTime: '18:00:00', status: 0 }),
    ])

    expect(rows).toHaveLength(7)
    expect(rows[0]).toMatchObject({
      dayOfWeek: 1,
      label: '周一',
      startTime: '07:00',
      endTime: '21:00',
      status: 0,
    })
    expect(rows[2]).toMatchObject({
      id: 11,
      dayOfWeek: 3,
      label: '周三',
      startTime: '08:30',
      endTime: '20:00',
      status: 1,
    })
    expect(rows[5]).toMatchObject({
      id: 12,
      dayOfWeek: 6,
      label: '周六',
      startTime: '10:00',
      endTime: '18:00',
      status: 0,
    })
    expect(rows[6]).toMatchObject({
      dayOfWeek: 7,
      label: '周日',
      startTime: '07:00',
      endTime: '21:00',
      status: 0,
    })
  })

  it('rejects enabled rows whose start time is not earlier than the end time', () => {
    const rows = buildMerchantBusinessHoursRows([buildHour({ dayOfWeek: 2, startTime: '09:00:00', endTime: '09:00:00' })])

    expect(getMerchantBusinessHoursValidationError(rows)).toBe('周二的开始时间必须早于结束时间')
  })

  it('accepts disabled rows even if the time values are equal', () => {
    const rows = buildMerchantBusinessHoursRows([buildHour({ dayOfWeek: 5, startTime: '09:00:00', endTime: '09:00:00', status: 0 })])

    expect(getMerchantBusinessHoursValidationError(rows)).toBeNull()
  })

  it('builds a sorted save payload with hh:mm:ss values', () => {
    const rows = buildMerchantBusinessHoursRows([
      buildHour({ dayOfWeek: 7, startTime: '12:00:00', endTime: '20:30:00', status: 1 }),
      buildHour({ dayOfWeek: 1, startTime: '07:15:00', endTime: '19:45:00', status: 1 }),
    ])

    rows[1]!.startTime = '08:00'
    rows[1]!.endTime = '18:30'

    expect(buildMerchantBusinessHoursPayload(rows)).toEqual({
      hours: [
        { dayOfWeek: 1, startTime: '07:15:00', endTime: '19:45:00', status: 1 },
        { dayOfWeek: 2, startTime: '08:00:00', endTime: '18:30:00', status: 1 },
        { dayOfWeek: 3, startTime: '07:00:00', endTime: '21:00:00', status: 0 },
        { dayOfWeek: 4, startTime: '07:00:00', endTime: '21:00:00', status: 0 },
        { dayOfWeek: 5, startTime: '07:00:00', endTime: '21:00:00', status: 0 },
        { dayOfWeek: 6, startTime: '07:00:00', endTime: '21:00:00', status: 0 },
        { dayOfWeek: 7, startTime: '12:00:00', endTime: '20:30:00', status: 1 },
      ],
    })
  })
})
