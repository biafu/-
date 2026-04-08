import type { UserAddressResponse } from '@/api/student'

const WEEKDAY_LABELS = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

export function buildStudentHomeCampus(addresses: UserAddressResponse[]) {
  const target = addresses.find((item) => Number(item.isDefault) === 1) ?? addresses[0]
  const campusName = target?.campusName?.trim()
  return campusName || '请选择校区'
}

export function buildStudentHomeContextTag(now: Date) {
  return `${now.getMonth() + 1}月${now.getDate()}日 ${WEEKDAY_LABELS[now.getDay()]}`
}
