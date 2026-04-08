# Merchant Order Efficiency Design

## Goal

Improve merchant-side order handling efficiency by making urgent orders more visible on the order management page without changing the existing backend order state machine.

## Scope

This spec covers:

- Merchant order management page information hierarchy refresh
- Top summary cards for key handling states
- Frontend-only status aggregation, filtering, and priority sorting
- A focused exception-order view mode
- Lightweight test coverage for the new order-page rules

This spec does not cover:

- New backend order aggregation APIs
- Batch order actions
- Search, pagination, or export
- Delivery dispatch management changes
- Changes to merchant order state transition contracts

## Product Direction

Use a "top summary cards + focused list" model so merchants can see the most urgent work as soon as they open the page.

The page should no longer behave like one flat list where every order has equal visual weight. Instead, it should first answer:

- How many orders need attention right now?
- Which handling lane am I currently looking at?
- Which orders should I process first?

This keeps the page operationally simple while improving triage speed for merchants who are repeatedly checking the page during service hours.

## User Experience

### Page Structure

The order page should be divided into two layers:

1. A top summary-card row
2. A main content area below it

The summary-card row should include:

- `全部订单`
- `待接单`
- `备餐中`
- `异常单`

Each card shows a count and acts like a view switch. The active card must have clear visual emphasis.

### Default View

When the page first loads, the default active card should be `待接单`.

This ensures the first screen is immediately action-oriented rather than historical.

### Main Content Behavior

When the active card is:

- `全部订单`: show the normal merchant order list
- `待接单`: show only paid orders waiting for merchant acceptance
- `备餐中`: show accepted and preparing orders that still require merchant progress
- `异常单`: switch the content area to the exception-order list instead of mixing exception rows into the normal order list

### Order Row Information

The normal order list should keep current action buttons, but the row should better support quick confirmation before acting.

Keep these fields visible in the main row:

- Order number
- Order time
- Receiver address
- Pay amount
- Current status
- Available actions

Also show the ordered items and merchant remark in the card body so the merchant can confirm the order contents without leaving the page.

## Data Flow

### Read Path

The page continues to load from existing endpoints only:

- `fetchMerchantOrders()`
- `fetchMerchantExceptionOrders()`

On initial load and manual refresh, both requests should run together. The summary cards and the main list should always be derived from the same latest response set.

### Frontend Aggregation Rules

Top-card counts should be computed in the frontend:

- `全部订单`: total merchant orders
- `待接单`: orders where `orderStatus === 'PAID'`
- `备餐中`: orders where `orderStatus === 'ACCEPTED' || orderStatus === 'PREPARING'`
- `异常单`: exception orders from the exception-order API response

`备餐中` intentionally includes both accepted and preparing orders because both still require merchant follow-through from an operations perspective.

### Filter Rules

The active-card filter should behave as follows:

- `全部订单`: include all normal orders
- `待接单`: include only `PAID`
- `备餐中`: include only `ACCEPTED` and `PREPARING`
- `异常单`: render only the exception list

### Priority Sorting

Normal-order rendering should prioritize actionable states before passive history states.

Recommended sort priority:

1. `PAID`
2. `ACCEPTED`
3. `PREPARING`
4. `WAITING_DELIVERY`
5. `DELIVERING`
6. `COMPLETED`
7. `CANCELLED`

Within the same status group, newer orders should appear first.

### Post-Action Refresh

After accept, reject, prepare, finish, exception report, or exception resolve actions, the page should re-fetch merchant orders and exception orders rather than guessing local state transitions.

This keeps the UI aligned with backend truth and reduces drift risk.

## Frontend Structure

## Files To Modify

- `campus-catering-web/src/views/workspace/OrdersView.vue`

## Files To Add

- `campus-catering-web/src/views/workspace/merchant-orders.ts`
- `campus-catering-web/src/views/workspace/merchant-orders.spec.ts`

## Responsibility Split

### `OrdersView`

- Owns page-level loading and refresh
- Owns current active summary-card state
- Renders summary cards and the active content area
- Triggers existing order actions and refreshes after mutation

### `merchant-orders.ts`

- Maps backend order status to card counts
- Provides filter helpers for each view mode
- Provides priority sorting for normal orders
- Centralizes small label and formatting helpers if they become too noisy inside the page component

### `merchant-orders.spec.ts`

- Verifies summary-card counts
- Verifies per-card filtering rules
- Verifies actionable-order-first sorting behavior

## Error Handling

- If order loading fails, keep the current page-level error toast behavior
- If exception loading fails, surface a clear error and preserve the last good normal-order state
- If an action fails, do not optimistically modify cards or list rows
- Empty-card states should explain why the filtered list is empty, especially for `待接单`, `备餐中`, and `异常单`

## Testing Strategy

Add focused unit coverage for the new order-page rules instead of relying only on manual UI checks.

Tests should cover:

- Count aggregation for `全部订单`, `待接单`, `备餐中`, and `异常单`
- Active-card filtering behavior
- Priority sorting with mixed statuses
- Stable ordering for orders in the same status bucket
- Exception-view switching rules at the helper level where practical

## Success Criteria

Merchant can:

- Open the order page and immediately see how many urgent orders require handling
- Click a summary card to focus on one handling lane
- See `待接单` orders first by default
- See `备餐中` orders grouped into one operational lane
- Switch to exception orders without mixing them into the normal list
- Continue using existing accept, reject, prepare, finish, and exception actions

## Risks

### Frontend-only aggregation drift

Because the summary cards are computed from fetched lists instead of a dedicated backend aggregation API, the page depends on those list responses being complete and current. This is acceptable for this scope, but future metrics growth may justify a backend summary endpoint.

### View complexity growth

`OrdersView.vue` already contains display and mutation logic. Adding cards, filters, and sorting directly into the component would make it harder to maintain, so the transformation logic should be split into a focused helper module.

## Implementation Notes

- Keep the current backend contracts unchanged
- Keep the current merchant action buttons in place
- Prefer a clear operational layout over adding more advanced controls
- Stay within a frontend-only enhancement unless implementation reveals a hard contract gap
