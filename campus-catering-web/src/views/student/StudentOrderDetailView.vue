<template>
  <section v-if="order" class="detail-stack">
    <section class="header glass-panel">
      <div>
        <p class="eyebrow">Order Detail</p>
        <h1 class="cc-section-title">{{ order.storeName }}</h1>
      </div>
      <el-button round @click="loadOrder">刷新</el-button>
    </section>

    <section class="cc-card summary-card">
      <div class="summary-line">
        <span>收货人</span>
        <strong>{{ order.receiverName || '-' }}</strong>
      </div>
      <div class="summary-line">
        <span>联系电话</span>
        <strong>{{ order.receiverPhone || '-' }}</strong>
      </div>
      <div class="summary-line block">
        <span>收货地址</span>
        <strong>{{ order.receiverAddress || '-' }}</strong>
      </div>
      <div class="summary-line block">
        <span>订单备注</span>
        <strong>{{ order.remark || '无备注' }}</strong>
      </div>
    </section>

    <section class="cc-card items-card">
      <h2>商品明细</h2>
      <div class="items-list">
        <div v-for="item in order.items" :key="`${order.id}-${item.skuId}`" class="item-row">
          <div>
            <strong>{{ item.spuName }}</strong>
            <p>{{ item.skuName }} × {{ item.quantity }}</p>
          </div>
          <span>{{ formatStudentOrderPrice(item.totalAmount) }}</span>
        </div>
      </div>
      <div class="total-line">
        <span>实付金额</span>
        <strong>{{ formatStudentOrderPrice(order.payAmount) }}</strong>
      </div>
    </section>

    <section class="cc-card actions-card">
      <el-button v-if="order.orderStatus === 'PENDING_PAYMENT'" type="primary" @click="pay">立即支付</el-button>
      <el-button v-if="order.orderStatus === 'PENDING_PAYMENT'" @click="cancel">取消订单</el-button>
      <el-button v-if="canRefundStudentOrder(order.orderStatus)" type="danger" plain @click="requestRefund">
        申请退款
      </el-button>
      <el-button
        v-if="['PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING'].includes(order.orderStatus)"
        @click="urge"
      >
        催单
      </el-button>
      <el-button v-if="['COMPLETED', 'CANCELLED', 'REFUNDED'].includes(order.orderStatus)" @click="reorder">
        再来一单
      </el-button>
      <el-button plain @click="router.push('/student/orders')">返回订单列表</el-button>
    </section>
  </section>

  <el-empty v-else description="未找到对应订单" />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  cancelStudentOrder,
  fetchStudentOrders,
  payStudentOrder,
  refundStudentOrder,
  reorderStudentOrder,
  type OrderDetailResponse,
  urgeStudentOrder,
} from '@/api/student'
import { buildOrderDetailTarget } from '@/views/student/student-profile'
import {
  canRefundStudentOrder,
  formatStudentOrderPrice,
  formatStudentOrderTime,
  getStudentOrderStatusLabel,
} from '@/views/student/student-orders'
import { emitStudentMessageChanged } from '@/utils/student-message-events'

const route = useRoute()
const router = useRouter()
const orders = ref<OrderDetailResponse[]>([])

const order = computed(() => buildOrderDetailTarget(orders.value, String(route.params.id)) as OrderDetailResponse | undefined)

const loadOrder = async () => {
  try {
    orders.value = await fetchStudentOrders()
  } catch (error) {
    const message = error instanceof Error ? error.message : '订单详情加载失败'
    ElMessage.error(message)
  }
}

const pay = async () => {
  if (!order.value) return
  try {
    await payStudentOrder(order.value.id)
    ElMessage.success('支付成功')
    emitStudentMessageChanged()
    await loadOrder()
  } catch (error) {
    const message = error instanceof Error ? error.message : '支付失败'
    ElMessage.error(message)
  }
}

const cancel = async () => {
  if (!order.value) return
  try {
    await cancelStudentOrder(order.value.id)
    ElMessage.success('订单已取消')
    emitStudentMessageChanged()
    await loadOrder()
  } catch (error) {
    const message = error instanceof Error ? error.message : '取消失败'
    ElMessage.error(message)
  }
}

const requestRefund = async () => {
  if (!order.value) return
  try {
    await refundStudentOrder(order.value.id)
    ElMessage.success('退款已处理完成')
    emitStudentMessageChanged()
    await loadOrder()
  } catch (error) {
    const message = error instanceof Error ? error.message : '退款失败'
    ElMessage.error(message)
  }
}

const urge = async () => {
  if (!order.value) return
  try {
    await urgeStudentOrder(order.value.id)
    ElMessage.success('已提醒商家尽快处理')
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '催单失败'
    ElMessage.error(message)
  }
}

const reorder = async () => {
  if (!order.value) return
  try {
    const result = await reorderStudentOrder(order.value.id)
    ElMessage.success(`再来一单成功，订单号 ${result.orderNo}`)
    emitStudentMessageChanged()
    await router.push('/student/orders')
  } catch (error) {
    const message = error instanceof Error ? error.message : '再来一单失败'
    ElMessage.error(message)
  }
}

onMounted(loadOrder)
</script>

<style scoped>
.detail-stack {
  display: grid;
  gap: 16px;
}

.header,
.summary-card,
.items-card,
.actions-card {
  padding: 18px;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #9a7a36;
}

.summary-card,
.items-card {
  display: grid;
  gap: 12px;
}

.summary-line,
.total-line {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.summary-line.block {
  align-items: flex-start;
}

.items-card h2 {
  margin: 0;
}

.items-list {
  display: grid;
  gap: 12px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--cc-color-border);
}

.item-row p {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
}

.actions-card {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .summary-line,
  .item-row {
    flex-direction: column;
  }
}
</style>
