import { describe, expect, it } from 'vitest'
import { buildStudentStoreProductRows } from '@/views/student/student-store-products'

describe('student store product helpers', () => {
  it('preserves product image urls when building rows', () => {
    const rows = buildStudentStoreProductRows([
      {
        spuId: 1,
        categoryId: 1,
        productName: '黄焖鸡米饭',
        productType: 1,
        imageUrl: '/uploads/hmj.jpg',
        description: '招牌热卖',
        comboDesc: undefined,
        saleStatus: 1,
        sortNo: 1,
        skus: [
          {
            skuId: 101,
            skuName: '标准份',
            price: 18,
            stock: 12,
          },
        ],
      },
    ])

    expect(rows).toHaveLength(1)
    expect(rows[0]).toMatchObject({
      productName: '黄焖鸡米饭',
      skuId: 101,
      imageUrl: '/uploads/hmj.jpg',
    })
    expect(rows[0]?.fallbackImageUrl).toContain('data:image/svg+xml')
  })

  it('builds a placeholder image when product image is missing', () => {
    const rows = buildStudentStoreProductRows([
      {
        spuId: 2,
        categoryId: 1,
        productName: '双拼超值套餐',
        productType: 2,
        imageUrl: '   ',
        description: '适合双人分享',
        comboDesc: '双拼套餐',
        saleStatus: 1,
        sortNo: 2,
        comboItems: [
          {
            skuId: 201,
            spuName: '黄焖鸡米饭',
            skuName: '标准份',
            quantity: 1,
            sortNo: 1,
          },
          {
            skuId: 202,
            spuName: '香辣鸡腿饭',
            skuName: '标准份',
            quantity: 1,
            sortNo: 2,
          },
        ],
        skus: [
          {
            skuId: 203,
            skuName: '套餐标准份',
            price: 29,
            stock: 8,
          },
        ],
      },
    ])

    expect(rows).toHaveLength(1)
    expect(rows[0]?.imageUrl).toBe('')
    expect(rows[0]?.comboSummary).toBe('黄焖鸡米饭 x 1 + 香辣鸡腿饭 x 1')
    expect(rows[0]?.fallbackImageUrl).toContain('data:image/svg+xml')
  })
})
