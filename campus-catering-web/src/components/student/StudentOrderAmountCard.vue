<template>
  <section class="cc-card amount-card">
    <h2>金额明细</h2>
    <div class="line">
      <span>商品金额</span>
      <strong>{{ toPrice(preview?.goodsAmount ?? 0) }}</strong>
    </div>
    <div class="line">
      <span>配送费</span>
      <strong>{{ toPrice(preview?.deliveryFee ?? 0) }}</strong>
    </div>
    <div class="line">
      <span>优惠</span>
      <strong class="discount">-{{ toPrice(preview?.discountAmount ?? 0) }}</strong>
    </div>
    <div class="line total">
      <span>实付</span>
      <strong>{{ toPrice(preview?.payAmount ?? 0) }}</strong>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { OrderPreviewResponse } from '@/api/student'

defineProps<{
  preview: OrderPreviewResponse | null
}>()

const toPrice = (value: number) => `${Number(value ?? 0).toFixed(2)}元`
</script>

<style scoped>
.amount-card {
  display: grid;
  gap: 12px;
  padding: 18px 20px;
}

.amount-card h2 {
  margin: 0;
  font-size: 18px;
}

.line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--cc-color-text-secondary);
}

.line strong {
  color: var(--cc-color-text);
}

.discount {
  color: var(--cc-color-success) !important;
}

.total {
  padding-top: 10px;
  border-top: 1px solid var(--cc-color-border);
}

.total strong {
  font-size: 24px;
  color: var(--cc-color-primary);
}
</style>
