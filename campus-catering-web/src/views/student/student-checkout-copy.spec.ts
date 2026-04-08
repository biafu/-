import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const checkoutViewSource = readFileSync(resolve(currentDir, 'StudentCheckoutView.vue'), 'utf8')
const addressCardSource = readFileSync(resolve(currentDir, '../../components/student/StudentCheckoutAddressCard.vue'), 'utf8')

describe('student checkout copy', () => {
  it('keeps checkout page strings in Chinese', () => {
    expect(checkoutViewSource).toContain('确认订单')
    expect(checkoutViewSource).toContain('返回购物车')
    expect(checkoutViewSource).toContain('切换地址')
    expect(checkoutViewSource).toContain('优惠券')
    expect(checkoutViewSource).toContain('提交订单')
  })

  it('keeps checkout address card strings in Chinese', () => {
    expect(addressCardSource).toContain('收货地址')
    expect(addressCardSource).toContain('管理地址')
    expect(addressCardSource).toContain('去新增地址')
  })
})
