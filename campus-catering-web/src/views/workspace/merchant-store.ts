import type { StoreUpdateRequest } from '@/api/merchant'
import type { StoreSimpleResponse } from '@/api/student'

export type MerchantStoreFormState = {
  storeName: string
  address: string
  minOrderAmount: number
  deliveryFee: number
  notice: string
  deliveryType: number
  deliveryScopeDesc: string
  deliveryRadiusKm: number
  businessStatus: number
}

export function createMerchantStoreFormState(): MerchantStoreFormState {
  return {
    storeName: '',
    address: '',
    minOrderAmount: 0,
    deliveryFee: 0,
    notice: '',
    deliveryType: 2,
    deliveryScopeDesc: '',
    deliveryRadiusKm: 0,
    businessStatus: 1,
  }
}

export function buildMerchantStoreSnapshot(detail: StoreSimpleResponse): MerchantStoreFormState {
  return {
    storeName: detail.storeName || '',
    address: detail.address || '',
    minOrderAmount: Number(detail.minOrderAmount ?? 0),
    deliveryFee: Number(detail.deliveryFee ?? 0),
    notice: detail.notice || '',
    deliveryType: 2,
    deliveryScopeDesc: detail.deliveryScopeDesc || '',
    deliveryRadiusKm: Number(detail.deliveryRadiusKm ?? 0),
    businessStatus: Number(detail.businessStatus ?? 0),
  }
}

export function applyMerchantStoreSnapshot(form: MerchantStoreFormState, snapshot: MerchantStoreFormState) {
  form.storeName = snapshot.storeName
  form.address = snapshot.address
  form.minOrderAmount = snapshot.minOrderAmount
  form.deliveryFee = snapshot.deliveryFee
  form.notice = snapshot.notice
  form.deliveryType = snapshot.deliveryType
  form.deliveryScopeDesc = snapshot.deliveryScopeDesc
  form.deliveryRadiusKm = snapshot.deliveryRadiusKm
  form.businessStatus = snapshot.businessStatus
}

export function buildMerchantStoreUpdatePayload(form: MerchantStoreFormState): StoreUpdateRequest {
  return {
    storeName: form.storeName.trim(),
    address: form.address.trim(),
    deliveryType: form.deliveryType,
    deliveryScopeDesc: form.deliveryScopeDesc.trim(),
    deliveryRadiusKm: form.deliveryRadiusKm,
    minOrderAmount: form.minOrderAmount,
    deliveryFee: form.deliveryFee,
    businessStatus: form.businessStatus,
    notice: form.notice.trim(),
  }
}

export function hasMerchantStoreRequiredFields(form: MerchantStoreFormState) {
  return form.storeName.trim().length > 0 && form.address.trim().length > 0
}
