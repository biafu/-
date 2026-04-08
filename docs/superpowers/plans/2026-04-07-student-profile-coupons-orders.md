# Student Profile Coupons Orders Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a complete student-side personal area by introducing a profile hub, a coupon center, and an order detail page with stable cross-page routing.

**Architecture:** Reuse existing student APIs and list data instead of introducing new backend contracts. Add three focused student views, extend routing/navigation, and centralize lightweight summary-building helpers so the new pages stay consistent with the existing student flow.

**Tech Stack:** Vue 3, TypeScript, Vue Router, Element Plus, Vite, Vitest

---

## File Map

- Modify: `campus-catering-web/src/router/index.ts`
  Purpose: register profile, coupon center, and order detail routes.
- Modify: `campus-catering-web/src/views/student/StudentLayout.vue`
  Purpose: add the profile navigation entry and keep student summary signals visible.
- Create: `campus-catering-web/src/views/student/StudentProfileView.vue`
  Purpose: render the student personal hub with summary cards and quick actions.
- Create: `campus-catering-web/src/views/student/StudentCouponsView.vue`
  Purpose: show claimable coupons and owned coupons in a dedicated page.
- Create: `campus-catering-web/src/views/student/StudentOrderDetailView.vue`
  Purpose: render an independent order detail page with existing order actions.
- Create: `campus-catering-web/src/views/student/student-profile.ts`
  Purpose: hold reusable summary/helper builders for profile, coupons, and order lookup.
- Create: `campus-catering-web/src/views/student/student-profile.spec.ts`
  Purpose: verify new helper logic and route-facing summary shaping.
- Modify: `campus-catering-web/src/views/student/StudentMessagesView.vue`
  Purpose: route order-related message actions to the new order detail page.
- Modify: `campus-catering-web/src/views/student/StudentPaymentResultView.vue`
  Purpose: direct “view order” action to the new detail page.
- Modify: `campus-catering-web/src/views/student/StudentOrdersView.vue`
  Purpose: add stable detail-page navigation while preserving current inline details.

### Task 1: Add Student Routes And Navigation Entry

**Files:**
- Modify: `campus-catering-web/src/router/index.ts`
- Modify: `campus-catering-web/src/views/student/StudentLayout.vue`
- Test: `campus-catering-web/src/views/student/student-profile.spec.ts`

- [ ] **Step 1: Write the failing test**

Create a route-oriented helper test to document the expected student destinations.

