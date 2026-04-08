<template>
  <el-drawer
    :model-value="visible"
    :with-header="false"
    size="760px"
    destroy-on-close
    class="merchant-product-drawer"
    @close="emit('update:visible', false)"
  >
    <div class="drawer-shell">
      <header class="drawer-header">
        <div>
          <p class="drawer-kicker">{{ mode === 'create' ? '新增商品' : '编辑商品' }}</p>
          <h2 class="drawer-title">{{ mode === 'create' ? '创建商品档案' : '维护商品档案' }}</h2>
        </div>
        <p class="drawer-description">编辑商品基础信息、SKU、库存和组合内容，底部操作区会始终保持可见。</p>
      </header>

      <div class="drawer-body">
        <el-form label-position="top" class="editor-form">
          <div class="editor-sections">
            <section class="editor-section editor-section--basic">
              <div class="section-heading">
                <div>
                  <h3>基础信息</h3>
                  <p>选择商品类型、分类和商品展示信息。</p>
                </div>
              </div>

              <el-form-item label="商品类型">
                <el-segmented
                  v-model="form.productType"
                  :options="[
                    { label: '单品', value: 1 },
                    { label: '套餐', value: 2 },
                  ]"
                  :disabled="mode === 'edit'"
                />
              </el-form-item>

              <div class="form-grid">
                <el-form-item label="商品分类">
                  <el-select v-model="form.categoryId" placeholder="请选择商品分类">
                    <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
                  </el-select>
                </el-form-item>
                <el-form-item label="排序值">
                  <el-input-number v-model="form.sortNo" :min="0" :step="1" />
                </el-form-item>
              </div>

              <el-form-item label="商品名称">
                <el-input v-model="form.productName" maxlength="32" show-word-limit />
              </el-form-item>

              <el-form-item label="图片链接">
                <div class="upload-row">
                  <el-input v-model="form.imageUrl" placeholder="可直接粘贴图片地址，或在右侧上传" />
                  <input ref="fileInputRef" type="file" accept="image/*" @change="uploadImage" />
                </div>
              </el-form-item>

              <el-form-item label="商品描述">
                <el-input v-model="form.description" type="textarea" :rows="3" maxlength="160" show-word-limit />
              </el-form-item>
            </section>

            <section class="editor-section editor-section--sku">
              <div class="section-heading">
                <div>
                  <h3>SKU 信息</h3>
                  <p>维护规格名称、价格、库存和上下架状态。单品创建时支持一次录入多个 SKU。</p>
                </div>
                <el-button v-if="canManageSkuRows" plain @click="appendSkuRow">添加 SKU</el-button>
              </div>

              <div class="sku-list">
                <section v-for="(sku, index) in form.skus" :key="sku.skuId ?? index" class="sku-card">
                  <div class="sku-card-head">
                    <strong>SKU {{ index + 1 }}</strong>
                    <el-button v-if="canManageSkuRows && form.skus.length > 1" type="danger" plain @click="removeSkuRow(index)">
                      删除
                    </el-button>
                  </div>

                  <div class="form-grid">
                    <el-form-item label="规格名称">
                      <el-input v-model="sku.skuName" maxlength="32" show-word-limit />
                    </el-form-item>
                    <el-form-item label="库存">
                      <el-input-number v-model="sku.stock" :min="0" :step="1" />
                    </el-form-item>
                  </div>

                  <div class="form-grid">
                    <el-form-item label="价格（元）">
                      <el-input-number v-model="sku.price" :min="0.01" :step="1" :precision="2" />
                    </el-form-item>
                    <el-form-item v-if="mode === 'edit'" label="规格状态">
                      <el-radio-group v-model="sku.skuStatus">
                        <el-radio :label="1">上架</el-radio>
                        <el-radio :label="0">下架</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </div>
                </section>
              </div>
            </section>

            <section v-if="form.productType === 2" class="editor-section editor-section--combo">
              <div class="section-heading">
                <div>
                  <h3>套餐组成</h3>
                  <p>填写套餐描述，并在这里维护组合内容。</p>
                </div>
              </div>

              <el-form-item label="套餐描述">
                <el-input v-model="form.comboDesc" maxlength="80" placeholder="例如：鸡腿饭 + 饮料 + 小食" />
              </el-form-item>

              <MerchantComboItemsEditor v-model="form.comboItems" :candidates="comboCandidates" :show-header="false" />
            </section>
          </div>
        </el-form>
      </div>

      <footer class="drawer-footer">
        <el-button @click="emit('update:visible', false)">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </footer>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadMerchantImage } from '@/api/merchant'
import MerchantComboItemsEditor from '@/components/workspace/MerchantComboItemsEditor.vue'
import type {
  MerchantCategoryOption,
  MerchantComboCandidate,
  MerchantProductEditorSubmitPayload,
  MerchantSkuDraft,
} from '@/views/workspace/merchant-product-editor'

type InitialProduct = {
  categoryId: number
  productName: string
  productType: number
  imageUrl?: string
  description?: string
  sortNo: number
  comboDesc?: string
  comboItems?: Array<{ skuId: number; quantity: number }>
  skus: MerchantSkuDraft[]
}

const props = defineProps<{
  visible: boolean
  mode: 'create' | 'edit'
  categories: MerchantCategoryOption[]
  comboCandidates: MerchantComboCandidate[]
  initialProduct?: InitialProduct | null
  submitting?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [payload: MerchantProductEditorSubmitPayload]
}>()

const fileInputRef = ref<HTMLInputElement>()

const createEmptySku = (): MerchantSkuDraft => ({
  skuId: undefined,
  skuName: '',
  price: 0,
  stock: 0,
  skuStatus: 1,
})

