# Merchant Order Efficiency Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve merchant-side order handling efficiency by adding top summary cards, focused board filtering, and actionable-order-first sorting on the merchant order management page.

**Architecture:** Keep the existing merchant order APIs and mutation flow unchanged. Add one focused helper module to own board counts, filter rules, status priority sorting, and empty-state copy, then rework the current `OrdersView` to render a summary-card shell on top of the existing order/exception actions.

**Tech Stack:** Vue 3, TypeScript, Element Plus, Vite, Vitest

---

## File Map

- Modify: `campus-catering-web/src/views/workspace/OrdersView.vue`
  Purpose: add summary cards, active board state, filtered order rendering, and improved order card layout.
- Create: `campus-catering-web/src/views/workspace/merchant-orders.ts`
  Purpose: centralize merchant order board counts, filtering, sorting, labels, and empty-state helpers.
- Create: `campus-catering-web/src/views/workspace/merchant-orders.spec.ts`
  Purpose: verify board counts, board filtering, status priority sorting, and empty-state behavior.

## Task 1: Add Merchant Order Board Helpers

**Files:**
- Create: `campus-catering-web/src/views/workspace/merchant-orders.ts`
- Create: `campus-catering-web/src/views/workspace/merchant-orders.spec.ts`

- [ ] **Step 1: Write the failing test**

Create helper fixtures and assertions that lock the new board behavior before implementation.

```ts
import { describe, expect, it } from 'vitest'
import type { OrderDetailResponse } from '@/api/student'
import type { OrderExceptionResponse } from '@/api/merchant'
import {
  buildMerchantOrderBoards,
  filterMerchantOrdersByBoard,
  getMerchantOrderEmptyDescription,
  sortMerchantOrdersByPriority,
  type MerchantOrderBoardKey,
} from '@/views/workspace/merchant-orders'

const sampleOrders: OrderDetailResponse[] = [
  {
    id: 11,
    orderNo: '202604070011',
    orderStatus: 'PAID',
    storeId: 1,
    storeName: '一食堂',
    payAmount: 18,
    receiverName: '张三',
    receiverPhone: '13800000001',
    receiverAddress: '南苑 1 栋 101',
    remark: '少辣',
    createdAt: '2026-04-07T10:05:00',
    items: [],
  },
  {
    id: 12,
    orderNo: '202604070012',
    orderStatus: 'ACCEPTED',
    storeId: 1,
    storeName: '一食堂',
    payAmount: 22,
    receiverName: '李四',
    receiverPhone: '13800000002',
    receiverAddress: '南苑 2 栋 202',
    remark: '',
    createdAt: '2026-04-07T10:03:00',
    items: [],
  },
  {
    id: 13,
    orderNo: '202604070013',
    orderStatus: 'PREPARING',
    storeId: 1,
    storeName: '一食堂',
    payAmount: 25,
    receiverName: '王五',
    receiverPhone: '13800000003',
    receiverAddress: '南苑 3 栋 303',
    remark: '不要香菜',
    createdAt: '2026-04-07T10:04:00',
    items: [],
  },
  {
    id: 14,
    orderNo: '202604070014',
    orderStatus: 'COMPLETED',
    storeId: 1,
    storeName: '一食堂',
    payAmount: 16,
    receiverName: '赵六',
    receiverPhone: '13800000004',
    receiverAddress: '南苑 4 栋 404',
    remark: '',
    createdAt: '2026-04-07T09:30:00',
    items: [],
  },
  {
    id: 15,
    orderNo: '202604070015',
    orderStatus: 'CANCELLED',
    storeId: 1,
    storeName: '一食堂',
    payAmount: 14,
    receiverName: '钱七',
    receiverPhone: '13800000005',
    receiverAddress: '南苑 5 栋 505',
    remark: '',
    createdAt: '2026-04-07T09:10:00',
    items: [],
  },
]

const sampleExceptions: OrderExceptionResponse[] = [
  {
    id: 1,
    orderId: 11,
    orderNo: 202604070011,
    orderStatus: 'PAID',
    payAmount: 18,
    reason: '骑手未接单',
    status: 'PROCESSING',
    createdAt: '2026-04-07T10:20:00',
    updatedAt: '2026-04-07T10:20:00',
  },
]

describe('merchant order helpers', () => {
  it('builds board counts from normal orders and exceptions', () => {
    expect(buildMerchantOrderBoards(sampleOrders, sampleExceptions, 'paid')).toEqual([
      { key: 'all', label: '全部订单', count: 5, active: false },
      { key: 'paid', label: '待接单', count: 1, active: true },
      { key: 'preparing', label: '备餐中', count: 2, active: false },
      { key: 'exception', label: '异常单', count: 1, active: false },
    ])
  })

  it('filters orders for each board', () => {
    expect(filterMerchantOrdersByBoard(sampleOrders, 'all').map((item) => item.id)).toEqual([11, 12, 13, 14, 15])
    expect(filterMerchantOrdersByBoard(sampleOrders, 'paid').map((item) => item.id)).toEqual([11])
    expect(filterMerchantOrdersByBoard(sampleOrders, 'preparing').map((item) => item.id)).toEqual([12, 13])
  })

  it('sorts orders by status priority before time', () => {
    expect(sortMerchantOrdersByPriority(sampleOrders).map((item) => item.id)).toEqual([11, 12, 13, 14, 15])
  })

  it('returns board-specific empty descriptions', () => {
    expect(getMerchantOrderEmptyDescription('paid')).toBe('当前没有待接单订单')
    expect(getMerchantOrderEmptyDescription('preparing')).toBe('当前没有备餐中订单')
    expect(getMerchantOrderEmptyDescription('exception')).toBe('当前没有异常订单')
    expect(getMerchantOrderEmptyDescription('all')).toBe('暂无订单')
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
npm run test -- src/views/workspace/merchant-orders.spec.ts
```