```ts
it('exposes stable student destinations for profile, coupons, and order detail', () => {
  expect(buildStudentProfileLinks()).toEqual({
    profile: '/student/profile',
    coupons: '/student/coupons',
    orders: '/student/orders',
    addresses: '/student/addresses',
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: FAIL because the helper file and test target do not exist yet.

- [ ] **Step 3: Write minimal implementation**

Add the helper and register the new routes/navigation entry.

```ts
export function buildStudentProfileLinks() {
  return {
    profile: '/student/profile',
    coupons: '/student/coupons',
    orders: '/student/orders',
    addresses: '/student/addresses',
  }
}
```

```ts
{
  path: 'profile',
  name: 'student-profile',
  meta: { requiresRole: 'STUDENT' },
  component: () => import('@/views/student/StudentProfileView.vue'),
},
{
  path: 'coupons',
  name: 'student-coupons',
  meta: { requiresRole: 'STUDENT' },
  component: () => import('@/views/student/StudentCouponsView.vue'),
},
{
  path: 'orders/:id',
  name: 'student-order-detail',
  meta: { requiresRole: 'STUDENT' },
  component: () => import('@/views/student/StudentOrderDetailView.vue'),
},
```

```vue
<RouterLink to="/student/profile" class="tab">我的</RouterLink>
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/router/index.ts campus-catering-web/src/views/student/StudentLayout.vue campus-catering-web/src/views/student/student-profile.ts campus-catering-web/src/views/student/student-profile.spec.ts
git commit -m "feat: add student profile routes and nav"
```

### Task 2: Build Student Profile Hub

**Files:**
- Create: `campus-catering-web/src/views/student/StudentProfileView.vue`
- Modify: `campus-catering-web/src/views/student/student-profile.ts`
- Modify: `campus-catering-web/src/views/student/student-profile.spec.ts`

- [ ] **Step 1: Write the failing test**

Add a summary-builder test for the profile dashboard cards.

```ts
it('builds student profile summary cards from orders, messages, addresses, and coupons', () => {
  expect(
    buildStudentProfileSummary({
      unpaidOrderCount: 2,
      unreadCount: 5,
      defaultAddressText: '南校区 / 5号宿舍 / 302',
      availableCouponCount: 3,
    }),
  ).toEqual([
    { key: 'orders', value: '2', label: '待支付订单' },
    { key: 'messages', value: '5', label: '未读消息' },
    { key: 'address', value: '南校区 / 5号宿舍 / 302', label: '默认地址' },
    { key: 'coupons', value: '3', label: '可用优惠券' },
  ])
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: FAIL because the summary helper does not exist yet.

- [ ] **Step 3: Write minimal implementation**

Add the helper and create the profile page using existing APIs.

```ts
export function buildStudentProfileSummary(input: {
  unpaidOrderCount: number
  unreadCount: number
  defaultAddressText: string
  availableCouponCount: number
}) {
  return [
    { key: 'orders', value: String(input.unpaidOrderCount), label: '待支付订单' },
    { key: 'messages', value: String(input.unreadCount), label: '未读消息' },
    { key: 'address', value: input.defaultAddressText, label: '默认地址' },
    { key: 'coupons', value: String(input.availableCouponCount), label: '可用优惠券' },
  ]
}
```

```vue
<template>
  <section class="header glass-panel">
    <div>
      <p class="eyebrow">My Campus</p>
      <h1 class="cc-section-title">我的</h1>
      <p class="cc-section-subtitle">在这里快速查看订单、消息、地址和优惠券。</p>
    </div>
  </section>

  <section class="summary-grid">
    <article v-for="card in summaryCards" :key="card.key" class="cc-card summary-card">
      <span>{{ card.label }}</span>
      <strong>{{ card.value }}</strong>
    </article>
  </section>

  <section class="quick-grid">
    <RouterLink v-for="entry in quickLinks" :key="entry.to" :to="entry.to" class="cc-card quick-card">
      <strong>{{ entry.title }}</strong>
      <p>{{ entry.description }}</p>
    </RouterLink>
  </section>
</template>
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/views/student/StudentProfileView.vue campus-catering-web/src/views/student/student-profile.ts campus-catering-web/src/views/student/student-profile.spec.ts
git commit -m "feat: add student profile hub"
```

### Task 3: Build Student Coupon Center

**Files:**
- Create: `campus-catering-web/src/views/student/StudentCouponsView.vue`
- Modify: `campus-catering-web/src/views/student/student-profile.ts`
- Modify: `campus-catering-web/src/views/student/student-profile.spec.ts`

- [ ] **Step 1: Write the failing test**

Add a coupon grouping helper test.

```ts
it('groups coupons into claimable and owned sections', () => {
  expect(
    buildCouponSections({
      claimableCount: 2,
      ownedCount: 4,
    }),
  ).toEqual([
    { key: 'center', label: '可领取优惠券', count: 2 },
    { key: 'mine', label: '我的优惠券', count: 4 },
  ])
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: FAIL because the coupon helper is missing.

- [ ] **Step 3: Write minimal implementation**

Add the helper and build the coupon page using `fetchCouponCenter`, `fetchMyCoupons`, and `claimCoupon`.

```ts
export function buildCouponSections(input: { claimableCount: number; ownedCount: number }) {
  return [
    { key: 'center', label: '可领取优惠券', count: input.claimableCount },
    { key: 'mine', label: '我的优惠券', count: input.ownedCount },
  ]
}
```

```vue
<template>
  <section class="coupon-grid">
    <article class="cc-card coupon-panel">
      <header><h2>可领取优惠券</h2></header>
      <button v-for="coupon in centerCoupons" :key="coupon.couponId" class="coupon-card" @click="claim(coupon.couponId)">
        <strong>{{ coupon.couponName }}</strong>
      </button>
    </article>

    <article class="cc-card coupon-panel">
      <header><h2>我的优惠券</h2></header>
      <div v-for="coupon in myCoupons" :key="coupon.userCouponId" class="coupon-card">
        <strong>{{ coupon.couponName }}</strong>
      </div>
    </article>
  </section>
</template>
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/views/student/StudentCouponsView.vue campus-catering-web/src/views/student/student-profile.ts campus-catering-web/src/views/student/student-profile.spec.ts
git commit -m "feat: add student coupon center"
```

### Task 4: Build Student Order Detail Page And Redirects

**Files:**
- Create: `campus-catering-web/src/views/student/StudentOrderDetailView.vue`
- Modify: `campus-catering-web/src/views/student/student-profile.ts`
- Modify: `campus-catering-web/src/views/student/student-profile.spec.ts`
- Modify: `campus-catering-web/src/views/student/StudentOrdersView.vue`
- Modify: `campus-catering-web/src/views/student/StudentMessagesView.vue`
- Modify: `campus-catering-web/src/views/student/StudentPaymentResultView.vue`

- [ ] **Step 1: Write the failing test**

Add an order lookup helper test.

```ts
it('finds a target order by route id', () => {
  expect(buildOrderDetailTarget(sampleOrders, '101')?.id).toBe(101)
  expect(buildOrderDetailTarget(sampleOrders, '999')).toBeUndefined()
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: FAIL because the order detail helper is missing.

- [ ] **Step 3: Write minimal implementation**

Add the helper and build the detail view by reusing the existing order list response and action handlers.

```ts
export function buildOrderDetailTarget(
  orders: Array<{ id: number }>,
  routeId: string,
) {
  const targetId = Number(routeId)
  return orders.find((order) => order.id === targetId)
}
```

```vue
<template>
  <section v-if="order" class="detail-stack">
    <article class="cc-card detail-card">
      <h1>{{ order.orderStatus }}</h1>
      <p>订单号 {{ order.orderNo }}</p>
    </article>
  </section>
  <el-empty v-else description="未找到订单" />
</template>
```

Update navigation points:

```ts
router.push(`/student/orders/${order.id}`)
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/views/student/StudentOrderDetailView.vue campus-catering-web/src/views/student/StudentOrdersView.vue campus-catering-web/src/views/student/StudentMessagesView.vue campus-catering-web/src/views/student/StudentPaymentResultView.vue campus-catering-web/src/views/student/student-profile.ts campus-catering-web/src/views/student/student-profile.spec.ts
git commit -m "feat: add student order detail page"
```

### Task 5: Final Verification Sweep

**Files:**
- Modify: none
- Test: `campus-catering-web/src/views/student/student-profile.spec.ts`
- Test: `campus-catering-web/src/views/workspace/merchant-product-editor.spec.ts`

- [ ] **Step 1: Run student helper tests**

Run:

```bash
npm run test -- src/views/student/student-profile.spec.ts
```

Expected: PASS

- [ ] **Step 2: Re-run merchant editor regression tests**

Run:

```bash
npm run test -- src/views/workspace/merchant-product-editor.spec.ts
```

Expected: PASS

- [ ] **Step 3: Run frontend build**

Run:

```bash
npm run build
```

Expected: PASS, with only the existing bundle-size warning if any.

- [ ] **Step 4: Smoke check key student flows manually**

Verify in browser:

```txt
1. Student nav shows “我的” and opens the profile page.
2. Profile page links to orders, messages, addresses, and coupons.
3. Coupon center can claim a coupon and refresh state.
4. Order list can open `/student/orders/:id`.
5. Message and payment result pages can open the order detail route.
6. Order detail still supports the key order actions.
```

- [ ] **Step 5: Commit final integration**

```bash
git add campus-catering-web/src/router/index.ts campus-catering-web/src/views/student/StudentLayout.vue campus-catering-web/src/views/student/StudentProfileView.vue campus-catering-web/src/views/student/StudentCouponsView.vue campus-catering-web/src/views/student/StudentOrderDetailView.vue campus-catering-web/src/views/student/StudentOrdersView.vue campus-catering-web/src/views/student/StudentMessagesView.vue campus-catering-web/src/views/student/StudentPaymentResultView.vue campus-catering-web/src/views/student/student-profile.ts campus-catering-web/src/views/student/student-profile.spec.ts
git commit -m "feat: complete student personal area"
```
