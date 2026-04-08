import { describe, expect, it } from 'vitest'
import { resolveAssetUrl } from '@/utils/assets'

describe('resolveAssetUrl', () => {
  it('resolves upload paths against api base origin', () => {
    expect(resolveAssetUrl('/uploads/demo.jpg', 'http://localhost:8080/api')).toBe(
      'http://localhost:8080/uploads/demo.jpg',
    )
  })

  it('preserves absolute image urls', () => {
    expect(resolveAssetUrl('https://cdn.example.com/demo.jpg', 'http://localhost:8080/api')).toBe(
      'https://cdn.example.com/demo.jpg',
    )
  })
})
