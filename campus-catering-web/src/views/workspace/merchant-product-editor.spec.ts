import { renderToString } from '@vue/server-renderer'
import { describe, expect, it } from 'vitest'
import { createSSRApp, h, type Component, type SetupContext } from 'vue'
import MerchantProductEditor from '@/components/workspace/MerchantProductEditor.vue'
import type { ProductViewResponse } from '@/api/student'
import {
  buildComboUpdatePayload,
  buildMerchantCategoryOptions,
  buildMerchantComboCandidates,
  buildMerchantProductBasicUpdateRequest,
  buildMerchantProductEditorUpdateRequest,
  buildMerchantProductSaveRequest,
  buildMerchantSkuUpdateRequest,
  isMerchantProductEditable,
  summarizeComboItems,
} from '@/views/workspace/merchant-product-editor'

const sampleProducts: ProductViewResponse[] = [
  {
    spuId: 11,
    categoryId: 5,
    productName: '招牌鸡腿饭',
    productType: 1,
    imageUrl: '/a.png',
    description: '热卖单品',
    saleStatus: 1,
    sortNo: 3,
    skus: [
      {
        skuId: 101,
        skuName: '标准份',
        price: 18,
        stock: 25,
        status: 1,
      },
    ],
  },
  {
    spuId: 12,
    categoryId: 7,
    productName: '双人套餐',
    productType: 2,
    imageUrl: '/b.png',
    description: '固定套餐',
    comboDesc: '鸡腿饭 + 可乐',
    saleStatus: 1,
    sortNo: 8,
    skus: [
      {
        skuId: 201,
        skuName: '套餐价',
        price: 29,
        stock: 10,
        status: 1,
      },
    ],
    comboItems: [
      {
        skuId: 101,
        spuName: '招牌鸡腿饭',
        skuName: '标准份',
        quantity: 1,
        sortNo: 1,
      },
    ],
  },
]

const slotStub = (tag: string): Component => ({
  inheritAttrs: false,
  setup(_: any, { attrs, slots }: SetupContext) {
    return () => h(tag, attrs, slots.default?.())
  },
})

const registerEditorShellStubs = (app: ReturnType<typeof createSSRApp>) => {
  app.component('el-drawer', {
    inheritAttrs: false,
    setup(_: any, { attrs, slots }: SetupContext) {
      return () => h('div', { ...attrs, 'data-shell': 'drawer' }, slots.default?.())
    },
  })
  app.component('el-button', slotStub('button'))
  app.component('el-form', slotStub('form'))
  app.component('el-form-item', slotStub('section'))
  app.component('el-segmented', slotStub('div'))
  app.component('el-select', slotStub('div'))
  app.component('el-option', slotStub('div'))
  app.component('el-input-number', slotStub('div'))
  app.component('el-input', slotStub('div'))
  app.component('el-radio-group', slotStub('div'))
  app.component('el-radio', slotStub('div'))
  app.component('el-empty', slotStub('div'))
  app.component('el-checkbox', slotStub('div'))
}

