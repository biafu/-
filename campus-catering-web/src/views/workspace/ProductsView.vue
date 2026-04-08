<template>
  <section class="head glass-panel">
    <div>
      <h1 class="cc-section-title">商品中心</h1>
    </div>
    <div class="head-actions">
      <el-button plain @click="categoryDialogVisible = true">新增分类</el-button>
      <el-button type="primary" round @click="openCreate">新增商品</el-button>
      <el-button round @click="loadPageData">刷新</el-button>
    </div>
  </section>

  <section class="grid">
    <el-skeleton v-if="loading" :rows="6" animated />

    <template v-else>
      <RevealOnScroll v-for="item in products" :key="item.spuId">
        <article class="cc-card card hover-lift">
          <div class="top">
            <div class="title-block">
              <h3>
                {{ item.productName }}
                <el-tag size="small" :type="item.productType === 2 ? 'warning' : 'success'" effect="plain" round>
                  {{ item.productType === 2 ? '套餐' : '单品' }}
                </el-tag>
                <el-tag v-if="item.skus.length > 1" size="small" type="warning" effect="plain" round>
                  多 SKU
                </el-tag>
              </h3>
              <div class="meta-tags">
                <el-tag v-for="sku in item.skus" :key="sku.skuId ?? sku.skuName" type="primary" effect="plain" round>
                  {{ sku.skuName }}
                </el-tag>
                <el-tag effect="plain" round>{{ categoryLabel(item.categoryId) }}</el-tag>
              </div>
            </div>
            <el-image v-if="item.imageUrl" :src="previewImageUrl(item.imageUrl)" fit="cover" class="thumb" />
          </div>

          <p>{{ item.description || '暂无商品描述' }}</p>
          <p v-if="item.comboSummary" class="combo-summary">套餐包含：{{ item.comboSummary }}</p>
          <p v-if="item.skus.length > 1" class="warning-text">
            多规格商品现在支持在编辑面板统一维护价格、库存和上下架状态。
          </p>

          <div class="bottom">
            <strong>{{ priceLabel(item) }}</strong>
            <span>{{ stockLabel(item) }}</span>
          </div>

          <div class="actions">
            <el-button size="small" @click="openEdit(item)">编辑</el-button>
            <el-button size="small" @click="editStock(item)">
              {{ item.skus.length > 1 ? '编辑 SKU' : '修改库存' }}
            </el-button>
            <el-button
              size="small"
              :type="item.saleStatus === 1 ? 'warning' : 'success'"
              @click="toggleShelf(item)"
            >
              {{ item.saleStatus === 1 ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="remove(item.spuId)">删除</el-button>
          </div>
        </article>
      </RevealOnScroll>

      <el-empty v-if="products.length === 0" description="暂时还没有商品，先新增一个吧" />
    </template>
  </section>

  <MerchantCategoryEditor
    v-model:visible="categoryDialogVisible"
    :submitting="submittingCategory"
    @submit="submitCategory"
  />

  <MerchantProductEditor
    v-model:visible="productDialogVisible"
    :mode="editorMode"
    :categories="categories"
    :combo-candidates="comboCandidates"
    :initial-product="activeProduct"
    :submitting="submittingProduct"
    @submit="submitProduct"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RevealOnScroll from '@/components/RevealOnScroll.vue'
import MerchantCategoryEditor from '@/components/workspace/MerchantCategoryEditor.vue'
import MerchantProductEditor from '@/components/workspace/MerchantProductEditor.vue'
import {
  deleteMerchantProduct,
  fetchMerchantCategories,
  fetchMerchantProducts,
  offShelfMerchantProduct,
  onShelfMerchantProduct,
  saveMerchantCategory,
  saveMerchantProduct,
  updateMerchantProductBasic,
  updateMerchantProductEditor,
  updateMerchantSku,
  updateMerchantSkuStock,
  type MerchantCategoryResponse,
} from '@/api/merchant'
import type { ProductViewResponse } from '@/api/student'
import { resolveAssetUrl } from '@/utils/assets'
import {
  buildComboUpdatePayload,
  buildMerchantCategoryOptions,
  buildMerchantComboCandidates,
  buildMerchantProductBasicUpdateRequest,
  buildMerchantProductEditorUpdateRequest,
  buildMerchantProductSaveRequest,
  buildMerchantSkuUpdateRequest,
  type MerchantProductEditorSubmitPayload,
  type MerchantSkuDraft,
  summarizeComboItems,
} from '@/views/workspace/merchant-product-editor'

type ProductCard = {
  spuId: number
  categoryId: number
  imageUrl?: string
  sortNo: number
  saleStatus: number
  productName: string
  productType: number
  comboDesc?: string
  comboItems: Array<{ skuId: number; quantity: number }>
  comboSummary?: string
  description?: string
  skus: MerchantSkuDraft[]
}

const loading = ref(false)
const submittingCategory = ref(false)
const submittingProduct = ref(false)
const categoryDialogVisible = ref(false)
const productDialogVisible = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const activeProduct = ref<ProductCard | null>(null)
const rawProducts = ref<ProductViewResponse[]>([])
const products = ref<ProductCard[]>([])
const categoryOptions = ref<MerchantCategoryResponse[]>([])

const categories = computed(() => buildMerchantCategoryOptions(rawProducts.value, categoryOptions.value))
const comboCandidates = computed(() => buildMerchantComboCandidates(rawProducts.value))
const previewImageUrl = (imageUrl?: string) => resolveAssetUrl(imageUrl)

const categoryLabel = (categoryId: number) =>
  categories.value.find((item) => item.id === categoryId)?.categoryName ?? `分类 ${categoryId}`

const priceLabel = (item: ProductCard) => {
  const prices = item.skus.map((sku) => Number(sku.price ?? 0))
  const minPrice = Math.min(...prices)
  if (item.productType === 1 && item.skus.length > 1) {
    return `${minPrice.toFixed(2)} 元起`
  }
  return `${minPrice.toFixed(2)} 元`
}

const stockLabel = (item: ProductCard) => {
  const totalStock = item.skus.reduce((sum, sku) => sum + Number(sku.stock ?? 0), 0)
  return item.skus.length > 1 ? `总库存 ${totalStock}` : `库存 ${totalStock}`
}

const normalizeProductCard = (product: ProductViewResponse): ProductCard | null => {
  if (!product.skus.length) {
    return null
  }

  return {
    spuId: product.spuId,
    categoryId: product.categoryId,
    imageUrl: product.imageUrl,
    sortNo: Number(product.sortNo ?? 0),
    saleStatus: Number(product.saleStatus ?? 1),
    productName: product.productName,
    productType: Number(product.productType ?? 1),
    comboDesc: product.comboDesc,
    comboItems: product.comboItems?.map((item) => ({ skuId: item.skuId, quantity: item.quantity })) ?? [],
    comboSummary: summarizeComboItems(product.comboItems),
    description: product.description,
    skus: product.skus.map((sku) => ({
      skuId: sku.skuId,
      skuName: sku.skuName,
      price: Number(sku.price ?? 0),
      stock: Number(sku.stock ?? 0),
      skuStatus: Number(sku.status ?? 1),
    })),
  }
}

const loadPageData = async () => {
  loading.value = true
  try {
    const [categoriesResponse, productsResponse] = await Promise.all([
      fetchMerchantCategories(),
      fetchMerchantProducts(),
    ])
    categoryOptions.value = categoriesResponse
    rawProducts.value = productsResponse
    products.value = productsResponse
      .map(normalizeProductCard)
      .filter((item): item is ProductCard => item !== null)
      .sort((left, right) => left.sortNo - right.sortNo || left.spuId - right.spuId)
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载商品失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editorMode.value = 'create'
  activeProduct.value = null
  productDialogVisible.value = true
}

const openEdit = (item: ProductCard) => {
  editorMode.value = 'edit'
  activeProduct.value = {
    ...item,
    comboItems: item.comboItems.map((comboItem) => ({ ...comboItem })),
    skus: item.skus.map((sku) => ({ ...sku })),
  }
  productDialogVisible.value = true
}

const submitCategory = async (payload: { categoryName: string; sortNo?: number }) => {
  submittingCategory.value = true
  try {
    await saveMerchantCategory(payload)
    categoryDialogVisible.value = false
    ElMessage.success('分类创建成功')
    await loadPageData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '分类创建失败'
    ElMessage.error(message)
  } finally {
    submittingCategory.value = false
  }
}

const submitProduct = async (payload: MerchantProductEditorSubmitPayload) => {
  submittingProduct.value = true
  try {
    if (editorMode.value === 'create') {
      await saveMerchantProduct(buildMerchantProductSaveRequest(payload))
      ElMessage.success('商品已创建')
    } else if (activeProduct.value) {
      if (activeProduct.value.productType === 2) {
        const comboItems =
          JSON.stringify(payload.comboItems) !== JSON.stringify(activeProduct.value.comboItems)
            ? buildComboUpdatePayload(payload).comboItems
            : undefined
        await updateMerchantProductEditor(
          activeProduct.value.spuId,
          buildMerchantProductEditorUpdateRequest({
            activeProduct: activeProduct.value,
            payload,
            comboItems,
          }),
        )
      } else {
        await updateMerchantProductBasic(activeProduct.value.spuId, buildMerchantProductBasicUpdateRequest(payload))
        await Promise.all(
          payload.skus.map(async (sku) => {
            if (!sku.skuId) {
              throw new Error('编辑已有商品时缺少 SKU 标识')
            }
            await updateMerchantSku(sku.skuId, buildMerchantSkuUpdateRequest(sku))
            await updateMerchantSkuStock(sku.skuId, { stock: sku.stock })
          }),
        )
      }
      ElMessage.success('商品已更新')
    }
    productDialogVisible.value = false
    await loadPageData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '商品保存失败'
    ElMessage.error(message)
  } finally {
    submittingProduct.value = false
  }
}

const editStock = async (item: ProductCard) => {
  if (item.skus.length > 1) {
    openEdit(item)
    ElMessage.info('多规格商品请在编辑面板中统一修改库存')
    return
  }

  try {
    const targetSku = item.skus[0]
    const { value } = await ElMessageBox.prompt('请输入库存数量', '修改库存', {
      inputValue: String(targetSku.stock),
      inputPattern: /^\d+$/,
      inputErrorMessage: '库存必须是非负整数',
    })
    if (!targetSku.skuId) {
      throw new Error('缺少 SKU 标识，无法修改库存')
    }
    await updateMerchantSkuStock(targetSku.skuId, { stock: Number(value) })
    ElMessage.success('库存已更新')
    await loadPageData()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    const message = error instanceof Error ? error.message : '库存更新失败'
    ElMessage.error(message)
  }
}

const toggleShelf = async (item: ProductCard) => {
  try {
    if (item.saleStatus === 1) {
      await offShelfMerchantProduct(item.spuId)
    } else {
      await onShelfMerchantProduct(item.spuId)
    }
    ElMessage.success(item.saleStatus === 1 ? '商品已下架' : '商品已上架')
    await loadPageData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(message)
  }
}

const remove = async (spuId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？删除后无法恢复。', '删除确认', {
      type: 'warning',
    })
    await deleteMerchantProduct(spuId)
    ElMessage.success('商品已删除')
    await loadPageData()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    const message = error instanceof Error ? error.message : '删除失败'
    ElMessage.error(message)
  }
}

onMounted(loadPageData)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.head-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.grid {
  margin-top: 16px;
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.card {
  padding: 16px;
}

.top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.title-block {
  min-width: 0;
}

.title-block h3 {
  margin: 0;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.meta-tags {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.thumb {
  width: 84px;
  height: 84px;
  border-radius: 18px;
  overflow: hidden;
  flex-shrink: 0;
}

.card p {
  margin: 10px 0;
  color: var(--cc-color-text-secondary);
}

.combo-summary {
  margin-top: -2px;
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

.warning-text {
  color: #b42318;
  font-size: 12px;
  line-height: 1.6;
}

.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.bottom strong {
  font-size: 24px;
  color: var(--cc-color-primary);
}

.bottom span {
  color: var(--cc-color-text-tertiary);
  font-size: 13px;
}

.actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .head {
    flex-direction: column;
    align-items: flex-start;
  }

  .grid {
    grid-template-columns: 1fr;
  }

  .top {
    flex-direction: column;
  }
}
</style>
