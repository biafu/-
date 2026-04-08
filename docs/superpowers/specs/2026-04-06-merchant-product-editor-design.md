# Merchant Product Editor Design

## Goal

Complete the merchant-side basic product management loop by adding a unified product editor that supports both single products and fixed-combo products, while preserving the existing shelf, delete, and stock-adjustment actions.

## Scope

This spec covers:

- Merchant product center add-product entry
- Merchant category creation entry
- Unified product editor for single items and combo items
- Fixed combo composition management
- Reuse of the same editor for editing existing products

This spec does not cover:

- Optional combo groups
- Replaceable combo items
- Multi-level combo pricing logic
- Batch import/export
- Advanced category ordering or drag-and-drop management

## Product Direction

Use one unified merchant product editor instead of separate flows for single items and combos.

The merchant enters from one `新增商品` action, selects `单品` or `套餐`, and then sees the corresponding form area:

- Single item: basic info + one SKU block
- Combo: same basic info + one SKU block + fixed combo composition block

This keeps the entry simple, matches the current merchant product list structure, and avoids maintaining two mostly duplicated editing flows.

## User Experience

### Product Center

Enhance the current merchant product center with:

- `新增商品` primary action
- `新增分类` secondary action
- Existing product cards kept in place
- Existing actions kept: edit, stock update, on/off shelf, delete

Editing an existing product opens the same unified editor, prefilled from the selected product.

### Category Creation

Provide a lightweight dialog for category creation:

- Category name
- Sort number

After saving:

- Refresh merchant product data
- Refresh category options in the editor

### Unified Product Editor

The editor should support these shared fields:

- Product type: single or combo
- Category
- Product name
- Description
- Image URL / upload
- Sort number
- SKU name
- Price
- Stock
- SKU status

Single item mode:

- No combo composition area

Combo mode:

- Show fixed combo composition editor
- Merchant selects existing single-item SKUs from the current store
- Merchant assigns quantity for each selected SKU
- Combo summary is derived from chosen items

### Combo Composition Rules

For this version, combo composition is fixed:

- Each combo item references one existing SKU
- Quantity must be a positive integer
- Duplicate SKU selections are not allowed
- Combo items must come from the current merchant store
- Prefer selecting from currently existing single-item products

The UI should make the combo structure easy to review before saving.

## Data Flow

### Read Path

Merchant product center loads:

- Merchant product list
- Category options

Combo editor also needs selectable single-item SKU candidates derived from current merchant products.

### Create Path

1. Merchant opens `新增商品`
2. Fills unified editor
3. Frontend maps form data to backend `product/save`
4. On success, refresh product list
5. Close editor and show success feedback

### Edit Path

1. Merchant opens existing item
2. Editor hydrates from selected product
3. Save updates basic product info and SKU info
4. If combo, save combo composition in the same submit flow
5. Refresh product list after success

## Frontend Structure

## Files To Modify

- `campus-catering-web/src/views/workspace/ProductsView.vue`
- `campus-catering-web/src/api/merchant.ts`

## Files To Add

- `campus-catering-web/src/components/workspace/MerchantProductEditor.vue`
- `campus-catering-web/src/components/workspace/MerchantComboItemsEditor.vue`
- `campus-catering-web/src/components/workspace/MerchantCategoryEditor.vue`

## Component Responsibilities

### `ProductsView`

- Owns page-level loading and refresh
- Opens add/edit dialogs
- Supplies category list and SKU candidate list
- Keeps existing product actions available

### `MerchantProductEditor`

- Owns unified single/combo form
- Handles create vs edit modes
- Validates required fields
- Emits final payload back to page shell

### `MerchantComboItemsEditor`

- Manages fixed combo item rows
- Handles SKU selection and quantity edits
- Prevents duplicate SKU selection

### `MerchantCategoryEditor`

- Small dialog for creating categories

## API Contract Expectations

The current backend already exposes merchant product save and update endpoints. The frontend should align with:

- category save
- product save
- product basic update
- SKU update
- SKU stock update
- product list
- image upload
- on-shelf / off-shelf
- delete

If combo save requires fields not yet mapped in the current frontend API typings, extend `merchant.ts` so the editor can send a complete payload.

## Validation Rules

Frontend validation should enforce:

- Product name required
- Category required
- SKU name required
- Price must be greater than zero
- Stock must be zero or greater
- Combo must contain at least one combo item
- Combo quantities must be positive integers
- Duplicate combo SKU selections not allowed

Backend remains source of truth for final validation and ownership checks.

## Error Handling

- Save failure shows inline feedback or message toast
- Upload failure preserves current form state
- Category save failure does not close product editor
- Combo item validation failure blocks submit before API call
- Refresh failure after submit should surface a clear message

## Success Criteria

Merchant can:

- Create a category
- Create a single-item product
- Create a fixed-combo product
- Edit both single and combo products from the same editor
- Continue using stock update, shelf toggle, and delete from the list

## Risks

### Existing backend payload shape

Combo product creation depends on the exact backend `product/save` contract. If the existing frontend types do not fully cover combo fields, frontend API types must be expanded carefully to match current backend DTOs.

### Editing existing combo details

If current product list data is not sufficient to fully hydrate combo composition for editing, the page may need a small transformation layer based on existing `comboItems` response fields.

## Implementation Notes

Build the first version as a practical merchant operations tool:

- prioritize completeness over advanced workflow polish
- prefer one clear editor over multiple fragmented dialogs
- keep the current product grid intact unless a change directly improves basic usability