Expected: FAIL because `merchant-orders.ts` and its exports do not exist yet.

- [ ] **Step 3: Write minimal implementation**

Create `merchant-orders.ts` with focused board helpers and no UI code.

```ts
import type { OrderExceptionResponse } from '@/api/merchant'
import type { OrderDetailResponse } from '@/api/student'

export type MerchantOrderBoardKey = 'all' | 'paid' | 'preparing' | 'exception'

export type MerchantOrderBoard = {
  key: MerchantOrderBoardKey
  label: string
  count: number
  active: boolean
}

const preparingStatuses = new Set(['ACCEPTED', 'PREPARING'])

const statusPriority: Record<string, number> = {
  PAID: 1,
  ACCEPTED: 2,
  PREPARING: 3,
  WAITING_DELIVERY: 4,
  DELIVERING: 5,
  COMPLETED: 6,
  CANCELLED: 7,
}

export function buildMerchantOrderBoards(
  orders: OrderDetailResponse[],
  exceptions: OrderExceptionResponse[],
  activeBoard: MerchantOrderBoardKey,
): MerchantOrderBoard[] {
  return [
    { key: 'all', label: '全部订单', count: orders.length, active: activeBoard === 'all' },
    {
      key: 'paid',
      label: '待接单',
      count: orders.filter((item) => item.orderStatus === 'PAID').length,
      active: activeBoard === 'paid',
    },
    {
      key: 'preparing',
      label: '备餐中',
      count: orders.filter((item) => preparingStatuses.has(item.orderStatus)).length,
      active: activeBoard === 'preparing',
    },
    { key: 'exception', label: '异常单', count: exceptions.length, active: activeBoard === 'exception' },
  ]
}

export function sortMerchantOrdersByPriority(orders: OrderDetailResponse[]) {
  return [...orders].sort((left, right) => {
    const leftPriority = statusPriority[left.orderStatus] ?? 99
    const rightPriority = statusPriority[right.orderStatus] ?? 99
    if (leftPriority !== rightPriority) {
      return leftPriority - rightPriority
    }
    return String(right.createdAt).localeCompare(String(left.createdAt))
  })
}

export function filterMerchantOrdersByBoard(
  orders: OrderDetailResponse[],
  activeBoard: MerchantOrderBoardKey,
) {
  const sorted = sortMerchantOrdersByPriority(orders)
  if (activeBoard === 'paid') {
    return sorted.filter((item) => item.orderStatus === 'PAID')
  }
  if (activeBoard === 'preparing') {
    return sorted.filter((item) => preparingStatuses.has(item.orderStatus))
  }
  return sorted
}

export function getMerchantOrderEmptyDescription(activeBoard: MerchantOrderBoardKey) {
  if (activeBoard === 'paid') {
    return '当前没有待接单订单'
  }
  if (activeBoard === 'preparing') {
    return '当前没有备餐中订单'
  }
  if (activeBoard === 'exception') {
    return '当前没有异常订单'
  }
  return '暂无订单'
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
npm run test -- src/views/workspace/merchant-orders.spec.ts
```

Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add campus-catering-web/src/views/workspace/merchant-orders.ts campus-catering-web/src/views/workspace/merchant-orders.spec.ts
git commit -m "feat: add merchant order board helpers"
```

## Task 2: Rebuild The Merchant Orders Page Around Summary Cards

**Files:**
- Modify: `campus-catering-web/src/views/workspace/OrdersView.vue`
- Modify: `campus-catering-web/src/views/workspace/merchant-orders.spec.ts`

- [ ] **Step 1: Extend the failing test with formatting and status expectations**

Add a second batch of assertions so the page wiring can reuse helper-owned copy instead of hardcoding everything inside the view.

```ts
import {
  formatMerchantOrderPrice,
  formatMerchantOrderTime,
  getMerchantOrderStatusLabel,
  getMerchantOrderStatusType,
} from '@/views/workspace/merchant-orders'

