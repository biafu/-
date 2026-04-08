# Student Checkout Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the student-side cart, checkout, and payment-result flow on top of the existing backend contracts, while adding the missing cart update, delete, and clear endpoints needed for a usable cart page.

**Architecture:** Keep the flow split across independent student routes: cart, checkout, and payment result. Backend cart operations stay in the existing `cart` module with thin controller endpoints over `CartService`; frontend state is centralized in a lightweight checkout store so the first version can later grow into address switching, coupon selection, and remark support without rewriting the flow.

**Tech Stack:** Spring Boot 3.2, MyBatis, JUnit 5 / Spring Boot Test, Vue 3, Vite, Pinia, Vue Router, Element Plus, TypeScript

---

## File Map

### Backend

- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/controller/CartController.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/service/CartService.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/mapper/ShoppingCartMapper.java`
- Create: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/dto/CartQuantityUpdateRequest.java`
- Create: `campus-catering-server/src/test/java/com/zhinengpt/campuscatering/cart/service/CartServiceTest.java`

### Frontend

- Modify: `campus-catering-web/package.json`
- Modify: `campus-catering-web/src/router/index.ts`
- Modify: `campus-catering-web/src/api/student.ts`
- Modify: `campus-catering-web/src/views/student/StudentLayout.vue`
- Modify: `campus-catering-web/src/views/student/StudentStoreDetailView.vue`
- Create: `campus-catering-web/src/stores/student-checkout.ts`
- Create: `campus-catering-web/src/components/student/StudentCartStoreSection.vue`
- Create: `campus-catering-web/src/components/student/StudentCheckoutAddressCard.vue`
- Create: `campus-catering-web/src/components/student/StudentOrderAmountCard.vue`
- Create: `campus-catering-web/src/components/student/StudentPaymentSummaryCard.vue`
- Create: `campus-catering-web/src/views/student/StudentCartView.vue`
- Create: `campus-catering-web/src/views/student/StudentCheckoutView.vue`
- Create: `campus-catering-web/src/views/student/StudentPaymentResultView.vue`
- Create: `campus-catering-web/src/stores/student-checkout.spec.ts`

## Task 1: Add backend tests for missing cart operations

**Files:**
- Create: `campus-catering-server/src/test/java/com/zhinengpt/campuscatering/cart/service/CartServiceTest.java`
- Modify later: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/service/CartService.java`
- Modify later: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/mapper/ShoppingCartMapper.java`

- [ ] **Step 1: Write the failing cart service tests**

```java
package com.zhinengpt.campuscatering.cart.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zhinengpt.campuscatering.cart.entity.ShoppingCart;
import com.zhinengpt.campuscatering.cart.mapper.ShoppingCartMapper;
import com.zhinengpt.campuscatering.common.exception.BusinessException;
import com.zhinengpt.campuscatering.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ShoppingCartMapper shoppingCartMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    @Test
    void updateQuantity_updatesExistingCartItem() {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(8L);
        cart.setUserId(1001L);
        cart.setSkuId(501L);
        cart.setQuantity(2);

        when(shoppingCartMapper.selectByIdAndUserId(8L, 1001L)).thenReturn(cart);

        cartService.updateQuantity(1001L, 8L, 5);

        verify(shoppingCartMapper).updateQuantity(cart);
    }

    @Test
    void updateQuantity_rejectsNonPositiveQuantity() {
        assertThrows(BusinessException.class, () -> cartService.updateQuantity(1001L, 8L, 0));
        verify(shoppingCartMapper, never()).updateQuantity(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void removeItem_deletesOwnedCartItem() {
        when(shoppingCartMapper.deleteByIdAndUserId(9L, 1001L)).thenReturn(1);

        cartService.removeItem(1001L, 9L);

        verify(shoppingCartMapper).deleteByIdAndUserId(9L, 1001L);
    }

    @Test
    void removeItem_rejectsMissingCartItem() {
        when(shoppingCartMapper.deleteByIdAndUserId(9L, 1001L)).thenReturn(0);

        assertThrows(BusinessException.class, () -> cartService.removeItem(1001L, 9L));
    }

    @Test
    void clearStoreCart_clearsOnlySpecifiedStore() {
        cartService.clearStoreCart(1001L, 66L);

        verify(shoppingCartMapper).deleteByUserIdAndStoreId(1001L, 66L);
    }
}
```

