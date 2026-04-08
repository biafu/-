# Merchant Product Editor Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the merchant-side product management loop by adding category creation plus unified single-item and fixed-combo create/edit flows.

**Architecture:** Keep the existing merchant product center as the page shell and add three focused workspace components: a category dialog, a unified product editor, and a combo-items editor. Reuse the current list/grid actions, extend the merchant API typings to match backend save/update contracts, and drive the new editor from page-level product/category data so create and edit both use the same flow.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Vite, existing merchant API module, existing Spring Boot merchant product endpoints

---

## File Map

### Frontend

- Modify: `campus-catering-web/src/views/workspace/ProductsView.vue`
- Modify: `campus-catering-web/src/api/merchant.ts`
- Create: `campus-catering-web/src/components/workspace/MerchantCategoryEditor.vue`
- Create: `campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue`
- Create: `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`

## Task 1: Extend merchant API typings and requests

**Files:**
- Modify: `campus-catering-web/src/api/merchant.ts`

- [ ] **Step 1: Add the missing merchant-side request and response types**

```ts
export type MerchantCategoryResponse = {
  id: number
  categoryName: string
  sortNo: number
}

export type CategorySaveRequest = {
  id?: number
  categoryName: string
  sortNo?: number
}

export type ComboItemSaveRequest = {
  skuId: number
  quantity: number
  sortNo?: number
}

export type ProductSkuSaveRequest = {
  skuName: string
  price: number
  stock: number
  status: number
}

export type ProductSaveRequest = {
  id?: number
  categoryId: number
  productName: string
  productType: number
  imageUrl?: string
  description?: string
  sortNo?: number
  skus: ProductSkuSaveRequest[]
  comboItems?: ComboItemSaveRequest[]
}
```

- [ ] **Step 2: Add the missing merchant category and product save requests**

```ts
export function saveMerchantCategory(payload: CategorySaveRequest) {
  return request<number>('/api/merchant/category/save', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function saveMerchantProduct(payload: ProductSaveRequest) {
  return request<number>('/api/merchant/product/save', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}
```

- [ ] **Step 3: Reuse the existing merchant product list shape to expose category options**

```ts
// no new endpoint required yet; categories can be derived from current merchant products
// the page layer will normalize list data into distinct category options
```

- [ ] **Step 4: Run frontend build to verify the API file still type-checks**

Run: `npm run build`

Expected: PASS, with at most the existing large chunk warning.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/api/merchant.ts
git commit -m "feat: extend merchant product api contracts"
```

## Task 2: Add focused merchant editor components

**Files:**
- Create: `campus-catering-web/src/components/workspace/MerchantCategoryEditor.vue`
- Create: `campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue`
- Create: `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`

- [ ] **Step 1: Create the category dialog component**

```vue
<template>
  <el-dialog :model-value="visible" title="新增分类" width="460px" @close="emit('update:visible', false)">
    <el-form label-position="top">
      <el-form-item label="分类名称">
        <el-input v-model="form.categoryName" maxlength="24" />
      </el-form-item>
      <el-form-item label="排序值">
        <el-input-number v-model="form.sortNo" :min="0" :step="1" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>
```

- [ ] **Step 2: Create the combo items editor component**

```vue
<template>
  <section class="combo-editor">
    <div class="head">
      <div>
        <h3>套餐组成</h3>
        <p>选择门店已有单品 SKU，并设置固定数量。</p>
      </div>
      <el-button plain @click="appendRow">添加组成项</el-button>
    </div>

    <div v-if="modelValue.length === 0" class="empty-text">当前还没有套餐组成项。</div>

    <div v-else class="rows">
      <div v-for="(row, index) in modelValue" :key="index" class="row">
        <el-select :model-value="row.skuId" placeholder="选择 SKU" @update:model-value="updateSku(index, $event)">
          <el-option
            v-for="candidate in candidates"
            :key="candidate.skuId"
            :label="`${candidate.productName} / ${candidate.skuName}`"
            :value="candidate.skuId"
            :disabled="isSkuTaken(candidate.skuId, index)"
          />
        </el-select>
        <el-input-number :model-value="row.quantity" :min="1" :step="1" @update:model-value="updateQty(index, $event)" />
        <el-button type="danger" plain @click="removeRow(index)">删除</el-button>
      </div>
    </div>
  </section>
