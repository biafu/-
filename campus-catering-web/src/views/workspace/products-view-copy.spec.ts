import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { describe, expect, it } from 'vitest'

const currentDir = dirname(fileURLToPath(import.meta.url))
const source = readFileSync(resolve(currentDir, 'ProductsView.vue'), 'utf8')

describe('ProductsView copy', () => {
  it('keeps the merchant product center page copy in Chinese', () => {
    expect(source).toContain('商品中心')
    expect(source).toContain('新增分类')
    expect(source).toContain('新增商品')
    expect(source).toContain('刷新')
    expect(source).toContain('单品')
    expect(source).toContain('套餐')
    expect(source).toContain('暂无商品描述')
    expect(source).toContain('库存')
    expect(source).toContain('编辑')
    expect(source).toContain('上架')
    expect(source).toContain('下架')
    expect(source).toContain('删除')
    expect(source).toContain('暂时还没有商品，先新增一个吧')
    expect(source).not.toContain('鍟嗗搧')
  })
})