- [ ] **Step 2: Run the backend cart tests to verify they fail**

Run: `mvn -Dtest=CartServiceTest test`

Expected: FAIL with compilation errors because `selectByIdAndUserId`, `deleteByIdAndUserId`, `updateQuantity(userId, cartId, quantity)`, or `removeItem` do not exist yet.

- [ ] **Step 3: Implement the minimal backend support**

```java
// CartQuantityUpdateRequest.java
package com.zhinengpt.campuscatering.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartQuantityUpdateRequest {

    @NotNull
    @Min(1)
    private Integer quantity;
}
```

```java
// ShoppingCartMapper.java additions
@Select("""
        select id, user_id, store_id, sku_id, quantity, created_at, updated_at
        from shopping_cart
        where id = #{id} and user_id = #{userId}
        limit 1
        """)
ShoppingCart selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

@Delete("delete from shopping_cart where id = #{id} and user_id = #{userId}")
int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
```

```java
// CartService.java additions
public void updateQuantity(Long userId, Long cartId, Integer quantity) {
    if (quantity == null || quantity < 1) {
        throw new BusinessException("购物车数量至少为1");
    }
    ShoppingCart existed = shoppingCartMapper.selectByIdAndUserId(cartId, userId);
    if (existed == null) {
        throw new BusinessException("购物车商品不存在");
    }
    existed.setQuantity(quantity);
    shoppingCartMapper.updateQuantity(existed);
}

public void removeItem(Long userId, Long cartId) {
    int deleted = shoppingCartMapper.deleteByIdAndUserId(cartId, userId);
    if (deleted == 0) {
        throw new BusinessException("购物车商品不存在");
    }
}
```

- [ ] **Step 4: Run the backend cart tests to verify they pass**

Run: `mvn -Dtest=CartServiceTest test`

Expected: PASS with 5 tests run, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/controller/CartController.java campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/service/CartService.java campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/mapper/ShoppingCartMapper.java campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/dto/CartQuantityUpdateRequest.java campus-catering-server/src/test/java/com/zhinengpt/campuscatering/cart/service/CartServiceTest.java
git commit -m "feat: add student cart mutation endpoints"
```

## Task 2: Expose the backend cart mutation endpoints

**Files:**
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/controller/CartController.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/service/CartService.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/dto/CartQuantityUpdateRequest.java`

- [ ] **Step 1: Write the failing controller-level expectations**

```java
// Add to CartServiceTest only if controller tests are skipped:
// "CartController should expose update, delete, and clear routes"
//
// If writing a dedicated controller test instead:
// - PATCH /api/student/cart/{id}/quantity with {"quantity": 3}
// - DELETE /api/student/cart/{id}
// - DELETE /api/student/cart/store/{storeId}
```

- [ ] **Step 2: Run the backend tests to verify the route expectations fail**

Run: `mvn -Dtest=CartServiceTest test`

Expected: FAIL until controller methods are added, or no route exists for frontend integration yet.

- [ ] **Step 3: Add the controller endpoints**

```java
// CartController.java imports
import com.zhinengpt.campuscatering.cart.dto.CartQuantityUpdateRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
```

```java
// CartController.java methods
@PatchMapping("/{id}/quantity")
public ApiResponse<Void> updateQuantity(@PathVariable Long id, @Valid @RequestBody CartQuantityUpdateRequest request) {
    cartService.updateQuantity(SecurityUtils.getLoginUser().getUserId(), id, request.getQuantity());
    return ApiResponse.success();
}

@DeleteMapping("/{id}")
public ApiResponse<Void> remove(@PathVariable Long id) {
    cartService.removeItem(SecurityUtils.getLoginUser().getUserId(), id);
    return ApiResponse.success();
}

@DeleteMapping("/store/{storeId}")
public ApiResponse<Void> clearStore(@PathVariable Long storeId) {
    cartService.clearStoreCart(SecurityUtils.getLoginUser().getUserId(), storeId);
    return ApiResponse.success();
}
```

- [ ] **Step 4: Run the backend cart tests again**

Run: `mvn -Dtest=CartServiceTest test`

