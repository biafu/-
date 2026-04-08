# Campus Catering Backlog Prioritization Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Identify the biggest remaining product gaps, prioritize them, and map each gap to the files most likely to change.

**Architecture:** The repo already covers the basic student ordering flow, merchant order handling, admin review/statistics, and rider fulfillment. Remaining work is concentrated in authentication/payment, merchant operations, admin operations, and realtime/product-growth features. The best next move is to split these gaps into independent delivery tracks and turn the top-priority items into separate implementation plans.

**Tech Stack:** Spring Boot 3, MyBatis, MySQL, Redis, WebSocket, Vue 3, Vite, Element Plus

---

## Scope Note

This document is a prioritized backlog, not a single end-to-end implementation plan. The remaining work spans several independent subsystems, so each item below should become its own detailed implementation plan before coding.

## Priority Guide

- `P0`: Blocks core business completeness or leaves a major user-visible gap in the main flow
- `P1`: Important product capability missing, but the main order loop still works
- `P2`: Enhancement, channel integration, or operational maturity work

## Current Baseline

- Student flow already supports browsing, cart, checkout, coupon selection, order creation, mock payment, cancel, reorder, urge, review, messages, and address management.
- Merchant flow already supports product/category management, stock updates, store settings, order actions, and exception reporting.
- Admin flow already supports merchant application review, dashboard stats, daily rebuild, store enable/disable, and seckill activity CRUD.
- Delivery flow already supports viewing assigned orders, pickup, and completion.

## Backlog

### P0. Real Payment and Refund Flow

**Why it matters:** The system still relies on mock payment, and refund handling exists in status enums but not as a complete business flow.

**Evidence in repo:**
- Design doc explicitly allows mock payment first and real payment later.
- Student order API exposes pay/cancel but no refund request endpoint.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\order\controller\StudentOrderController.java`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\order\service\OrderService.java`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\trace\service\OrderTraceService.java`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\student\StudentPaymentResultView.vue`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\student\StudentOrdersView.vue`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\student\StudentOrderDetailView.vue`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\api\student.ts`

### P0. Merchant WebSocket Consumption and Reconnect UX

**Why it matters:** Backend WebSocket exists, but the merchant frontend still behaves like a polling-only console. The design doc expects heartbeat and reconnect support.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\workspace\OrdersView.vue`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\utils\merchant-order-ws.ts`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\stores\auth.ts`
- Optional modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\workspace\WorkspaceLayout.vue`
- Reference backend: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\message\WebSocketConfig.java`

### P0. Merchant Business Hours UI

**Why it matters:** The backend already supports business-hour CRUD, but merchants cannot manage it from the current UI.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\workspace\StoreView.vue`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\api\merchant.ts`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\workspace\merchant-business-hours.ts`
- Reference backend: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\merchant\controller\MerchantController.java`

### P0. Admin Dispatch UI

**Why it matters:** Admin can dispatch orders by API, but there is no frontend workflow for choosing a rider and sending the order.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\admin\AdminOperationsView.vue`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\api\admin.ts`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\admin\admin-dispatch.ts`
- Reference backend: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\statistics\controller\AdminController.java`

### P1. Merchant Application Submission Page

**Why it matters:** Admin can review applications and backend can receive them, but merchants cannot submit from the web app.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\router\index.ts`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\auth\LoginView.vue`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\auth\MerchantApplyView.vue`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\auth\merchant-apply.ts`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\api\merchant.ts`

### P1. Student Seckill Entry and Result UX

**Why it matters:** Seckill is implemented server-side and configurable in admin, but students have no visible activity feed or participation flow.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\router\index.ts`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\student\StudentHomeView.vue`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\student\StudentStoreDetailView.vue`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\student\StudentSeckillView.vue`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\api\seckill.ts`
- Reference backend: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\seckill\controller\SeckillController.java`

### P1. Admin Campus, Building, Region, and Delivery Rule Management

**Why it matters:** The design doc calls out campus/building/region and delivery-rule management, but the current system only stores free-text delivery range descriptions.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\resources\db\schema.sql`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\statistics\controller\AdminCampusController.java`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\statistics\service\AdminCampusService.java`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\admin\AdminOperationsView.vue`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\admin\AdminCampusRulesView.vue`

### P1. User Feedback Processing

**Why it matters:** The platform operations spec includes user feedback handling, but there is no feedback submission or admin processing module.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\resources\db\schema.sql`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\message\controller\StudentFeedbackController.java`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\statistics\controller\AdminFeedbackController.java`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\student\StudentFeedbackView.vue`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\admin\AdminFeedbackView.vue`

### P2. WeChat Login and Phone Binding

**Why it matters:** This is a product-channel requirement from the original design, but it is not needed to keep the web demo functional.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\auth\controller\AuthController.java`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\auth\service\AuthService.java`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\resources\application.yml`
- Create or separate repo: miniapp-side login entry if a real miniapp client is introduced

### P2. Merchant Review Center

**Why it matters:** Students can submit reviews, but merchants currently cannot view aggregated review content inside the console.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\order\service\OrderReviewService.java`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\order\controller\MerchantReviewController.java`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\router\index.ts`
- Create: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\workspace\ReviewsView.vue`

### P2. Replace Demo-Only Store Presentation Metrics

**Why it matters:** Home-page ratings, ETA, distance, monthly sales, and average spend are currently synthesized in the frontend instead of being computed from backend data.

**Likely files:**
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-web\src\views\student\StudentHomeView.vue`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\student\controller\StudentStoreController.java`
- Modify: `C:\Users\Administrator\Desktop\zhinengpt\campus-catering-server\src\main\java\com\zhinengpt\campuscatering\merchant\dto\StoreSimpleResponse.java`

## Recommended Delivery Order

1. Real payment and refund flow
2. Merchant WebSocket consumption and reconnect UX
3. Merchant business hours UI
4. Admin dispatch UI
5. Merchant application submission page
6. Student seckill entry and result UX
7. Admin campus and delivery-rule management
8. User feedback processing
9. Merchant review center
10. WeChat login and phone binding
11. Replace demo-only store presentation metrics

## Recommended Next Plans

- First detailed plan: `real-payment-and-refund`
- Second detailed plan: `merchant-websocket-reconnect`
- Third detailed plan: `admin-dispatch-console`

## Self-Review

- Spec coverage: The backlog covers the main missing items called out by the design doc that are not yet fully implemented in the current repo.
- Placeholder scan: No `TODO`, `TBD`, or deferred placeholders remain in this backlog.
- Type consistency: File names and subsystem ownership align with current repository structure.
