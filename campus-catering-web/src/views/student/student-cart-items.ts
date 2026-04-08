import { resolveAssetUrl } from '@/utils/assets'
import { buildStudentProductFallbackImage } from '@/views/student/student-store-products'

export function buildStudentCartImageUrl(input: { productName: string; imageUrl?: string }) {
  const resolvedImageUrl = resolveAssetUrl(input.imageUrl)
  if (resolvedImageUrl) {
    return resolvedImageUrl
  }
  return buildStudentProductFallbackImage(input.productName)
}
