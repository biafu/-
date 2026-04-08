import type {
  ProductBasicUpdateRequest,
  ProductEditorUpdateRequest,
  ProductSaveRequest,
  ProductSkuUpdateRequest,
} from '@/api/merchant'
import type { ProductViewResponse } from '@/api/student'

export type MerchantCategoryOption = {
  id: number
  categoryName: string
  sortNo: number
}

export type MerchantComboCandidate = {
  skuId: number
  productName: string
  skuName: string
}

export type MerchantComboDraftItem = {
  skuId: number | null
  quantity: number
}

export type MerchantProductEditorSubmitPayload = {
  categoryId: number
  productName: string
  productType: number
  imageUrl?: string
  description?: string
  sortNo: number
  comboDesc?: string
  comboItems: MerchantComboDraftItem[]
  skus: MerchantSkuDraft[]
}

export type MerchantProductEditorUpdateSource = {
  productType: number
  comboDesc?: string
}

export type MerchantSkuDraft = {
  skuId?: number
  skuName: string
  price: number
  stock: number
  skuStatus: number
}

export function buildMerchantCategoryOptions(
  products: ProductViewResponse[],
  fetchedCategories: MerchantCategoryOption[] = [],
): MerchantCategoryOption[] {
  const categoryMap = new Map<number, MerchantCategoryOption>()

  fetchedCategories.forEach((item) => {
    categoryMap.set(item.id, item)
  })

  products.forEach((product) => {
    if (!categoryMap.has(product.categoryId)) {
      categoryMap.set(product.categoryId, {
        id: product.categoryId,
        categoryName: `分类 ${product.categoryId}`,
        sortNo: 0,
      })
    }
  })

  return Array.from(categoryMap.values()).sort((left, right) => left.sortNo - right.sortNo || left.id - right.id)
}

export function buildMerchantComboCandidates(products: ProductViewResponse[]): MerchantComboCandidate[] {
  const candidates = new Map<number, MerchantComboCandidate>()

  products
    .filter((product) => product.productType === 1)
    .forEach((product) => {
      product.skus
        .filter((sku) => Number(sku.status ?? 1) === 1)
        .forEach((sku) => {
          candidates.set(sku.skuId, {
            skuId: sku.skuId,
            productName: product.productName,
            skuName: sku.skuName,
          })
        })
    })

  products.forEach((product) => {
    product.comboItems?.forEach((item) => {
      if (!candidates.has(item.skuId)) {
        candidates.set(item.skuId, {
          skuId: item.skuId,
          productName: item.spuName,
          skuName: item.skuName,
        })
      }
    })
  })

  return Array.from(candidates.values())
}

export function buildComboUpdatePayload(input: {
  comboDesc?: string
  comboItems: MerchantComboDraftItem[]
}) {
  return {
    comboDesc: input.comboDesc?.trim() || undefined,
    comboItems: input.comboItems.map((item, index) => ({
      skuId: Number(item.skuId),
      quantity: item.quantity,
      sortNo: index + 1,
    })),
  }
}

export function buildMerchantProductEditorUpdateRequest(input: {
  activeProduct: MerchantProductEditorUpdateSource
  payload: MerchantProductEditorSubmitPayload
  comboItems?: ProductEditorUpdateRequest['comboItems']
}): ProductEditorUpdateRequest {
  const primarySku = input.payload.skus[0]
  return {
    categoryId: input.payload.categoryId,
    productName: input.payload.productName,
    imageUrl: input.payload.imageUrl?.trim() ?? '',
    description: input.payload.description?.trim() ?? '',
    sortNo: input.payload.sortNo,
    skuId: primarySku?.skuId ?? 0,
    skuName: primarySku?.skuName.trim() ?? '',
    price: primarySku?.price ?? 0,
    stock: primarySku?.stock ?? 0,
    skuStatus: primarySku?.skuStatus ?? 1,
    comboDesc: input.activeProduct.productType === 2 ? input.payload.comboDesc?.trim() ?? '' : undefined,
    comboItems: input.comboItems,
  }
}

export function buildMerchantProductBasicUpdateRequest(
  payload: MerchantProductEditorSubmitPayload,
): ProductBasicUpdateRequest {
  return {
    categoryId: payload.categoryId,
    productName: payload.productName.trim(),
    imageUrl: payload.imageUrl?.trim() || undefined,
    description: payload.description?.trim() || undefined,
    sortNo: payload.sortNo,
  }
}

export function buildMerchantProductSaveRequest(payload: MerchantProductEditorSubmitPayload): ProductSaveRequest {
  return {
    categoryId: payload.categoryId,
    productName: payload.productName.trim(),
    productType: payload.productType,
    imageUrl: payload.imageUrl?.trim() || undefined,
    description: payload.description?.trim() || undefined,
    sortNo: payload.sortNo,
    comboDesc: payload.productType === 2 ? payload.comboDesc?.trim() || undefined : undefined,
    comboItems: payload.productType === 2 ? buildComboUpdatePayload(payload).comboItems : undefined,
    skus: payload.skus.map((sku) => ({
      skuName: sku.skuName.trim(),
      price: sku.price,
      stock: sku.stock,
    })),
  }
}

export function buildMerchantSkuUpdateRequest(sku: MerchantSkuDraft): ProductSkuUpdateRequest {
  return {
    skuName: sku.skuName.trim(),
    price: sku.price,
    status: sku.skuStatus,
  }
}

export function summarizeComboItems(
  comboItems?: Array<{ skuId: number; spuName: string; skuName: string; quantity: number }>,
) {
  if (!comboItems || comboItems.length === 0) {
    return ''
  }
  return comboItems.map((item) => `${item.spuName}x${item.quantity}`).join(' + ')
}

export function isMerchantProductEditable(product: ProductViewResponse) {
  return product.skus.length >= 1
}
