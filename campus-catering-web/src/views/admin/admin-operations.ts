export type AdminDispatchFormState = {
  orderId: number | undefined
  deliveryUserId: number | undefined
  dispatchRemark: string
}

export function createAdminDispatchForm(): AdminDispatchFormState {
  return {
    orderId: undefined,
    deliveryUserId: undefined,
    dispatchRemark: '',
  }
}

export function buildAdminDispatchPayload(form: AdminDispatchFormState) {
  if (!form.orderId || form.orderId <= 0 || !form.deliveryUserId || form.deliveryUserId <= 0) {
    return null
  }

  const remark = form.dispatchRemark.trim()
  return {
    orderId: form.orderId,
    deliveryUserId: form.deliveryUserId,
    ...(remark ? { dispatchRemark: remark } : {}),
  }
}