it('formats merchant-facing status and text labels', () => {
  expect(getMerchantOrderStatusLabel('PAID')).toBe('待接单')
  expect(getMerchantOrderStatusLabel('PREPARING')).toBe('备餐中')
  expect(getMerchantOrderStatusLabel('UNKNOWN')).toBe('UNKNOWN')
  expect(getMerchantOrderStatusType('COMPLETED')).toBe('success')
  expect(getMerchantOrderStatusType('CANCELLED')).toBe('danger')
  expect(getMerchantOrderStatusType('PAID')).toBe('primary')
  expect(formatMerchantOrderPrice(18)).toBe('18.00元')
  expect(formatMerchantOrderTime('2026-04-07T10:05:00')).toBe('2026-04-07 10:05')
})
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
npm run test -- src/views/workspace/merchant-orders.spec.ts
```

Expected: FAIL because the new formatting helpers do not exist yet.

- [ ] **Step 3: Add the missing formatting exports**

Extend `merchant-orders.ts` with status and formatting helpers used by `OrdersView.vue`.

```ts
export function getMerchantOrderStatusLabel(status: string) {
  const map: Record<string, string> = {
    PENDING_PAYMENT: '待支付',
    PAID: '待接单',
    ACCEPTED: '已接单',
    PREPARING: '备餐中',
    WAITING_DELIVERY: '待配送',
    DELIVERING: '配送中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[status] ?? status
}

export function getMerchantOrderStatusType(status: string) {
  if (status === 'COMPLETED') {
    return 'success'
  }
  if (status === 'CANCELLED') {
    return 'danger'
  }
  if (status === 'PAID' || status === 'ACCEPTED' || status === 'PREPARING') {
    return 'primary'
  }
  return 'warning'
}

export function formatMerchantOrderPrice(value?: number) {
  return `${Number(value ?? 0).toFixed(2)}元`
}

export function formatMerchantOrderTime(raw: string) {
  return String(raw || '').replace('T', ' ').slice(0, 16)
}
```

- [ ] **Step 4: Rework `OrdersView.vue` to render summary cards and board-specific content**

Update the page structure to keep current actions while adding the new card row and filtered list state.

```vue
<section class="boards">
  <button
    v-for="board in boards"
    :key="board.key"
    type="button"
    class="board-card"
    :class="{ 'board-card--active': board.active }"
    @click="activeBoard = board.key"
  >
    <span>{{ board.label }}</span>
    <strong>{{ board.count }}</strong>
  </button>
</section>

<section v-if="activeBoard !== 'exception'" class="list cc-card">
  <el-skeleton v-if="loading" :rows="8" animated />

  <template v-else>
    <article v-for="row in visibleOrders" :key="row.id" class="order-card">
      <div class="order-card__main">
        <div>
          <h3>订单号 {{ row.orderNo }}</h3>
          <p>{{ formatMerchantOrderTime(row.createdAt) }} · {{ row.receiverAddress }}</p>
        </div>
        <strong>{{ formatMerchantOrderPrice(row.payAmount) }}</strong>
        <el-tag :type="getMerchantOrderStatusType(row.orderStatus)" round>
          {{ getMerchantOrderStatusLabel(row.orderStatus) }}
        </el-tag>
      </div>

      <div class="order-card__items">
        <p v-for="item in row.items" :key="`${row.id}-${item.skuId}`">
          {{ item.spuName }} / {{ item.skuName }} × {{ item.quantity }}
        </p>
        <p v-if="row.remark" class="order-card__remark">备注：{{ row.remark }}</p>
      </div>

      <div class="action-buttons">
        <template v-for="action in actionsFor(row.orderStatus)" :key="action.key">
          <el-button size="small" round :type="action.type" @click="handleAction(action.key, row.id)">
            {{ action.label }}
          </el-button>
        </template>
        <el-button
          v-if="['PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING'].includes(row.orderStatus)"
          size="small"
          round
          type="warning"
          @click="reportException(row.id)"
        >
          上报异常
        </el-button>
      </div>
    </article>

    <el-empty v-if="visibleOrders.length === 0" :description="emptyDescription" />
  </template>
</section>
```

Wire the state in the script:

```ts
import { computed, onMounted, ref } from 'vue'
import {
  buildMerchantOrderBoards,
  filterMerchantOrdersByBoard,
  formatMerchantOrderPrice,
  formatMerchantOrderTime,
  getMerchantOrderEmptyDescription,
  getMerchantOrderStatusLabel,
  getMerchantOrderStatusType,
  type MerchantOrderBoardKey,
} from '@/views/workspace/merchant-orders'

const activeBoard = ref<MerchantOrderBoardKey>('paid')

const boards = computed(() => buildMerchantOrderBoards(orders.value, exceptions.value, activeBoard.value))
const visibleOrders = computed(() => filterMerchantOrdersByBoard(orders.value, activeBoard.value))
const emptyDescription = computed(() => getMerchantOrderEmptyDescription(activeBoard.value))
```

Keep the exception list in the page, but only render it when the active board is `exception`.

```vue
<section v-else class="cc-card exception-wrap">
  <div class="exception-head">
    <h3>异常订单</h3>
    <el-button size="small" round @click="loadExceptions">刷新</el-button>
  </div>
  <div v-if="exceptions.length === 0" class="empty">{{ emptyDescription }}</div>
  <div v-for="row in exceptions" :key="row.id" class="exception-line">
    <div>
      <h4>订单号 {{ row.orderNo }}</h4>
      <p>{{ row.reason }}</p>
    </div>
    <el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'" round>
      {{ row.status === 'RESOLVED' ? '已处理' : '处理中' }}
    </el-tag>
    <el-button v-if="row.status !== 'RESOLVED'" size="small" type="primary" @click="resolveException(row.orderId)">
      处理完成
    </el-button>
  </div>
</section>
```

- [ ] **Step 5: Re-run the helper test to verify it still passes**

Run:

```bash
npm run test -- src/views/workspace/merchant-orders.spec.ts
```

Expected: PASS

- [ ] **Step 6: Run frontend build to verify the updated page compiles**

Run:

```bash
npm run build
```

Expected: PASS, with at most the existing large chunk warning.

- [ ] **Step 7: Commit**

```bash
git add campus-catering-web/src/views/workspace/OrdersView.vue campus-catering-web/src/views/workspace/merchant-orders.ts campus-catering-web/src/views/workspace/merchant-orders.spec.ts
git commit -m "feat: improve merchant order efficiency layout"
```

## Task 3: Final Verification Sweep

**Files:**
- Verify only

- [ ] **Step 1: Re-read the merchant order efficiency spec and check coverage**

Checklist:

- Top summary cards exist
- Default board is `待接单`
- `备餐中` merges `ACCEPTED` and `PREPARING`
- `异常单` is a focused view, not mixed into normal orders
- Normal orders are sorted by actionable priority before historical statuses
- Existing accept, reject, prepare, finish, and exception actions still exist

- [ ] **Step 2: Run merchant order helper tests**

Run:

```bash
npm run test -- src/views/workspace/merchant-orders.spec.ts
```

Expected: PASS

- [ ] **Step 3: Run production build**

Run:

```bash
npm run build
```

Expected: PASS, with only the existing bundle-size warning if any.

- [ ] **Step 4: Smoke check the merchant order page manually**

Verify in browser:

```txt
1. Page first opens on the 待接单 board.
2. Top cards show counts for 全部订单 / 待接单 / 备餐中 / 异常单.
3. Clicking 待接单 only shows PAID orders.
4. Clicking 备餐中 only shows ACCEPTED and PREPARING orders.
5. Clicking 异常单 hides the normal order list and shows only exception rows.
6. Accept / reject / prepare / finish / exception actions still refresh the page state correctly.
```

- [ ] **Step 5: Commit final integration**

```bash
git add campus-catering-web/src/views/workspace/OrdersView.vue campus-catering-web/src/views/workspace/merchant-orders.ts campus-catering-web/src/views/workspace/merchant-orders.spec.ts docs/superpowers/specs/2026-04-07-merchant-order-efficiency-design.md docs/superpowers/plans/2026-04-07-merchant-order-efficiency.md
git commit -m "feat: improve merchant order management efficiency"
```

## Self-Review

### Spec coverage

- Summary-card row: covered by Task 2
- Default `待接单` focus: covered by Task 2
- Frontend-only aggregation and filtering: covered by Tasks 1 and 2
- Exception-only view mode: covered by Task 2
- Lightweight test coverage: covered by Tasks 1, 2, and 3

### Placeholder scan

- No `TODO`, `TBD`, or deferred implementation placeholders remain
- All file paths are explicit
- Verification steps use concrete commands and expected results

### Type consistency

- `MerchantOrderBoardKey` is defined before later tasks reference it
- Board keys stay consistent across tests, helper functions, and the view
- Status labels and formatting are centralized in one helper module before `OrdersView.vue` consumes them
