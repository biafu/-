import type { MerchantApplyRequest } from '@/api/merchant'

export type MerchantApplyFormState = {
  merchantName: string
  contactName: string
  contactPhone: string
  campusCode: string
  licenseNo: string
}

export function createMerchantApplyFormState(): MerchantApplyFormState {
  return {
    merchantName: '',
    contactName: '',
    contactPhone: '',
    campusCode: '',
    licenseNo: '',
  }
}

export function getMerchantApplyValidationError(form: MerchantApplyFormState) {
  if (!form.merchantName.trim()) {
    return '请填写商户名称'
  }
  if (!form.contactName.trim()) {
    return '请填写联系人'
  }
  if (!/^1\d{10}$/.test(form.contactPhone.trim())) {
    return '请填写正确的联系电话'
  }
  if (!form.campusCode.trim()) {
    return '请填写校区编码'
  }
  if (!form.licenseNo.trim()) {
    return '请填写营业执照号'
  }
  return null
}

export function buildMerchantApplyPayload(form: MerchantApplyFormState): MerchantApplyRequest {
  return {
    merchantName: form.merchantName.trim(),
    contactName: form.contactName.trim(),
    contactPhone: form.contactPhone.trim(),
    campusCode: form.campusCode.trim(),
    licenseNo: form.licenseNo.trim(),
  }
}