describe('merchant product editor helpers', () => {
  it('builds fallback category options from products when no category list is available', () => {
    expect(
      buildMerchantCategoryOptions([
        ...sampleProducts,
        {
          ...sampleProducts[0],
          spuId: 13,
          sortNo: 1,
        },
      ]),
    ).toEqual([
      { id: 5, categoryName: '分类 5', sortNo: 0 },
      { id: 7, categoryName: '分类 7', sortNo: 0 },
    ])
  })

  it('prefers fetched category labels when they are available', () => {
    expect(
      buildMerchantCategoryOptions(sampleProducts, [
        { id: 5, categoryName: '主食', sortNo: 2 },
        { id: 7, categoryName: '套餐', sortNo: 1 },
      ]),
    ).toEqual([
      { id: 7, categoryName: '套餐', sortNo: 1 },
      { id: 5, categoryName: '主食', sortNo: 2 },
    ])
  })

  it('only exposes active single-product skus as combo candidates', () => {
    expect(buildMerchantComboCandidates(sampleProducts)).toEqual([
      {
        skuId: 101,
        productName: '招牌鸡腿饭',
        skuName: '标准份',
      },
    ])
  })

  it('keeps referenced combo items available as fallback candidates', () => {
    expect(
      buildMerchantComboCandidates([
        {
          ...sampleProducts[0],
          skus: [
            {
              skuId: 101,
              skuName: '标准份',
              price: 18,
              stock: 25,
              status: 0,
            },
          ],
        },
        sampleProducts[1],
      ]),
    ).toEqual([
      {
        skuId: 101,
        productName: '招牌鸡腿饭',
        skuName: '标准份',
      },
    ])
  })

  it('maps combo items into update payload sort order', () => {
    expect(
      buildComboUpdatePayload({
        comboDesc: '新套餐',
        comboItems: [
          { skuId: 101, quantity: 1 },
          { skuId: 102, quantity: 2 },
        ],
      }),
    ).toEqual({
      comboDesc: '新套餐',
      comboItems: [
        { skuId: 101, quantity: 1, sortNo: 1 },
        { skuId: 102, quantity: 2, sortNo: 2 },
      ],
    })
  })

  it('summarizes combo items for product cards', () => {
    expect(summarizeComboItems(sampleProducts[1].comboItems)).toBe('招牌鸡腿饭x1')
  })

  it('allows multi-sku products to enter the editor', () => {
    expect(
      isMerchantProductEditable({
        ...sampleProducts[0],
        skus: [
          ...sampleProducts[0].skus,
          {
            skuId: 102,
            skuName: '大份',
            price: 22,
            stock: 12,
            status: 1,
          },
        ],
      }),
    ).toBe(true)
    expect(isMerchantProductEditable(sampleProducts[0])).toBe(true)
  })

  it('builds a product save request with multiple skus for single products', () => {
    expect(
      buildMerchantProductSaveRequest({
        categoryId: 5,
        productName: '招牌鸡腿饭',
        productType: 1,
        imageUrl: ' /a.png ',
        description: ' 热卖单品 ',
        sortNo: 3,
        comboDesc: undefined,
        comboItems: [],
        skus: [
          { skuId: undefined, skuName: '标准份', price: 18, stock: 25, skuStatus: 1 },
          { skuId: undefined, skuName: '大份', price: 22, stock: 12, skuStatus: 1 },
        ],
      }),
    ).toEqual({
      categoryId: 5,
      productName: '招牌鸡腿饭',
      productType: 1,
      imageUrl: '/a.png',
      description: '热卖单品',
      sortNo: 3,
      comboDesc: undefined,
      comboItems: undefined,
      skus: [
        { skuName: '标准份', price: 18, stock: 25 },
        { skuName: '大份', price: 22, stock: 12 },
      ],
    })
  })

  it('builds a basic update request for single-product editing', () => {
    expect(
      buildMerchantProductBasicUpdateRequest({
        categoryId: 5,
        productName: ' 招牌鸡腿饭 ',
        productType: 1,
        imageUrl: ' /a.png ',
        description: ' 热卖单品 ',
        sortNo: 3,
        comboDesc: undefined,
        comboItems: [],
        skus: [{ skuId: 101, skuName: '标准份', price: 18, stock: 25, skuStatus: 1 }],
      }),
    ).toEqual({
      categoryId: 5,
      productName: '招牌鸡腿饭',
      imageUrl: '/a.png',
      description: '热卖单品',
      sortNo: 3,
    })
  })

  it('builds an sku update request for existing skus', () => {
    expect(
      buildMerchantSkuUpdateRequest({
        skuId: 101,
        skuName: ' 标准份 ',
        price: 18,
        stock: 25,
        skuStatus: 0,
      }),
    ).toEqual({
      skuName: '标准份',
      price: 18,
      status: 0,
    })
  })

  it('preserves cleared optional fields in the combo edit update request', () => {
    expect(
      buildMerchantProductEditorUpdateRequest({
        activeProduct: { productType: 2, comboDesc: '原套餐描述' },
        payload: {
          categoryId: 7,
          productName: '双人套餐',
          productType: 2,
          imageUrl: undefined,
          description: undefined,
          sortNo: 8,
          comboDesc: undefined,
          comboItems: [],
          skus: [{ skuId: 201, skuName: '套餐价', price: 29, stock: 10, skuStatus: 1 }],
        },
      }),
    ).toEqual({
      categoryId: 7,
      productName: '双人套餐',
      imageUrl: '',
      description: '',
      sortNo: 8,
      skuId: 201,
      skuName: '套餐价',
      price: 29,
      stock: 10,
      skuStatus: 1,
      comboDesc: '',
      comboItems: undefined,
    })
  })

  it('renders the merchant product editor as a drawer shell with persistent actions', async () => {
    const app = createSSRApp(MerchantProductEditor, {
      visible: true,
      mode: 'create',
      categories: [{ id: 5, categoryName: '主食', sortNo: 1 }],
      comboCandidates: [],
      submitting: false,
    })
    registerEditorShellStubs(app)
    const rendered = await renderToString(app)
    const bodyIndex = rendered.indexOf('class="drawer-body"')
    const footerIndex = rendered.indexOf('class="drawer-footer"')

    expect(rendered).toContain('merchant-product-drawer')
    expect(rendered).toContain('创建商品档案')
    expect(rendered).toContain('编辑商品基础信息、SKU、库存和组合内容')
    expect(rendered).toContain('class="drawer-body"')
    expect(rendered).toContain('class="drawer-footer"')
    expect(rendered).toContain('destroy-on-close')
    expect(bodyIndex).toBeGreaterThan(-1)
    expect(footerIndex).toBeGreaterThan(bodyIndex)
    expect(rendered).toContain('取消')
    expect(rendered).toContain('保存')
    expect(rendered).toContain('添加 SKU')
  })

  it('keeps combo editing visible in drawer mode for combo products', async () => {
    const app = createSSRApp(MerchantProductEditor, {
      visible: true,
      mode: 'edit',
      categories: [{ id: 7, categoryName: '套餐', sortNo: 1 }],
      comboCandidates: sampleProducts[0].skus.map((sku) => ({
        skuId: sku.skuId,
        productName: sampleProducts[0].productName,
        skuName: sku.skuName,
      })),
      initialProduct: {
        categoryId: 7,
        productName: '双人套餐',
        productType: 2,
        imageUrl: '/b.png',
        description: '固定套餐',
        sortNo: 8,
        comboDesc: '鸡腿饭 + 可乐',
        comboItems: [{ skuId: 101, quantity: 1 }],
        skus: [{ skuId: 201, skuName: '套餐价', price: 29, stock: 10, skuStatus: 1 }],
      },
      submitting: false,
    })
    registerEditorShellStubs(app)

    const rendered = await renderToString(app)

    expect(rendered).toContain('class="editor-section editor-section--basic"')
    expect(rendered).toContain('class="editor-section editor-section--sku"')
    expect(rendered).toContain('class="editor-section editor-section--combo"')
    expect(rendered).toContain('section-heading')
    expect(rendered).toContain('套餐组成')
    expect(rendered).toContain('combo-editor')
    expect(rendered).not.toContain('combo-head')
  })

  it('keeps the add-combo action visible in drawer mode when combo items are empty', async () => {
    const app = createSSRApp(MerchantProductEditor, {
      visible: true,
      mode: 'create',
      categories: [{ id: 7, categoryName: '套餐', sortNo: 1 }],
      comboCandidates: sampleProducts[0].skus.map((sku) => ({
        skuId: sku.skuId,
        productName: sampleProducts[0].productName,
        skuName: sku.skuName,
      })),
      initialProduct: {
        categoryId: 7,
        productName: '双人套餐',
        productType: 2,
        imageUrl: '',
        description: '',
        sortNo: 8,
        comboDesc: '',
        comboItems: [],
        skus: [{ skuId: 201, skuName: '套餐价', price: 29, stock: 10, skuStatus: 1 }],
      },
      submitting: false,
    })
    registerEditorShellStubs(app)

    const rendered = await renderToString(app)

    expect(rendered).toContain('combo-toolbar')
    expect(rendered).toContain('添加组成项')
    expect(rendered).toContain('当前还没有套餐组成项')
  })
})
