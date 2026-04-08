# Merchant Reliability And Address Refresh Design

**Date:** 2026-04-06

**Goal:** Improve the reliability of merchant product editing while also refreshing the student address page UI without changing its business contract.

## Scope

Primary scope:

- Merchant product category data becomes real and reloadable
- Merchant product edit submits through a single reliable update flow
- The current single-SKU editor stops pretending it can safely edit multi-SKU products

Secondary scope:

- Student address list and create/edit form get a stronger page layout and clearer form experience

## Merchant Reliability Design

### 1. Real category loading

Current category options are derived from product data and in-memory newly created items. That means:

- Empty categories disappear
- Reload loses category names
- The editor falls back to fake labels like `分类 5`

The backend already has `selectByStoreId` in `ProductCategoryMapper`, so add a merchant category list endpoint and consume it from the frontend.

### 2. Transactional product update

Current edit flow performs:

- product basic update
- sku basic update
- sku stock update
- combo update

as separate requests. If any later request fails, earlier changes remain committed.

Replace that for the editor path with one backend update endpoint that:

- verifies merchant ownership once
- updates basic info and the selected SKU in one transaction
- updates combo description/items only when combo changes are actually supplied

This preserves today’s editor scope while removing the worst partial-save failure mode.

### 3. Multi-SKU safety rail

The current editor only models one SKU. Instead of silently editing the first SKU of a multi-SKU product:

- detect products with more than one SKU
- show a clear `多规格` marker
- disable the unified editor entry for those products
- keep stock/shelf/delete actions available where already supported

This is intentionally a safety rail, not a full multi-SKU editor.

### 4. Combo edit behavior

If an existing combo has unchanged composition, editing unrelated fields should not require re-submitting combo items.

The update endpoint will therefore treat combo payload as optional:

- if combo fields are absent, preserve current combo state
- if combo fields are present, replace combo description/items transactionally

That prevents basic edits from failing just because the combo section was not touched.

## Student Address UI Refresh

The address page keeps the same API behavior but gets a stronger UI:

- a clearer page header and empty state
- a more card-like address list with stronger default-state emphasis
- a larger create/edit dialog with grouped fields
- a live summary block so the resulting address is easier to verify before saving

The goal is usability and visual clarity, not new address business rules.

## Files Expected To Change

Backend:

- `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/controller/MerchantProductController.java`
- `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/service/ProductService.java`
- `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/mapper/ProductCategoryMapper.java`
- new DTOs under `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/product/dto/`

Frontend:

- `campus-catering-web/src/api/merchant.ts`
- `campus-catering-web/src/views/workspace/merchant-product-editor.ts`
- `campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts`
- `campus-catering-web/src/views/workspace/ProductsView.vue`
- `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`
- `campus-catering-web/src/views/student/StudentAddressView.vue`

## Verification

Minimum verification:

- Merchant categories load from a real endpoint and survive reload
- Merchant editor update uses one request for the main edit path
- Multi-SKU products are not silently editable through the single-SKU editor
- Student address page still supports create, edit, delete, and set-default flows
- Frontend tests and build pass

## Self-Review

- Scope stays focused on reliability and UI refresh, not a full merchant product redesign
- The design avoids requiring a brand-new multi-SKU editor
- The student address page remains API-compatible