</template>
```

- [ ] **Step 3: Create the unified product editor component**

```vue
<template>
  <el-dialog
    :model-value="visible"
    :title="mode === 'create' ? '新增商品' : '编辑商品'"
    width="720px"
    @close="emit('update:visible', false)"
  >
    <el-form label-position="top" class="editor-form">
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
      <el-form-item label="商品分类">
        <el-select v-model="form.categoryId" placeholder="请选择分类">
          <el-option v-for="item in categories" :key="item.id" :label="item.categoryName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品名称">
        <el-input v-model="form.productName" maxlength="32" />
      </el-form-item>
      <el-form-item label="商品描述">
        <el-input v-model="form.description" type="textarea" :rows="3" />
      </el-form-item>
      <el-form-item label="图片链接">
        <div class="upload-row">
          <el-input v-model="form.imageUrl" />
          <input ref="fileInputRef" type="file" accept="image/*" @change="uploadImage" />
        </div>
      </el-form-item>
      <el-form-item label="排序值">
        <el-input-number v-model="form.sortNo" :min="0" :step="1" />
      </el-form-item>
      <el-form-item label="规格名称">
        <el-input v-model="form.skuName" maxlength="32" />
      </el-form-item>
      <el-form-item label="价格（元）">
        <el-input-number v-model="form.price" :min="0.01" :step="1" :precision="2" />
      </el-form-item>
      <el-form-item label="库存">
        <el-input-number v-model="form.stock" :min="0" :step="1" />
      </el-form-item>
      <el-form-item label="规格状态">
        <el-radio-group v-model="form.skuStatus">
          <el-radio :label="1">上架</el-radio>
          <el-radio :label="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <MerchantComboItemsEditor
      v-if="form.productType === 2"
      :model-value="form.comboItems"
      :candidates="comboCandidates"
      @update:model-value="form.comboItems = $event"
    />
```

- [ ] **Step 4: Implement editor validation and submit contract**

```ts
const submit = async () => {
  if (!form.productName.trim() || !form.skuName.trim() || !form.categoryId) {
    ElMessage.warning('请先填写商品名称、分类和规格名称')
    return
  }
  if (form.price <= 0) {
    ElMessage.warning('价格必须大于 0')
    return
  }
  if (form.stock < 0) {
    ElMessage.warning('库存不能为负数')
    return
  }
  if (form.productType === 2 && form.comboItems.length === 0) {
    ElMessage.warning('套餐至少需要一个组成项')
    return
  }
  if (form.productType === 2 && form.comboItems.some((item) => !item.skuId || item.quantity < 1)) {
    ElMessage.warning('请先补全套餐组成')
    return
  }

  emit('submit', {
    id: form.id,
    categoryId: form.categoryId,
    productName: form.productName.trim(),
    productType: form.productType,
    imageUrl: form.imageUrl?.trim() || undefined,
    description: form.description?.trim() || undefined,
    sortNo: form.sortNo,
    skus: [
      {
        skuName: form.skuName.trim(),
        price: form.price,
        stock: form.stock,
        status: form.skuStatus,
      },
    ],
    comboItems:
      form.productType === 2
        ? form.comboItems.map((item, index) => ({
            skuId: item.skuId,
            quantity: item.quantity,
            sortNo: index + 1,
          }))
        : undefined,
  })
}
```

- [ ] **Step 5: Run frontend build to verify the new components compile**

Run: `npm run build`

Expected: PASS, with at most the existing large chunk warning.

- [ ] **Step 6: Commit**

```bash
git add campus-catering-web/src/components/workspace/MerchantCategoryEditor.vue campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue campus-catering-web/src/components/workspace/MerchantProductEditor.vue
git commit -m "feat: add merchant product editor components"
```

## Task 3: Wire the unified editor into the product center

**Files:**
- Modify: `campus-catering-web/src/views/workspace/ProductsView.vue`

- [ ] **Step 1: Introduce normalized category and combo candidate data**

```ts
type CategoryOption = {
  id: number
  categoryName: string
  sortNo: number
}

type ComboCandidate = {
  skuId: number
  productName: string
  skuName: string
}
```

```ts
const categories = computed<CategoryOption[]>(() => {
  const map = new Map<number, CategoryOption>()
  products.value.forEach((item) => {
    if (!map.has(item.categoryId)) {
      map.set(item.categoryId, {
        id: item.categoryId,
        categoryName: item.categoryName ?? `分类 ${item.categoryId}`,
        sortNo: item.categorySortNo ?? 0,
      })
    }
  })
  return Array.from(map.values()).sort((a, b) => a.sortNo - b.sortNo || a.id - b.id)
})

const comboCandidates = computed<ComboCandidate[]>(() =>
  products.value
    .filter((item) => item.productType === 1)
    .map((item) => ({
      skuId: item.skuId,
      productName: item.productName,
      skuName: item.skuName,
    })),
)
```

- [ ] **Step 2: Extend product card data with category fields needed by the editor**

```ts
type ProductCard = {
  spuId: number
  categoryId: number
  categoryName?: string
  categorySortNo?: number
  imageUrl?: string
  sortNo: number
  saleStatus: number
  skuId: number
  productName: string
  productType: number
  comboItems?: Array<{ skuId: number; spuName: string; skuName: string; quantity: number }>
  comboSummary?: string
  skuName: string
  description?: string
  price: number
  stock: number
  skuStatus: number
}
```

- [ ] **Step 3: Replace the old edit-only dialog with the new reusable editors**

```vue
<section class="head glass-panel">
  <div>
    <h1 class="cc-section-title">商品中心</h1>
    <p class="cc-section-subtitle">支持单品、套餐、分类和现有商品管理操作。</p>
  </div>
  <div class="head-actions">
    <el-button plain @click="categoryDialogVisible = true">新增分类</el-button>
    <el-button type="primary" round @click="openCreate">新增商品</el-button>
    <el-button round @click="loadProducts">刷新</el-button>
  </div>
