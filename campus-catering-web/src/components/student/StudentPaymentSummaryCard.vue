<template>
  <section class="cc-card payment-card">
    <div class="status-row">
      <div>
        <h2>订单信息</h2>
        <p>订单号 {{ order.orderNo || '-' }}</p>
      </div>
      <el-tag :type="statusType" effect="plain" round>{{ statusText }}</el-tag>
    </div>

    <div class="line">
      <span>订单 ID</span>
      <strong>{{ order.orderId || '-' }}</strong>
    </div>
    <div class="line">
      <span>订单金额</span>
      <strong class="price">{{ toPrice(order.payAmount) }}</strong>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getStudentOrderStatusLabel } from '@/views/student/student-orders'

const props = defineProps<{
  order: {
    orderId: number
    orderNo: string
    orderStatus: string
    payAmount: number
  }
}>()

const statusText = computed(() => getStudentOrderStatusLabel(props.order.orderStatus))

const statusType = computed(() => {
  if (props.order.orderStatus === 'PAID' || props.order.orderStatus === 'COMPLETED') {
    return 'success'
  }
  if (props.order.orderStatus === 'CANCELLED' || props.order.orderStatus === 'REFUNDED') {
    return 'danger'
  }
  if (props.order.orderStatus === 'REFUNDING') {
    return 'warning'
  }
  return 'info'
})

const toPrice = (value: number) => `${Number(value ?? 0).toFixed(2)}元`
</script>

<style scoped>
.payment-card {
  display: grid;
  gap: 14px;
  padding: 20px;
}

.status-row,
.line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.status-row h2,
.line span,
.status-row p {
  margin: 0;
}

.status-row p,
.line span {
  color: var(--cc-color-text-secondary);
}

.price {
  font-size: 24px;
  color: var(--cc-color-primary);
}

@media (max-width: 768px) {
  .status-row,
  .line {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
