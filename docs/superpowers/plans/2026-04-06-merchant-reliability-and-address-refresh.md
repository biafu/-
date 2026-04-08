# Merchant Reliability And Address Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make merchant product editing safer and more durable while giving the student address page a clearer, more polished UI.

**Architecture:** Add a real merchant category list endpoint and a transactional product update endpoint, then simplify the frontend editor around those contracts. On the student side, keep the address API flow unchanged and focus only on layout, grouping, and presentation improvements.

**Tech Stack:** Spring Boot, MyBatis, Vue 3, TypeScript, Element Plus, Vitest, Vite

---

## File Map

Backend:

- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/controller/MerchantProductController.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/service/ProductService.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/mapper/ProductCategoryMapper.java`
- Create: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/dto/CategoryViewResponse.java`
- Create: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/dto/ProductEditorUpdateRequest.java`

Frontend:

- Modify: `campus-catering-web/src/api/merchant.ts`
- Modify: `campus-catering-web/src/views/workspace/merchant-product-editor.ts`
- Modify: `campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts`
- Modify: `campus-catering-web/src/views/workspace/ProductsView.vue`
- Modify: `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`
- Modify: `campus-catering-web/src/views/student/StudentAddressView.vue`

## Task 1: Add merchant helper coverage for safer editor behavior

**Files:**
- Modify: `campus-catering-web/src/views/workspace/merchant-product-editor.ts`
- Modify: `campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts`

- [ ] **Step 1: Add failing tests for real category merging and multi-SKU edit safety**
- [ ] **Step 2: Run `npm run test -- src/views/workspace/merchant-product-editor.spec.ts` and confirm the new assertions fail**
- [ ] **Step 3: Implement helper support for category normalization and `isMerchantProductEditable`**
- [ ] **Step 4: Re-run `npm run test -- src/views/workspace/merchant-product-editor.spec.ts` and confirm it passes**

## Task 2: Add backend category list and transactional product update

**Files:**
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/controller/MerchantProductController.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/service/ProductService.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/mapper/ProductCategoryMapper.java`
- Create: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/dto/CategoryViewResponse.java`
- Create: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/dto/ProductEditorUpdateRequest.java`

- [ ] **Step 1: Add a merchant category list response and endpoint**
- [ ] **Step 2: Add a transactional product editor update DTO and service method**
- [ ] **Step 3: Keep combo updates optional so unchanged combo state is preserved**
- [ ] **Step 4: Wire the new endpoint in the controller**

## Task 3: Rewire the merchant frontend around reliable contracts

**Files:**
- Modify: `campus-catering-web/src/api/merchant.ts`
- Modify: `campus-catering-web/src/views/workspace/ProductsView.vue`
- Modify: `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`
- Modify: `campus-catering-web/src/views/workspace/merchant-product-editor.ts`

- [ ] **Step 1: Add `fetchMerchantCategories` and `updateMerchantProductEditor` requests**
- [ ] **Step 2: Load categories from the backend instead of deriving only from product data**
- [ ] **Step 3: Use the new single update request for edit mode**
- [ ] **Step 4: Mark multi-SKU products as not editable through the unified editor and surface that clearly in the list**

## Task 4: Refresh the student address page UI

**Files:**
- Modify: `campus-catering-web/src/views/student/StudentAddressView.vue`

- [ ] **Step 1: Restructure the address list into a stronger card layout with clearer default-state emphasis**
- [ ] **Step 2: Redesign the create/edit dialog with grouped fields and a better visual hierarchy**
- [ ] **Step 3: Add a lightweight address preview summary within the dialog without changing save behavior**

## Task 5: Verify the integrated result

**Files:**
- Verify only

- [ ] **Step 1: Run `npm run test -- src/views/workspace/merchant-product-editor.spec.ts`**
- [ ] **Step 2: Run `npm run build`**
- [ ] **Step 3: Note backend verification limitations if Java tooling remains unavailable**

## Self-Review

### Spec coverage

- Real category loading: Tasks 1 to 3
- Transactional merchant edit path: Tasks 2 and 3
- Multi-SKU safety rail: Tasks 1 and 3
- Student address page UI refresh: Task 4

### Placeholder scan

- No deferred placeholders remain
- Verification steps are explicit

### Type consistency

- Frontend helper and API changes are introduced before the page depends on them
- The edit path uses one dedicated update contract instead of chained basic/sku/stock requests
