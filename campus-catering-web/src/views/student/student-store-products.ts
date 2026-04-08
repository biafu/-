import type { ProductViewResponse } from '@/api/student'
import { resolveAssetUrl } from '@/utils/assets'

export type StudentStoreProductRow = {
  productName: string
  productType: number
  comboSummary?: string
  skuId: number
  skuName: string
  stock: number
  price: number
  imageUrl: string
  fallbackImageUrl: string
}

const encodeSvg = (svg: string) =>
  `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg.replace(/\s+/g, ' ').trim())}`

export function buildStudentProductFallbackImage(productName: string) {
  const label = (productName || '商品').slice(0, 2)
  return encodeSvg(`
    <svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240">
      <defs>
        <linearGradient id="bg" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#ffe4b5" />
          <stop offset="100%" stop-color="#ffb36b" />
        </linearGradient>
      </defs>
      <rect width="240" height="240" rx="32" fill="url(#bg)" />
      <circle cx="120" cy="96" r="44" fill="rgba(255,255,255,0.38)" />
      <rect x="46" y="146" width="148" height="22" rx="11" fill="rgba(255,255,255,0.34)" />
      <text x="120" y="108" text-anchor="middle" font-size="34" font-family="Microsoft YaHei, Arial, sans-serif" fill="#7a3b00">
        ${label}
      </text>
    </svg>
  `)
}

export function buildStudentStoreProductRows(products: ProductViewResponse[]): StudentStoreProductRow[] {
  return products.flatMap((product) =>
    product.skus.map((sku) => ({
      productName: product.productName,
      productType: Number(product.productType ?? 1),
      comboSummary:
        Number(product.productType ?? 1) === 2
          ? product.comboItems?.map((comboItem) => `${comboItem.spuName} x ${comboItem.quantity}`).join(' + ')
          : undefined,
      skuId: sku.skuId,
      skuName: sku.skuName,
      stock: Number(sku.stock ?? 0),
      price: Number(sku.price ?? 0),
      imageUrl: resolveAssetUrl(product.imageUrl),
      fallbackImageUrl: buildStudentProductFallbackImage(product.productName),
    })),
  )
}
