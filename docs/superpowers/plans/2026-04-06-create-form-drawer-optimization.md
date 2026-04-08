# Create Form Drawer Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve the usability of complex create/edit flows by converting the merchant product editor and student address editor from dialogs to drawers with fixed action areas and scrollable content.

**Architecture:** Keep existing data flow, validation, and API calls intact while changing only the presentation shell and layout structure. Use `el-drawer` for the two complex forms, preserve short dialogs elsewhere, and extract a small shared layout pattern only if duplication remains after both conversions.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Vite, Vitest

---

## File Map

- Modify: `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`
  Purpose: convert merchant product create/edit shell from dialog to drawer and reorganize sections.
- Modify: `campus-catering-web/src/views/workspace/ProductsView.vue`
  Purpose: keep editor open/close wiring compatible with the new drawer-based component.
- Modify: `campus-catering-web/src/views/student/StudentAddressView.vue`
  Purpose: convert address create/edit shell from dialog to drawer and simplify the form layout.
- Modify: `campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts`
  Purpose: cover merchant editor shell behavior and ensure key actions remain visible and wired.
- Optional modify: `campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue`
  Purpose: adjust spacing or height behavior if the combo section needs drawer-specific styling.

### Task 1: Convert Merchant Product Editor Shell

**Files:**
- Modify: `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`
- Modify: `campus-catering-web/src/views/workspace/ProductsView.vue`
- Test: `campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts`

- [ ] **Step 1: Write the failing test**

Add a shell-oriented test that checks the merchant editor renders as a drawer and still exposes save/cancel actions.

```ts
it('renders product editor in a drawer shell with persistent actions', async () => {
  const wrapper = mount(MerchantProductEditor, {
    props: {
      visible: true,
      mode: 'create',
      submitting: false,
      categories: [{ id: 1, categoryName: 'Main', sortNo: 1 }],
      skuOptions: [],
      modelValue: {
        productType: 1,
        categoryId: 1,
        productName: '',
        imageUrl: '',
        description: '',
        sortNo: 0,
        skuId: undefined,
        skuName: '',
        price: 12,
        stock: 10,
        skuStatus: 1,
        comboDesc: '',
        comboItems: []
      }
    }
  })

  expect(wrapper.find('.merchant-product-drawer').exists()).toBe(true)
  expect(wrapper.find('.drawer-footer .el-button--primary').exists()).toBe(true)
  expect(wrapper.text()).toContain('新增商品')
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
npm run test -- src/views/workspace/merchant-product-editor.spec.ts
```

Expected: FAIL because the component still renders dialog-specific markup or missing drawer selectors.

- [ ] **Step 3: Write minimal implementation**

Update the editor shell to use `el-drawer`, a scrollable body, and a fixed footer.

```vue
<el-drawer
  :model-value="visible"
  size="720px"
  class="merchant-product-drawer"
  destroy-on-close
  @close="emit('close')"
>
  <template #header>
    <div class="drawer-header">
      <h3>{{ mode === 'create' ? '新增商品' : '编辑商品' }}</h3>
      <p>按分区填写商品信息，保存操作会固定显示。</p>
    </div>
  </template>

  <div class="drawer-body">
    <section class="editor-section">
      <h4>基础信息</h4>
      <!-- existing basic fields -->
    </section>

    <section class="editor-section">
      <h4>规格信息</h4>
      <!-- existing sku fields -->
    </section>

    <section v-if="form.productType === 2" class="editor-section">
      <h4>套餐组成</h4>
      <MerchantComboItemsEditor ... />
    </section>
  </div>

  <template #footer>
    <div class="drawer-footer">
      <el-button @click="emit('close')">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </div>
  </template>
</el-drawer>
```

Add drawer-specific layout styles in the same file.

```css
.merchant-product-drawer :deep(.el-drawer__body) {
  padding: 0;
  overflow: hidden;
}

.drawer-body {
  height: 100%;
  overflow: auto;
  padding: 0 24px 24px;
}

.drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.editor-section {
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 20px;
  background: #fffaf2;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
npm run test -- src/views/workspace/merchant-product-editor.spec.ts
```

