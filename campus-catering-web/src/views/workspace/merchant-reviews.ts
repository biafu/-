import type { MerchantReviewResponse } from '@/api/merchant'

export type MerchantReviewSummary = {
  totalCount: number
  averageRating: number
  positiveRate: number
}

export type MerchantReviewBucket = {
  rating: number
  count: number
}

export type MerchantReviewFeedItem = MerchantReviewResponse & {
  displayContent: string
}

export function buildMerchantReviewSummary(reviews: MerchantReviewResponse[]): MerchantReviewSummary {
  if (reviews.length === 0) {
    return {
      totalCount: 0,
      averageRating: 0,
      positiveRate: 0,
    }
  }

  const totalRating = reviews.reduce((sum, item) => sum + Number(item.rating ?? 0), 0)
  const positiveCount = reviews.filter((item) => Number(item.rating ?? 0) >= 4).length

  return {
    totalCount: reviews.length,
    averageRating: Math.round((totalRating / reviews.length) * 10) / 10,
    positiveRate: Math.round((positiveCount / reviews.length) * 100),
  }
}

export function buildMerchantReviewBuckets(reviews: MerchantReviewResponse[]): MerchantReviewBucket[] {
  return [5, 4, 3, 2, 1].map((rating) => ({
    rating,
    count: reviews.filter((item) => Number(item.rating ?? 0) === rating).length,
  }))
}

export function buildMerchantReviewFeed(reviews: MerchantReviewResponse[]): MerchantReviewFeedItem[] {
  return [...reviews]
    .sort((left, right) => Date.parse(right.createdAt) - Date.parse(left.createdAt))
    .map((item) => ({
      ...item,
      displayContent: item.content?.trim() || '用户未填写文字评价',
    }))
}

export function formatMerchantReviewTime(raw: string | null | undefined) {
  const value = String(raw || '').trim()
  return value ? value.replace('T', ' ').slice(0, 16) : '--'
}
