# Student Browse And Order Touchpoints Design

**Date:** 2026-04-06

**Goal:** Fill the student-facing interaction gaps where visible buttons or obvious entry points do not yet lead to meaningful behavior.

## Scope

This design covers three linked student-side enhancements:

1. Home page category and sort interactions
2. Message center business jump actions
3. Order list detail expansion and targeted entry

This design does not add a standalone order detail route, coupon center page, or a new backend search/filter API.

## Current Context

The student workspace already includes:

- Home, store detail, cart, checkout, payment result
- Orders, messages, addresses
- Student checkout state and message unread synchronization

The main gaps are interaction gaps rather than missing top-level pages:

- Home category buttons are static
- Home sort chips are static
- The “go order” button is visually prominent but not independently wired
- Message cards only support marking as read
- Orders do not have a detail-oriented target state for deep linking

## Approach

Use a lightweight enhancement strategy:

- Keep home filtering entirely client-side using already loaded store data
- Add message action routing by interpreting `bizType` and `bizId`
- Add inline order detail expansion instead of introducing a new page
- Reuse query parameters so payment result and message center can point into orders

This keeps the work small, preserves the existing route structure, and makes the currently dead-feeling controls useful.

## Home Page Design

### Interactions

- Category buttons become toggle filters
- Sort chips become a single-select sort mode
- Search keyword, selected category, and sort mode all apply together
- Clicking either the store card or the “去点餐” button opens the same store detail route

### Category Strategy

There is no real store category field in the backend response, so the home page will derive a coarse category for each store on the frontend using existing content:

- First use matching hints from `storeName`
- Then use hints from `notice`
- Fall back to a generic category such as “美食”

The category bar remains intentionally lightweight and approximate until the backend gains a formal category model.

### Sort Strategy

Use current frontend-derived fields only:

- `default`: preserve current list order
- `distance`: nearest first using derived numeric distance
- `minOrderAmount`: lowest first
- `deliveryFee`: lowest first

## Message Center Design

### New Action Model

Each message card may expose a “查看相关内容” button when its business context is recognized.

Supported cases for this round:

- Order-related message: route to `/student/orders?highlightOrderId=<bizId>&expandOrderId=<bizId>`
- Store-related message: route to `/student/store/<bizId>`

Recognition rule:

- If `bizType` is an order-like value, treat it as an order jump
- If `bizType` is a store-like value, treat it as a store jump
- If `bizType` is absent but `bizId` exists and `messageType` or `title` strongly implies order progress, treat it as an order jump

Unrecognized messages stay read-only and do not render a jump button.

### Read Behavior

- If a user clicks the action on an unread message, mark it as read first
- Then navigate
- If marking as read fails, stop and show an error instead of silently navigating

## Orders Page Design

### Detail Expansion

Each order card gets a `查看详情 / 收起详情` action.

Expanded content includes:

- Receiver name
- Receiver phone
- Receiver address
- Remark
- Item line list with quantity and subtotal
- Order total

Existing actions remain available:

- Pay
- Cancel
- Urge
- Reorder
- Review

### Targeted Entry

Orders page reads optional route query parameters:

- `highlightOrderId`
- `expandOrderId`

Behavior:

- Matching order auto-expands on load
- Matching order gets a visual highlight state
- If the target id is not present in the returned orders, show a friendly informational message and continue rendering normally

### Payment Result Linkage

The payment result page’s “查看订单” button will route to the orders page with target query parameters so the newly created order opens in context.

## Error Handling

- Home filters yielding no results show the existing empty state with updated messaging
- Message jump action only renders when a destination can be resolved
- Failed jump preparation shows an error toast
- Missing target order shows an info toast, not a hard failure
- Order expansion defaults to collapsed if there is no target state

## Files Expected To Change

- `campus-catering-web/src/views/student/StudentHomeView.vue`
- `campus-catering-web/src/views/student/StudentMessagesView.vue`
- `campus-catering-web/src/views/student/StudentOrdersView.vue`
- `campus-catering-web/src/views/student/StudentPaymentResultView.vue`
- `campus-catering-web/src/api/student.ts` if any student message typing normalization is needed
- Optional helper modules under `campus-catering-web/src/views/student/` if extracting filter or jump logic improves clarity

## Verification

Minimum verification for this work:

- Home page category filter changes the visible store list
- Home page sort chips change ordering deterministically
- “去点餐” reliably opens the selected store
- Recognized messages can jump to orders or stores
- Jumping from messages expands the targeted order
- Payment result “查看订单” lands on the new order and expands it
- Student web app build passes

## Self-Review

- No placeholder sections remain
- Scope is limited to interaction enhancement, not a new subsystem
- No requirement depends on a backend contract that does not already exist