Expected: PASS and the new drawer shell test succeeds.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/components/workspace/MerchantProductEditor.vue campus-catering-web/src/views/workspace/ProductsView.vue campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts
git commit -m "feat: move merchant product editor into drawer"
```

### Task 2: Refine Merchant Editor Section Layout

**Files:**
- Modify: `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`
- Optional modify: `campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue`
- Test: `campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts`

- [ ] **Step 1: Write the failing test**

Add a test that ensures combo editing remains visible in drawer mode when product type is combo.

```ts
it('shows combo section inside the drawer for combo products', async () => {
  const wrapper = mount(MerchantProductEditor, {
    props: {
      visible: true,
      mode: 'edit',
      submitting: false,
      categories: [{ id: 1, categoryName: 'Main', sortNo: 1 }],
      skuOptions: [{ skuId: 11, skuName: 'Rice', status: 1 }],
      modelValue: {
        productType: 2,
        categoryId: 1,
        productName: '套餐A',
        imageUrl: '',
        description: '',
        sortNo: 0,
        skuId: 22,
        skuName: '套餐A',
        price: 18,
        stock: 10,
        skuStatus: 1,
        comboDesc: '',
        comboItems: [{ skuId: 11, quantity: 1, sortNo: 1 }]
      }
    }
  })

  expect(wrapper.text()).toContain('套餐组成')
  expect(wrapper.find('.editor-section.combo-section').exists()).toBe(true)
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
npm run test -- src/views/workspace/merchant-product-editor.spec.ts
```

Expected: FAIL if the combo section lacks section marker classes or drawer layout support.

- [ ] **Step 3: Write minimal implementation**

Add section classes and spacing rules so combo composition behaves predictably in the drawer.

```vue
<section
  v-if="form.productType === 2"
  class="editor-section combo-section"
>
  <div class="section-heading">
    <h4>套餐组成</h4>
    <p>从当前门店已上架的单品中选择固定组成。</p>
  </div>
  <MerchantComboItemsEditor ... />
</section>
```

```css
.combo-section {
  padding-bottom: 28px;
}

.section-heading {
  margin-bottom: 14px;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
npm run test -- src/views/workspace/merchant-product-editor.spec.ts
```

Expected: PASS with combo section test green.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/components/workspace/MerchantProductEditor.vue campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts
git commit -m "feat: improve merchant drawer section layout"
```

### Task 3: Convert Student Address Editor Shell

**Files:**
- Modify: `campus-catering-web/src/views/student/StudentAddressView.vue`
- Test: existing manual build verification only

- [ ] **Step 1: Add a quick verification target**

Document the selectors and visible states that must hold after the refactor.

```txt
Expected selectors:
- .address-editor-drawer
- .address-drawer-body
- .address-drawer-footer
- .address-preview-card
```

- [ ] **Step 2: Run a baseline build before changing the view**

Run:

```bash
npm run build
```

Expected: PASS before the address shell refactor starts.

- [ ] **Step 3: Write minimal implementation**

Replace the dialog shell with a drawer and simplify the form into a single-column card layout.

```vue
<el-drawer
  v-model="dialogVisible"
  :title="editingId ? '编辑地址' : '新增地址'"
  size="560px"
  class="address-editor-drawer"
  destroy-on-close
>
  <div class="address-drawer-body">
    <section class="address-form-card">
      <h4>联系信息</h4>
      <!-- name and phone fields -->
    </section>

    <section class="address-form-card">
      <h4>地址信息</h4>
      <!-- campus/building/detail fields -->
    </section>

    <section class="address-form-card">
      <h4>配送偏好</h4>
      <!-- tag and default switch -->
    </section>

    <section class="address-preview-card">
      <!-- existing preview summary -->
    </section>
  </div>

  <template #footer>
    <div class="address-drawer-footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submitAddress">保存地址</el-button>
    </div>
  </template>
</el-drawer>
```

Add styles:

```css
.address-editor-drawer :deep(.el-drawer__body) {
  padding: 0;
  overflow: hidden;
}

.address-drawer-body {
  height: 100%;
  overflow: auto;
  padding: 0 20px 24px;
}

.address-form-card,
.address-preview-card {
  margin-bottom: 16px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fffdf8 0%, #fff8ef 100%);
}

.address-drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
```

- [ ] **Step 4: Run build to verify the view compiles**

Run:

```bash
npm run build
```

Expected: PASS with no new Vue or TypeScript errors.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/views/student/StudentAddressView.vue
git commit -m "feat: move address editor into drawer"
```

### Task 4: Final Verification Sweep

**Files:**
- Modify: none
- Test: `campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts`

- [ ] **Step 1: Run targeted merchant editor tests**

Run:

```bash
npm run test -- src/views/workspace/merchant-product-editor.spec.ts
```

Expected: PASS

- [ ] **Step 2: Run full frontend build**

Run:

```bash
npm run build
```

Expected: PASS, with only pre-existing bundle-size warnings if any.

- [ ] **Step 3: Smoke check key flows manually**

Verify in browser:

```txt
1. Merchant workspace > Products > 新增商品 opens in a drawer.
2. Drawer body scrolls independently and save/cancel remain visible.
3. Combo product editing still shows 套餐组成 and can submit.
4. Student > 地址管理 > 新增地址 opens in a drawer.
5. Address form remains readable without side-by-side crowding.
6. Save/cancel remain easy to reach while scrolling.
```

- [ ] **Step 4: Commit final integration**

```bash
git add campus-catering-web/src/components/workspace/MerchantProductEditor.vue campus-catering-web/src/views/workspace/ProductsView.vue campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts campus-catering-web/src/views/student/StudentAddressView.vue
git commit -m "feat: improve complex create forms with drawers"
```
