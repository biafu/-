import { describe, expect, it } from 'vitest'
import { buildStudentHomeCampus, buildStudentHomeContextTag } from '@/views/student/student-home'

describe('student home helpers', () => {
  it('prefers the default address campus name', () => {
    expect(
      buildStudentHomeCampus([
        {
          id: 1,
          campusName: '北校区',
          buildingName: '1号楼',
          roomNo: '101',
          contactName: '张三',
          contactPhone: '13800000000',
          isDefault: 0,
          fullAddress: '北校区 1号楼 101',
          updatedAt: '2026-04-08T08:00:00',
        },
        {
          id: 2,
          campusName: '南校区',
          buildingName: '5号宿舍',
          roomNo: '302',
          contactName: '李四',
          contactPhone: '13800000001',
          isDefault: 1,
          fullAddress: '南校区 5号宿舍 302',
          updatedAt: '2026-04-08T08:00:00',
        },
      ]),
    ).toBe('南校区')
  })

  it('builds a date-based context tag instead of fixed weather copy', () => {
    expect(buildStudentHomeContextTag(new Date('2026-04-08T09:00:00'))).toBe('4月8日 周三')
  })
})
