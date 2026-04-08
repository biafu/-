import { describe, expect, it } from 'vitest'
import type { MerchantReviewResponse } from '@/api/merchant'
import {
  buildMerchantReviewFeed,
  buildMerchantReviewSummary,
  buildMerchantReviewBuckets,
} from '@/views/workspace/merchant-reviews'

const buildReview = (overrides: Partial<MerchantReviewResponse>): MerchantReviewResponse =>
  ({
    orderId: 1,
    orderNo: 202604080001,
    storeId: 1,
    storeName: '一食堂轻食档口',
    rating: 5,
    content: '出餐很快',
    isAnonymous: 0,
    reviewerName: '学生甲',
    createdAt: '2026-04-08T12:00:00',
    ...overrides,
  }) satisfies MerchantReviewResponse

describe('merchant reviews helpers', () => {
  it('builds summary metrics for review center cards', () => {
    const summary = buildMerchantReviewSummary([
      buildReview({ rating: 5 }),
      buildReview({ orderId: 2, orderNo: 202604080002, rating: 4 }),
      buildReview({ orderId: 3, orderNo: 202604080003, rating: 2 }),
    ])

    expect(summary).toEqual({
      totalCount: 3,
      averageRating: 3.7,
      positiveRate: 67,
    })
  })

  it('builds buckets for all five rating levels', () => {
    expect(
      buildMerchantReviewBuckets([
        buildReview({ rating: 5 }),
        buildReview({ orderId: 2, orderNo: 202604080002, rating: 5 }),
        buildReview({ orderId: 3, orderNo: 202604080003, rating: 3 }),
      ]),
    ).toEqual([
      { rating: 5, count: 2 },
      { rating: 4, count: 0 },
      { rating: 3, count: 1 },
      { rating: 2, count: 0 },
      { rating: 1, count: 0 },
    ])
  })

  it('sorts reviews newest-first and fills empty text gracefully', () => {
    const feed = buildMerchantReviewFeed([
      buildReview({ orderId: 10, orderNo: 202604080010, content: '', createdAt: '2026-04-07T12:00:00' }),
      buildReview({ orderId: 11, orderNo: 202604080011, reviewerName: '匿名用户', createdAt: '2026-04-08T12:00:00' }),
    ])

    expect(feed.map((item) => item.orderId)).toEqual([11, 10])
    expect(feed[1]).toMatchObject({
      orderId: 10,
      displayContent: '用户未填写文字评价',
    })
  })
})
