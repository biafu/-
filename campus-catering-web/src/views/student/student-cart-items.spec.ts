import { describe, expect, it } from 'vitest'
import { buildStudentCartImageUrl } from '@/views/student/student-cart-items'

describe('student cart item helpers', () => {
  it('resolves cart item upload image urls', () => {
    expect(buildStudentCartImageUrl({ productName: '黄焖鸡米饭', imageUrl: '/uploads/hmj.jpg' })).toBe('/uploads/hmj.jpg')
  })

  it('returns fallback image when cart item has no image', () => {
    expect(buildStudentCartImageUrl({ productName: '黄焖鸡米饭', imageUrl: '   ' })).toContain(
      'data:image/svg+xml',
    )
  })
})
