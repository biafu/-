import { describe, expect, it } from 'vitest'
import { STUDENT_LAYOUT_BRAND } from '@/views/student/student-layout'

describe('student layout copy', () => {
  it('uses the correct chinese brand line', () => {
    expect(STUDENT_LAYOUT_BRAND).toBe('美团外卖 · 校园版')
  })
})