const form = reactive<MerchantProductEditorSubmitPayload>({
  categoryId: 0,
  productName: '',
  productType: 1,
  imageUrl: '',
  description: '',
  sortNo: 0,
  comboDesc: '',
  comboItems: [],
  skus: [createEmptySku()],
})

const canManageSkuRows = computed(() => props.mode === 'create' && form.productType === 1)

const resetForm = () => {
  form.categoryId = props.categories[0]?.id ?? 0
  form.productName = ''
  form.productType = 1
  form.imageUrl = ''
  form.description = ''
  form.sortNo = 0
  form.comboDesc = ''
  form.comboItems = []
  form.skus = [createEmptySku()]
}

watch(
  () => [props.visible, props.initialProduct, props.categories] as const,
  () => {
    if (!props.visible) {
      return
    }

    if (!props.initialProduct) {
      resetForm()
      return
    }

    form.categoryId = props.initialProduct.categoryId
    form.productName = props.initialProduct.productName
    form.productType = props.initialProduct.productType
    form.imageUrl = props.initialProduct.imageUrl ?? ''
    form.description = props.initialProduct.description ?? ''
    form.sortNo = props.initialProduct.sortNo
    form.comboDesc = props.initialProduct.comboDesc ?? ''
    form.comboItems = props.initialProduct.comboItems?.map((item) => ({ ...item })) ?? []
    form.skus = props.initialProduct.skus.map((item) => ({ ...item }))
  },
  { immediate: true },
)

watch(
  () => form.productType,
  (productType) => {
    if (productType === 2) {
      form.skus = [form.skus[0] ? { ...form.skus[0] } : createEmptySku()]
      return
    }

    if (form.skus.length === 0) {
      form.skus = [createEmptySku()]
    }
  },
)

const appendSkuRow = () => {
  form.skus = [...form.skus, createEmptySku()]
}

const removeSkuRow = (index: number) => {
  form.skus = form.skus.filter((_, currentIndex) => currentIndex !== index)
}

const uploadImage = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) {
    return
  }
  try {
    form.imageUrl = await uploadMerchantImage(file)
    ElMessage.success('图片上传成功')
  } catch (error) {
    const message = error instanceof Error ? error.message : '图片上传失败'
    ElMessage.error(message)
  } finally {
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

const submit = () => {
  if (!form.categoryId || !form.productName.trim()) {
    ElMessage.warning('请先填写分类和商品名称')
    return
  }
  if (form.skus.length === 0 || form.skus.some((item) => !item.skuName.trim())) {
    ElMessage.warning('请先补全所有 SKU 名称')
    return
  }
  if (form.skus.some((item) => item.price <= 0)) {
    ElMessage.warning('每个 SKU 的价格必须大于 0')
    return
  }
  if (form.skus.some((item) => item.stock < 0)) {
    ElMessage.warning('SKU 库存不能为负数')
    return
  }
  if (form.productType === 2) {
    if (form.comboItems.length === 0) {
      ElMessage.warning('套餐至少需要一个组成项')
      return
    }
    if (form.comboItems.some((item) => !item.skuId || item.quantity < 1)) {
      ElMessage.warning('请先补全套餐组成')
      return
    }
  }

  emit('submit', {
    categoryId: form.categoryId,
    productName: form.productName.trim(),
    productType: form.productType,
    imageUrl: form.imageUrl?.trim() || undefined,
    description: form.description?.trim() || undefined,
    sortNo: form.sortNo,
    comboDesc: form.comboDesc?.trim() || undefined,
    comboItems: form.comboItems.map((item) => ({ ...item })),
    skus: form.skus.map((item) => ({
      skuId: item.skuId,
      skuName: item.skuName.trim(),
      price: item.price,
      stock: item.stock,
      skuStatus: item.skuStatus,
    })),
  })
}
</script>

<style scoped>
.merchant-product-drawer :deep(.el-drawer__body) {
  height: 100%;
  padding: 0;
}

.drawer-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 24px 24px 20px;
  box-sizing: border-box;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.drawer-kicker {
  margin: 0 0 6px;
  color: var(--cc-color-primary);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.drawer-title {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
}

.drawer-description {
  max-width: 320px;
  margin: 0;
  color: var(--cc-color-text-secondary);
  line-height: 1.6;
}

.drawer-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding-right: 6px;
}

.editor-sections {
  display: grid;
  gap: 20px;
}

.editor-section {
  display: grid;
  gap: 14px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.section-heading h3 {
  margin: 0;
  font-size: 16px;
  line-height: 1.3;
}

.section-heading p {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
  line-height: 1.5;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.upload-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
}

.sku-list {
  display: grid;
  gap: 14px;
}

.sku-card {
  display: grid;
  gap: 14px;
  padding: 14px;
  border-radius: 16px;
  border: 1px solid var(--cc-border-color, var(--el-border-color-lighter));
  background: color-mix(in srgb, #fff 88%, #f5f1e5 12%);
}

.sku-card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.editor-section--combo :deep(.combo-editor) {
  border-top: 0;
  padding-top: 0;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 16px;
  margin-top: 16px;
  border-top: 1px solid var(--cc-border-color, var(--el-border-color-lighter));
  background: var(--cc-color-bg, var(--el-bg-color));
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .drawer-shell {
    padding: 20px 16px 16px;
  }

  .drawer-header,
  .section-heading {
    flex-direction: column;
  }

  .form-grid,
  .upload-row {
    grid-template-columns: 1fr;
  }

  .drawer-footer {
    justify-content: stretch;
  }

  .drawer-footer .el-button {
    flex: 1;
  }
}
</style>
