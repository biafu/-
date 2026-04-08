<template>
  <section class="header glass-panel">
    <div>
      <h1 class="cc-section-title">支付结果</h1>
    </div>
  </section>

  <StudentPaymentSummaryCard :order="paymentOrder" />

  <section class="cc-card actions-card">
    <el-button type="primary" round :loading="paying" :disabled="!canPay" @click="payNow">
      {{ canPay ? '立即支付' : '无需支付' }}
    </el-button>
    <el-button v-if="canRefund" type="danger" plain :loading="refunding" @click="requestRefund">
      申请退款
    </el-button>
    <el-button plain @click="viewOrder">查看订单</el-button>
    <el-button plain @click="router.push('/student/home')">返回首页</el-button>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { payStudentOrder, refundStudentOrder } from '@/api/student'
import StudentPaymentSummaryCard from '@/components/student/StudentPaymentSummaryCard.vue'
import { emitStudentMessageChanged } from '@/utils/student-message-events'
import { canRefundStudentOrder } from '@/views/student/student-orders'

const route = useRoute()
const router = useRouter()
const paying = ref(false)
const refunding = ref(false)
const paidStatus = ref<string>()

const paymentOrder = computed(() => ({
  orderId: Number(route.query.orderId ?? 0),
  orderNo: String(route.query.orderNo ?? ''),
  orderStatus: paidStatus.value ?? String(route.query.orderStatus ?? 'PENDING_PAYMENT'),
  payAmount: Number(route.query.payAmount ?? 0),
}))

const canPay = computed(() => paymentOrder.value.orderId > 0 && paymentOrder.value.orderStatus === 'PENDING_PAYMENT')
const canRefund = computed(() => paymentOrder.value.orderId > 0 && canRefundStudentOrder(paymentOrder.value.orderStatus))

const viewOrder = () => {
  const orderId = paymentOrder.value.orderId
  router.push({
    path: orderId > 0 ? `/student/orders/${orderId}` : '/student/orders',
  })
}

const payNow = async () => {
  if (!canPay.value) {
    return
  }

  paying.value = true
  try {
    await payStudentOrder(paymentOrder.value.orderId)
    paidStatus.value = 'PAID'
    emitStudentMessageChanged()
    ElMessage.success('支付成功')
  } catch (error) {
    const message = error instanceof Error ? error.message : '支付失败'
    ElMessage.error(message)
  } finally {
    paying.value = false
  }
}

const requestRefund = async () => {
  if (!canRefund.value) {
    return
  }

  refunding.value = true
  try {
    await refundStudentOrder(paymentOrder.value.orderId)
    paidStatus.value = 'REFUNDED'
    emitStudentMessageChanged()
    ElMessage.success('退款已处理完成')
  } catch (error) {
    const message = error instanceof Error ? error.message : '退款失败'
    ElMessage.error(message)
  } finally {
    refunding.value = false
  }
}
</script>

<style scoped>
.header,
.actions-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
}

.actions-card {
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .header,
  .actions-card {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
