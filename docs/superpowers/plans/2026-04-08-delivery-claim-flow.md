# Delivery Claim Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a rider-side claim pool so multiple riders can see the same waiting-delivery order and the fastest claimant gets the assignment.

**Architecture:** Keep the existing admin dispatch flow, but persist waiting-delivery orders in `delivery_order` with `dispatch_status = 0` and a sentinel unassigned rider id. Add backend APIs to list/claim available orders, then update the rider workbench to show both the claim pool and the rider's own assigned orders.

**Tech Stack:** Spring Boot, MyBatis, Vue 3, Pinia, Element Plus

---

### Task 1: Backend Delivery Claim APIs

**Files:**
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/order/mapper/DeliveryOrderMapper.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/order/service/OrderService.java`
- Modify: `campus-catering-server/src/main/java/com/zhinengpt/campuscatering/order/controller/DeliveryOrderController.java`

- [ ] Add mapper queries for available orders, claim updates, and admin assignment updates.
- [ ] Create or reuse `delivery_order` rows when merchant finish-prepare moves orders into the rider claim pool.
- [ ] Add claim/list service methods and expose `/api/delivery/order/available` plus `/api/delivery/order/claim/{orderId}`.

### Task 2: Delivery Workbench UI

**Files:**
- Modify: `campus-catering-web/src/api/delivery.ts`
- Modify: `campus-catering-web/src/views/delivery/DeliveryWorkbenchView.vue`

- [ ] Add frontend API calls for available orders and claiming.
- [ ] Split the workbench into claim-pool and my-orders sections with updated summary cards.
- [ ] Wire claim, pickup, and complete actions so the page refreshes both lists after each mutation.