</section>

<MerchantCategoryEditor v-model:visible="categoryDialogVisible" @submit="submitCategory" />

<MerchantProductEditor
  v-model:visible="productDialogVisible"
  :mode="editorMode"
  :initial-product="activeProduct"
  :categories="categories"
  :combo-candidates="comboCandidates"
  @submit="submitProduct"
/>
```

- [ ] **Step 4: Implement create, edit, and category submit handlers**

```ts
const categoryDialogVisible = ref(false)
const productDialogVisible = ref(false)
const editorMode = ref<'create' | 'edit'>('create')
const activeProduct = ref<ProductCard | null>(null)

const openCreate = () => {
  editorMode.value = 'create'
  activeProduct.value = null
  productDialogVisible.value = true
}

const openEdit = (item: ProductCard) => {
  editorMode.value = 'edit'
  activeProduct.value = item
  productDialogVisible.value = true
}

const submitCategory = async (payload: { categoryName: string; sortNo?: number }) => {
  await saveMerchantCategory(payload)
  ElMessage.success('分类已创建')
  categoryDialogVisible.value = false
  await loadProducts()
}

const submitProduct = async (payload: ProductSaveRequest) => {
  if (editorMode.value === 'create') {
    await saveMerchantProduct(payload)
  } else if (activeProduct.value) {
    await updateMerchantProductBasic(activeProduct.value.spuId, {
      categoryId: payload.categoryId,
      productName: payload.productName,
      imageUrl: payload.imageUrl,
      description: payload.description,
      sortNo: payload.sortNo,
    })
    await updateMerchantSku(activeProduct.value.skuId, {
      skuName: payload.skus[0].skuName,
      price: payload.skus[0].price,
      status: payload.skus[0].status,
    })
    await updateMerchantSkuStock(activeProduct.value.skuId, {
      stock: payload.skus[0].stock,
    })
  }

  ElMessage.success(editorMode.value === 'create' ? '商品已创建' : '商品已更新')
  productDialogVisible.value = false
  await loadProducts()
}
```

- [ ] **Step 5: Keep the existing stock, shelf, and delete actions intact**

```ts
// keep editStock, toggleShelf, remove as existing operations
// adjust only the surrounding UI state so they coexist with the new editor flow
```

- [ ] **Step 6: Run frontend build to verify the full merchant product center works**

Run: `npm run build`

Expected: PASS, with at most the existing large chunk warning.

- [ ] **Step 7: Commit**

```bash
git add campus-catering-web/src/views/workspace/ProductsView.vue
git commit -m "feat: wire merchant product editor into product center"
```

## Task 4: Verify merchant product editor coverage

**Files:**
- Verify only

- [ ] **Step 1: Re-read the merchant product editor spec and check coverage**

Checklist:

- Unified add-product entry exists
- Category creation exists
- Single-item create flow exists
- Fixed combo create flow exists
- Edit reuses the same product editor
- Existing stock update, shelf toggle, and delete still exist

- [ ] **Step 2: Run production build one more time**

Run: `npm run build`

Expected: PASS, with at most the existing large chunk warning.

- [ ] **Step 3: Commit final integration changes**

```bash
git add campus-catering-web/src/api/merchant.ts campus-catering-web/src/views/workspace/ProductsView.vue campus-catering-web/src/components/workspace/MerchantCategoryEditor.vue campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue campus-catering-web/src/components/workspace/MerchantProductEditor.vue docs/superpowers/specs/2026-04-06-merchant-product-editor-design.md docs/superpowers/plans/2026-04-06-merchant-product-editor.md
git commit -m "feat: complete merchant product editor flow"
```

## Self-Review

### Spec coverage

- Unified product editor: covered by Tasks 2 and 3
- Category creation: covered by Tasks 1 and 3
- Single-item and combo create flow: covered by Tasks 2 and 3
- Fixed combo composition: covered by Task 2
- Reuse for editing existing products: covered by Task 3
- Existing list actions retained: covered by Task 3

### Placeholder scan

- No `TODO`, `TBD`, or deferred implementation placeholders remain
- All modified or created files are explicit
- Verification steps use concrete commands

### Type consistency

- `ProductSaveRequest` is defined before later tasks reference it
- Category and combo editor payload shapes match the plan’s page-level submit handlers
- Build verification is repeated at the integration checkpoint