Expected: PASS. If a controller test was added, it should also pass with HTTP 200 and `code = 0`.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/controller/CartController.java campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/service/CartService.java campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/mapper/ShoppingCartMapper.java campus-catering-server/src/main/java/com/zhinengpt/campuscatering/cart/dto/CartQuantityUpdateRequest.java campus-catering-server/src/test/java/com/zhinengpt/campuscatering/cart/service/CartServiceTest.java
git commit -m "feat: expose student cart quantity and delete APIs"
```

## Task 3: Add frontend checkout store coverage and API wrappers

**Files:**
- Modify: `campus-catering-web/package.json`
- Modify: `campus-catering-web/src/api/student.ts`
- Create: `campus-catering-web/src/stores/student-checkout.ts`
- Create: `campus-catering-web/src/stores/student-checkout.spec.ts`

- [ ] **Step 1: Write the failing frontend store tests**

```ts
import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useStudentCheckoutStore } from './student-checkout'

describe('student checkout store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('builds a checkout draft from a single store cart group', () => {
    const store = useStudentCheckoutStore()

    store.setCartItems([
      { id: 1, storeId: 10, skuId: 101, productName: '黄焖鸡', skuName: '大份', price: 18, quantity: 2, totalAmount: 36 },
      { id: 2, storeId: 10, skuId: 102, productName: '可乐', skuName: '常规', price: 3, quantity: 1, totalAmount: 3 },
    ])

    store.beginCheckout(10)

    expect(store.checkoutDraft).toEqual({
      storeId: 10,
      items: [
        { skuId: 101, quantity: 2 },
        { skuId: 102, quantity: 1 },
      ],
      receiverName: '',
      receiverPhone: '',
      receiverAddress: '',
      userCouponId: undefined,
      remark: '',
    })
  })

  it('tracks total cart item count for navigation badge', () => {
    const store = useStudentCheckoutStore()
    store.setCartItems([
      { id: 1, storeId: 10, skuId: 101, productName: '黄焖鸡', skuName: '大份', price: 18, quantity: 2, totalAmount: 36 },
      { id: 2, storeId: 11, skuId: 201, productName: '牛肉面', skuName: '标准', price: 16, quantity: 1, totalAmount: 16 },
    ])

    expect(store.totalCount).toBe(3)
  })
})
```

- [ ] **Step 2: Run the frontend store tests to verify they fail**

Run: `npm run test -- student-checkout.spec.ts`

Expected: FAIL because no test command, no Vitest dependencies, and no `student-checkout` store yet.

- [ ] **Step 3: Add the minimal frontend test and store scaffolding**

```json
// package.json scripts/devDependencies additions
{
  "scripts": {
    "test": "vitest run"
  },
  "devDependencies": {
    "@vue/test-utils": "^2.4.6",
    "jsdom": "^24.1.0",
    "vitest": "^2.1.1"
  }
}
```

```ts
// student-checkout.ts
import { defineStore } from 'pinia'

export type StudentCartItem = {
  id: number
  storeId: number
  skuId: number
  productName: string
  skuName: string
  price: number
  quantity: number
  totalAmount: number
}

export const useStudentCheckoutStore = defineStore('student-checkout', {
  state: () => ({
    cartItems: [] as StudentCartItem[],
    checkoutDraft: null as null | {
      storeId: number
      items: Array<{ skuId: number; quantity: number }>
      receiverName: string
      receiverPhone: string
      receiverAddress: string
      userCouponId?: number
      remark?: string
    },
    previewResult: null as unknown,
    submitting: false,
    paying: false,
  }),
  getters: {
    totalCount: (state) => state.cartItems.reduce((sum, item) => sum + Number(item.quantity ?? 0), 0),
    cartGroups: (state) => {
      const groupMap = new Map<number, StudentCartItem[]>()
      state.cartItems.forEach((item) => {
        const group = groupMap.get(item.storeId) ?? []
        group.push(item)
        groupMap.set(item.storeId, group)
      })
      return Array.from(groupMap.entries()).map(([storeId, items]) => ({ storeId, items }))
    },
  },
  actions: {
    setCartItems(items: StudentCartItem[]) {
      this.cartItems = items
    },
    beginCheckout(storeId: number) {
      const selectedItems = this.cartItems.filter((item) => item.storeId === storeId)
      this.checkoutDraft = {
        storeId,
        items: selectedItems.map((item) => ({ skuId: item.skuId, quantity: item.quantity })),
        receiverName: '',
        receiverPhone: '',
        receiverAddress: '',
        userCouponId: undefined,
        remark: '',
      }
    },
  },
})
```

```ts
// student.ts additions
export type CartItemResponse = {
  id: number
  storeId: number
  skuId: number
  productName: string
  skuName: string
  price: number
  quantity: number
  totalAmount: number
}

