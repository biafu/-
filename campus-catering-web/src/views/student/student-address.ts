import type { UserAddressSaveRequest } from '@/api/student'

export type StudentAddressFormValue = {
  campusName: string
  buildingName: string
  roomNo: string
  detailAddress: string
  contactName: string
  contactPhone: string
}

export function validateStudentAddressForm(form: StudentAddressFormValue) {
  if (
    !form.campusName.trim() ||
    !form.buildingName.trim() ||
    !form.roomNo.trim() ||
    !form.contactName.trim() ||
    !form.contactPhone.trim()
  ) {
    return '请先填写校区、楼栋、房间号、联系人和联系电话'
  }
  return null
}

export function buildStudentAddressSavePayload(input: {
  editingId?: number
  isDefaultChecked: boolean
  form: StudentAddressFormValue
}): UserAddressSaveRequest {
  return {
    id: input.editingId,
    campusName: input.form.campusName.trim(),
    buildingName: input.form.buildingName.trim(),
    roomNo: input.form.roomNo.trim(),
    detailAddress: input.form.detailAddress.trim() || undefined,
    contactName: input.form.contactName.trim(),
    contactPhone: input.form.contactPhone.trim(),
    isDefault: input.isDefaultChecked ? 1 : 0,
  }
}

export function buildStudentAddressPreview(form: StudentAddressFormValue) {
  return (
    [form.campusName, form.buildingName, form.roomNo, form.detailAddress]
      .map((item) => item.trim())
      .filter(Boolean)
      .join(' / ') || '地址预览会显示在这里'
  )
}

export function formatStudentAddressTime(raw: string) {
  return String(raw || '').replace('T', ' ').slice(0, 16)
}

export function buildStudentAddressRouteQuery(options: {
  returnToCheckout?: boolean
  openCreate?: boolean
}) {
  const query: Record<string, string> = {}
  if (options.returnToCheckout) {
    query.returnTo = 'checkout'
  }
  if (options.openCreate) {
    query.openCreate = '1'
  }
  return query
}

export function parseStudentAddressRouteQuery(query: Record<string, unknown>) {
  const normalizeValue = (value: unknown) => {
    if (Array.isArray(value)) {
      return String(value[0] ?? '')
    }
    return String(value ?? '')
  }

  return {
    returnToCheckout: normalizeValue(query.returnTo) === 'checkout',
    openCreate: normalizeValue(query.openCreate) === '1',
  }
}
