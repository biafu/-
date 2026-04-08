import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'
import {
  buildStudentAddressPreview,
  buildStudentAddressRouteQuery,
  buildStudentAddressSavePayload,
  formatStudentAddressTime,
  parseStudentAddressRouteQuery,
  validateStudentAddressForm,
} from '@/views/student/student-address'

const currentDir = dirname(fileURLToPath(import.meta.url))
const addressViewSource = readFileSync(resolve(currentDir, 'StudentAddressView.vue'), 'utf8')

describe('student address helpers', () => {
  it('validates required address fields in Chinese', () => {
    expect(
      validateStudentAddressForm({
        campusName: '  ',
        buildingName: '5号宿舍',
        roomNo: '302',
        detailAddress: '',
        contactName: '张三',
        contactPhone: '13800000000',
      }),
    ).toBe('请先填写校区、楼栋、房间号、联系人和联系电话')
  })

  it('builds a normalized save payload for address submit', () => {
    expect(
      buildStudentAddressSavePayload({
        editingId: undefined,
        isDefaultChecked: true,
        form: {
          campusName: ' 南校区 ',
          buildingName: ' 5号宿舍 ',
          roomNo: ' 302 ',
          detailAddress: ' 靠近电梯 ',
          contactName: ' 张三 ',
          contactPhone: ' 13800000000 ',
        },
      }),
    ).toEqual({
      id: undefined,
      campusName: '南校区',
      buildingName: '5号宿舍',
      roomNo: '302',
      detailAddress: '靠近电梯',
      contactName: '张三',
      contactPhone: '13800000000',
      isDefault: 1,
    })
  })

  it('builds a Chinese preview and formatted time', () => {
    expect(
      buildStudentAddressPreview({
        campusName: '南校区',
        buildingName: '5号宿舍',
        roomNo: '302',
        detailAddress: '靠近电梯',
        contactName: '',
        contactPhone: '',
      }),
    ).toBe('南校区 / 5号宿舍 / 302 / 靠近电梯')

    expect(
      buildStudentAddressPreview({
        campusName: '',
        buildingName: '',
        roomNo: '',
        detailAddress: '',
        contactName: '',
        contactPhone: '',
      }),
    ).toBe('地址预览会显示在这里')

    expect(formatStudentAddressTime('2026-04-08T10:15:30')).toBe('2026-04-08 10:15')
  })

  it('builds and parses checkout return queries for the address page', () => {
    expect(buildStudentAddressRouteQuery({ returnToCheckout: true, openCreate: true })).toEqual({
      returnTo: 'checkout',
      openCreate: '1',
    })

    expect(parseStudentAddressRouteQuery({ returnTo: 'checkout', openCreate: '1' })).toEqual({
      returnToCheckout: true,
      openCreate: true,
    })

    expect(parseStudentAddressRouteQuery({})).toEqual({
      returnToCheckout: false,
      openCreate: false,
    })
  })
})

describe('StudentAddressView copy', () => {
  it('keeps the address page copy in Chinese', () => {
    expect(addressViewSource).toContain('地址管理')
    expect(addressViewSource).toContain('新增地址')
    expect(addressViewSource).toContain('默认地址')
    expect(addressViewSource).toContain('保存')
    expect(addressViewSource).not.toContain('Address Book')
    expect(addressViewSource).not.toContain('New address')
  })
})
