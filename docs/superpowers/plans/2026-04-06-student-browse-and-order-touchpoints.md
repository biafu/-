# Student Browse And Order Touchpoints Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the student home, messages, and orders pages fully interactive where visible buttons and entry points currently feel inert.

**Architecture:** Keep the existing student route structure and implement lightweight client-side enhancements. Use small helper functions for store filtering and message jump resolution, then wire those helpers into the current views so we avoid adding a new order detail page.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Vue Router, existing student API module, Vitest, Vite

---

## File Map

### Frontend

- Create: `campus-catering-web/src/views/student/student-touchpoints.ts`
- Create: `campus-catering-web/src/views/student/student-touchpoints.spec.ts`
- Modify: `campus-catering-web/src/views/student/StudentHomeView.vue`
- Modify: `campus-catering-web/src/views/student/StudentMessagesView.vue`
- Modify: `campus-catering-web/src/views/student/StudentOrdersView.vue`
- Modify: `campus-catering-web/src/views/student/StudentPaymentResultView.vue`

## Task 1: Add student touchpoint helper coverage

**Files:**
- Create: `campus-catering-web/src/views/student/student-touchpoints.ts`
- Create: `campus-catering-web/src/views/student/student-touchpoints.spec.ts`

- [ ] **Step 1: Write failing helper tests for home filters and message routing**
- [ ] **Step 2: Run `npm run test -- src/views/student/student-touchpoints.spec.ts` and confirm it fails because the helper file does not exist yet**
- [ ] **Step 3: Implement helper functions for derived categories, sort modes, message action resolution, and order target parsing**
- [ ] **Step 4: Re-run `npm run test -- src/views/student/student-touchpoints.spec.ts` and confirm it passes**

## Task 2: Wire the home page category and sort interactions

**Files:**
- Modify: `campus-catering-web/src/views/student/StudentHomeView.vue`
- Reuse: `campus-catering-web/src/views/student/student-touchpoints.ts`

- [ ] **Step 1: Replace static category buttons with a tracked selected category state**
- [ ] **Step 2: Replace static sort chips with a tracked selected sort mode**
- [ ] **Step 3: Apply keyword, category, and sort together to the visible store list**
- [ ] **Step 4: Bind the `去点餐` button to the same route action as the store card without double-trigger issues**

## Task 3: Add message business jump actions

**Files:**
- Modify: `campus-catering-web/src/views/student/StudentMessagesView.vue`
- Reuse: `campus-catering-web/src/views/student/student-touchpoints.ts`

- [ ] **Step 1: Add a resolved action per message when the business type can be interpreted**
- [ ] **Step 2: Render a `查看相关内容` button only for recognized destinations**
- [ ] **Step 3: Mark unread messages as read before navigating**
- [ ] **Step 4: Route order jumps to the orders page with `highlightOrderId` and `expandOrderId` query params**

## Task 4: Add order detail expansion and targeted entry

**Files:**
- Modify: `campus-catering-web/src/views/student/StudentOrdersView.vue`
- Reuse: `campus-catering-web/src/views/student/student-touchpoints.ts`

- [ ] **Step 1: Normalize and repair the orders view text/content while preserving existing order actions**
- [ ] **Step 2: Add local state for expanded order ids and highlighted target id**
- [ ] **Step 3: Render inline detail content for receiver, address, remark, items, and total**
- [ ] **Step 4: Auto-expand and highlight the targeted order when query parameters are present**

## Task 5: Link payment result into targeted orders

**Files:**
- Modify: `campus-catering-web/src/views/student/StudentPaymentResultView.vue`

- [ ] **Step 1: Update the `查看订单` action to preserve the created order id in the orders-page route query**
- [ ] **Step 2: Keep the existing pay-now behavior unchanged**

## Task 6: Verify student touchpoint flow

**Files:**
- Verify only

- [ ] **Step 1: Run `npm run test -- src/views/student/student-touchpoints.spec.ts`**
- [ ] **Step 2: Run `npm run build`**
- [ ] **Step 3: Confirm the flow coverage against the spec: home filters, message jumps, targeted order expansion, payment-result jump**

## Self-Review

### Spec coverage

- Home category and sort interactions: Tasks 1 and 2
- Message business jump actions: Tasks 1 and 3
- Order inline detail expansion: Task 4
- Payment-result linkage into orders: Task 5

### Placeholder scan

- No `TODO` or deferred placeholders remain
- Tasks are implementation-sized and sequenced

### Type consistency

- Helper functions are introduced before the modified views depend on them
- Query parameter names stay consistent: `highlightOrderId` and `expandOrderId`