export type OrderItemRequest = {
  skuId: number
  quantity: number
}

export type OrderPreviewRequest = {
  storeId: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  userCouponId?: number
  remark?: string
  items: OrderItemRequest[]
}

export type OrderPreviewResponse = {
  goodsAmount: number
  deliveryFee: number
  discountAmount: number
  payAmount: number
  items: Array<{ skuId: number; skuName: string; quantity: number; price: number; totalAmount: number }>
}

export type OrderCreateResponse = {
  orderId: number
  orderNo: number
  orderStatus: string
  payAmount: number
}

export function fetchCartList() {
  return request<CartItemResponse[]>('/api/student/cart', { auth: true })
}

export function updateCartQuantity(id: number, quantity: number) {
  return request<void>(`/api/student/cart/${id}/quantity`, {
    method: 'PATCH',
    body: { quantity },
    auth: true,
  })
}

export function deleteCartItem(id: number) {
  return request<void>(`/api/student/cart/${id}`, {
    method: 'DELETE',
    auth: true,
  })
}

export function clearStoreCart(storeId: number) {
  return request<void>(`/api/student/cart/store/${storeId}`, {
    method: 'DELETE',
    auth: true,
  })
}

export function previewStudentOrder(payload: OrderPreviewRequest) {
  return request<OrderPreviewResponse>('/api/student/order/preview', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}

export function createStudentOrder(payload: OrderPreviewRequest) {
  return request<OrderCreateResponse>('/api/student/order/create', {
    method: 'POST',
    body: payload,
    auth: true,
  })
}
```

- [ ] **Step 4: Run the frontend store tests to verify they pass**

Run: `npm install`

Run: `npm run test -- student-checkout.spec.ts`

Expected: PASS with 2 tests run, 0 failures.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/package.json campus-catering-web/package-lock.json campus-catering-web/src/api/student.ts campus-catering-web/src/stores/student-checkout.ts campus-catering-web/src/stores/student-checkout.spec.ts
git commit -m "feat: add student checkout store and api wrappers"
```

## Task 4: Build the student cart page

**Files:**
- Create: `campus-catering-web/src/components/student/StudentCartStoreSection.vue`
- Create: `campus-catering-web/src/views/student/StudentCartView.vue`
- Modify: `campus-catering-web/src/router/index.ts`
- Modify: `campus-catering-web/src/views/student/StudentLayout.vue`
- Modify: `campus-catering-web/src/views/student/StudentStoreDetailView.vue`

- [ ] **Step 1: Write the failing cart page interaction test**

```ts
import { beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useStudentCheckoutStore } from '@/stores/student-checkout'

describe('student cart workflow', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('begins checkout for a selected store group', () => {
    const store = useStudentCheckoutStore()
    store.setCartItems([
      { id: 1, storeId: 10, skuId: 101, productName: '黄焖鸡', skuName: '大份', price: 18, quantity: 2, totalAmount: 36 },
      { id: 2, storeId: 11, skuId: 201, productName: '牛肉面', skuName: '标准', price: 16, quantity: 1, totalAmount: 16 },
    ])

    store.beginCheckout(11)

    expect(store.checkoutDraft?.storeId).toBe(11)
    expect(store.checkoutDraft?.items).toEqual([{ skuId: 201, quantity: 1 }])
  })
})
```

- [ ] **Step 2: Run the frontend tests to verify they fail for missing route/UI integration**

Run: `npm run test -- student-checkout.spec.ts`

Expected: FAIL once assertions are added for route-facing behavior not yet wired, or fail because the cart flow is not yet reachable from the UI.

- [ ] **Step 3: Implement the cart page and navigation wiring**

```ts
// router/index.ts additions under /student children
{
  path: 'cart',
  name: 'student-cart',
  meta: { requiresRole: 'STUDENT' },
  component: () => import('@/views/student/StudentCartView.vue'),
},
{
  path: 'checkout',
  name: 'student-checkout',
  meta: { requiresRole: 'STUDENT' },
  component: () => import('@/views/student/StudentCheckoutView.vue'),
},
{
  path: 'payment-result',
  name: 'student-payment-result',
  meta: { requiresRole: 'STUDENT' },
  component: () => import('@/views/student/StudentPaymentResultView.vue'),
},
```

```vue
<!-- StudentLayout.vue nav addition -->
<RouterLink to="/student/cart" class="tab">
  购物车
  <span v-if="cartCount > 0" class="badge">{{ cartCount > 99 ? '99+' : cartCount }}</span>
</RouterLink>
```

```ts
// StudentLayout.vue script addition
import { computed } from 'vue'
import { useStudentCheckoutStore } from '@/stores/student-checkout'

const checkoutStore = useStudentCheckoutStore()
const cartCount = computed(() => checkoutStore.totalCount)
```

```vue
<!-- StudentStoreDetailView.vue button addition -->
<el-button plain round @click="router.push('/student/cart')">去购物车</el-button>
```

```vue
<!-- StudentCartView.vue skeleton -->
<template>
  <section class="header glass-panel">
    <div>
      <h1 class="cc-section-title">购物车</h1>
      <p class="cc-section-subtitle">按店铺结算，先完成当前版本的单店闭环。</p>
    </div>
    <el-button round @click="loadCart">刷新</el-button>
  </section>

  <el-skeleton v-if="loading" :rows="6" animated />

  <section v-else-if="cartGroups.length === 0" class="cc-card empty-card">
    <el-empty description="购物车还是空的">
      <el-button type="primary" @click="router.push('/student/home')">去逛逛</el-button>
    </el-empty>
  </section>

  <section v-else class="list">
    <StudentCartStoreSection
      v-for="group in cartGroups"
      :key="group.storeId"
      :group="group"
      @change-quantity="changeQuantity"
      @remove-item="removeItem"
      @clear-store="clearGroup"
      @checkout="goCheckout"
    />
  </section>
</template>
```

- [ ] **Step 4: Run the frontend tests and type-check/build**

Run: `npm run test -- student-checkout.spec.ts`

Expected: PASS.

Run: `npm run build`

Expected: PASS with no TypeScript errors.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/router/index.ts campus-catering-web/src/views/student/StudentLayout.vue campus-catering-web/src/views/student/StudentStoreDetailView.vue campus-catering-web/src/views/student/StudentCartView.vue campus-catering-web/src/components/student/StudentCartStoreSection.vue
git commit -m "feat: add student cart page"
```

## Task 5: Build the checkout and payment-result pages

**Files:**
- Create: `campus-catering-web/src/components/student/StudentCheckoutAddressCard.vue`
- Create: `campus-catering-web/src/components/student/StudentOrderAmountCard.vue`
- Create: `campus-catering-web/src/components/student/StudentPaymentSummaryCard.vue`
- Create: `campus-catering-web/src/views/student/StudentCheckoutView.vue`
- Create: `campus-catering-web/src/views/student/StudentPaymentResultView.vue`
- Modify: `campus-catering-web/src/api/student.ts`
- Modify: `campus-catering-web/src/stores/student-checkout.ts`

- [ ] **Step 1: Write the failing checkout store test for address hydration**

```ts
it('hydrates checkout draft with default address before preview', () => {
  const store = useStudentCheckoutStore()

  store.checkoutDraft = {
    storeId: 10,
    items: [{ skuId: 101, quantity: 2 }],
    receiverName: '',
    receiverPhone: '',
    receiverAddress: '',
    remark: '',
  }

  store.fillReceiver({
    contactName: '张三',
    contactPhone: '13800138000',
    fullAddress: '上海理工大学 南校区 6号楼 302',
  })

  expect(store.checkoutDraft).toMatchObject({
    receiverName: '张三',
    receiverPhone: '13800138000',
    receiverAddress: '上海理工大学 南校区 6号楼 302',
  })
})
```

- [ ] **Step 2: Run the frontend tests to verify they fail**

Run: `npm run test -- student-checkout.spec.ts`

Expected: FAIL because `fillReceiver` and preview-related state are not implemented yet.

- [ ] **Step 3: Implement checkout and payment-result pages**

```ts
// student-checkout.ts action addition
fillReceiver(address: { contactName: string; contactPhone: string; fullAddress: string }) {
  if (!this.checkoutDraft) return
  this.checkoutDraft.receiverName = address.contactName
  this.checkoutDraft.receiverPhone = address.contactPhone
  this.checkoutDraft.receiverAddress = address.fullAddress
}
```

```vue
<!-- StudentCheckoutView.vue flow -->
<template>
  <section class="header glass-panel">
    <div>
      <h1 class="cc-section-title">确认订单</h1>
      <p class="cc-section-subtitle">正式试算后再提交订单，金额以接口返回为准。</p>
    </div>
    <el-button plain @click="router.push('/student/cart')">返回购物车</el-button>
  </section>

  <StudentCheckoutAddressCard
    :address="defaultAddress"
    :missing="!defaultAddress"
    @manage-address="router.push('/student/addresses')"
  />

  <section class="cc-card items-card">
    <h3>商品清单</h3>
    <ul>
      <li v-for="item in preview?.items ?? draftItems" :key="`${item.skuId}-${item.skuName}`">
        <span>{{ item.skuName }}</span>
        <strong>x{{ item.quantity }}</strong>
      </li>
    </ul>
  </section>

  <StudentOrderAmountCard :preview="preview" />

  <section class="footer-actions">
    <el-button type="primary" :disabled="!canSubmit" :loading="submitting" @click="submitOrder">提交订单</el-button>
  </section>
</template>
```

```vue
<!-- StudentPaymentResultView.vue flow -->
<template>
  <section class="header glass-panel">
    <div>
      <h1 class="cc-section-title">订单已创建</h1>
      <p class="cc-section-subtitle">可以立即支付，或者先去订单列表查看。</p>
    </div>
  </section>

  <StudentPaymentSummaryCard :order="paymentOrder" />

  <section class="actions">
    <el-button type="primary" :loading="paying" @click="payNow">立即支付</el-button>
    <el-button @click="router.push('/student/orders')">查看订单</el-button>
    <el-button plain @click="router.push('/student/home')">返回首页</el-button>
  </section>
</template>
```

- [ ] **Step 4: Run the frontend tests and build**

Run: `npm run test -- student-checkout.spec.ts`

Expected: PASS.

Run: `npm run build`

Expected: PASS with all three new student pages compiled.

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/views/student/StudentCheckoutView.vue campus-catering-web/src/views/student/StudentPaymentResultView.vue campus-catering-web/src/components/student/StudentCheckoutAddressCard.vue campus-catering-web/src/components/student/StudentOrderAmountCard.vue campus-catering-web/src/components/student/StudentPaymentSummaryCard.vue campus-catering-web/src/stores/student-checkout.ts campus-catering-web/src/stores/student-checkout.spec.ts campus-catering-web/src/api/student.ts
git commit -m "feat: add student checkout and payment result flow"
```

## Task 6: Full verification

**Files:**
- Verify only

- [ ] **Step 1: Run backend tests**

Run: `mvn -Dtest=CartServiceTest test`

Expected: PASS with 0 failures.

- [ ] **Step 2: Run frontend targeted tests**

Run: `npm run test -- student-checkout.spec.ts`

Expected: PASS with 0 failures.

- [ ] **Step 3: Run frontend production build**

Run: `npm run build`

Expected: PASS with Vite build output and no TypeScript errors.

- [ ] **Step 4: Re-read the spec and verify coverage**

Checklist:
- Cart page exists and is routable
- Cart supports quantity change, delete, and clear-store
- Checkout page hydrates from default address
- Checkout uses preview then create
- Payment-result page supports pay and order-list navigation
- Data model still includes `userCouponId` and `remark`

- [ ] **Step 5: Commit final integration changes**

```bash
git add campus-catering-server campus-catering-web docs/superpowers/specs/2026-04-06-student-checkout-flow-design.md docs/superpowers/plans/2026-04-06-student-checkout-flow.md
git commit -m "feat: complete student checkout flow"
```

## Self-Review

### Spec coverage

- Student cart page: covered by Task 4
- Checkout page: covered by Task 5
- Payment result page: covered by Task 5
- Missing backend cart operations: covered by Tasks 1 and 2
- Shared checkout state and extensible draft model: covered by Tasks 3 and 5
- Verification and build evidence: covered by Task 6

### Placeholder scan

- No `TODO`, `TBD`, or “implement later” placeholders remain
- Each task includes concrete files and concrete commands
- The only branch point is whether controller tests are kept separate; the implementation target paths remain explicit

### Type consistency

- Cart mutation route shapes match the frontend API wrapper paths
- `checkoutDraft` shape is consistent across the store and page tasks
- `OrderPreviewRequest` / `OrderCreateResponse` names match the existing backend contracts
