import type { StoreBusinessHourResponse, StoreBusinessHoursSaveRequest } from '@/api/merchant'

export type MerchantBusinessHourFormRow = {
  id?: number
  dayOfWeek: number
  label: string
  startTime: string
  endTime: string
  status: number
}

const DAY_LABELS = [
  '\u5468\u4e00',
  '\u5468\u4e8c',
  '\u5468\u4e09',
  '\u5468\u56db',
  '\u5468\u4e94',
  '\u5468\u516d',
  '\u5468\u65e5',
] as const

const DEFAULT_START_TIME = '07:00'
const DEFAULT_END_TIME = '21:00'

function normalizeDisplayTime(value: string | undefined, fallback: string) {
  const text = String(value || '').trim()
  if (!text) {
    return fallback
  }

  return text.slice(0, 5)
}

function normalizePayloadTime(value: string | undefined, fallback: string) {
  const text = String(value || '').trim()
  if (!text) {
    return `${fallback}:00`
  }

  if (text.length === 5) {
    return `${text}:00`
  }

  return text.slice(0, 8)
}

function toMinutes(value: string) {
  const [hour, minute] = String(value || '')
    .split(':')
    .map((segment) => Number(segment))
  return hour * 60 + minute
}

export function buildMerchantBusinessHoursRows(hours: StoreBusinessHourResponse[]): MerchantBusinessHourFormRow[] {
  return DAY_LABELS.map((label, index) => {
    const dayOfWeek = index + 1
    const matched = hours.find((item) => Number(item.dayOfWeek) === dayOfWeek)

    return {
      id: matched?.id,
      dayOfWeek,
      label,
      startTime: normalizeDisplayTime(matched?.startTime, DEFAULT_START_TIME),
      endTime: normalizeDisplayTime(matched?.endTime, DEFAULT_END_TIME),
      status: Number(matched?.status ?? 0),
    }
  })
}

export function getMerchantBusinessHoursValidationError(rows: MerchantBusinessHourFormRow[]) {
  for (const row of rows) {
    if (Number(row.status) !== 1) {
      continue
    }

    if (toMinutes(row.startTime) >= toMinutes(row.endTime)) {
      return `${row.label}\u7684\u5f00\u59cb\u65f6\u95f4\u5fc5\u987b\u65e9\u4e8e\u7ed3\u675f\u65f6\u95f4`
    }
  }

  return null
}

export function buildMerchantBusinessHoursPayload(rows: MerchantBusinessHourFormRow[]): StoreBusinessHoursSaveRequest {
  return {
    hours: [...rows]
      .sort((left, right) => left.dayOfWeek - right.dayOfWeek)
      .map((row) => {
        const normalizedStartTime = normalizePayloadTime(row.startTime, DEFAULT_START_TIME)
        const normalizedEndTime = normalizePayloadTime(row.endTime, DEFAULT_END_TIME)
        const hasCustomizedHours =
          normalizedStartTime !== `${DEFAULT_START_TIME}:00` || normalizedEndTime !== `${DEFAULT_END_TIME}:00`

        return {
          dayOfWeek: row.dayOfWeek,
          startTime: normalizedStartTime,
          endTime: normalizedEndTime,
          status: Number(row.status ?? 0) === 1 || (!row.id && hasCustomizedHours) ? 1 : 0,
        }
      }),
  }
}
