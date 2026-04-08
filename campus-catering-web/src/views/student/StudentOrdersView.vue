<template>
  <section class="header glass-panel">
    <div>
      <h1 class="cc-section-title">我的订单</h1>
    </div>
    <el-button type="primary" round @click="loadOrders">刷新</el-button>
  </section>

  <section class="list">
    <el-skeleton v-if="loading" :rows="6" animated />

    <template v-else>
      <RevealOnScroll v-for="order in orders" :key="order.id">
        <article
          class="cc-card order-card hover-lift"
          :class="{ highlighted: highlightedOrderId === order.id }"
          :data-order-id="order.id"
        >
          <div class="line">
            <h3>{{ order.storeName }}</h3>
            <span :class="['status', getStudentOrderStatusClass(order.orderStatus)]">
              {{ getStudentOrderStatusLabel(order.orderStatus) }}
            </span>
          </div>
          <p class="meta">
            订单号 {{ order.orderNo }} · {{ formatStudentOrderTime(order.createdAt) }} · 实付
            {{ formatStudentOrderPrice(order.payAmount) }}
          </p>
          <div class="items">
            <el-tag v-for="item in order.items" :key="`${order.id}-${item.skuId}`" effect="plain" round>
              {{ item.spuName }} {{ item.skuName }} × {{ item.quantity }}
            </el-tag>
          </div>

          <div v-if="isExpanded(order.id)" class="detail-panel">
            <div class="detail-grid">
              <div>
                <span class="detail-label">收货人</span>
                <strong>{{ order.receiverName || '-' }}</strong>
              </div>
              <div>
                <span class="detail-label">联系电话</span>
                <strong>{{ order.receiverPhone || '-' }}</strong>
              </div>
            </div>
            <div class="detail-block">
              <span class="detail-label">收货地址</span>
              <p>{{ order.receiverAddress || '-' }}</p>
            </div>
            <div class="detail-block">
              <span class="detail-label">订单备注</span>
              <p>{{ order.remark || '无备注' }}</p>
            </div>
            <div class="detail-block">
              <span class="detail-label">商品明细</span>
              <div class="detail-items">
                <div v-for="item in order.items" :key="`${order.id}-detail-${item.skuId}`" class="detail-item">
                  <span>{{ item.spuName }} {{ item.skuName }}</span>
                  <span>{{ item.quantity }} 份</span>
                  <strong>{{ formatStudentOrderPrice(item.totalAmount) }}</strong>
                </div>
              </div>
            </div>
            <div class="detail-total">
              <span>订单总额</span>
              <strong>{{ formatStudentOrderPrice(order.payAmount) }}</strong>
            </div>
          </div>

          <div class="actions">
            <el-button size="small" text @click="toggleExpand(order.id)">
              {{ isExpanded(order.id) ? '收起详情' : '查看详情' }}
            </el-button>
            <el-button size="small" text @click="openDetail(order.id)">详情页</el-button>
            <el-button
              v-if="order.orderStatus === 'PENDING_PAYMENT'"
              size="small"
              type="primary"
              @click="pay(order.id)"
            >
              去支付
            </el-button>
            <el-button v-if="order.orderStatus === 'PENDING_PAYMENT'" size="small" @click="cancel(order.id)">
              取消订单
            </el-button>
            <el-button v-if="canRefundStudentOrder(order.orderStatus)" size="small" type="danger" plain @click="refund(order.id)">
              申请退款
            </el-button>
            <el-button
              v-if="['PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING'].includes(order.orderStatus)"
              size="small"
              @click="urge(order.id)"
            >
              催单
            </el-button>
            <el-button
              v-if="['COMPLETED', 'CANCELLED', 'REFUNDED'].includes(order.orderStatus)"
              size="small"
              @click="reorder(order.id)"
            >
              再来一单
            </el-button>
            <el-button v-if="order.orderStatus === 'COMPLETED'" size="small" type="warning" @click="openReview(order.id)">
              评价
            </el-button>
          </div>
        </article>
      </RevealOnScroll>

      <el-empty v-if="orders.length === 0" description="暂时没有订单记录" />
    </template>
  </section>

  <el-dialog v-model="reviewDialogVisible" title="订单评价" width="460px">
    <el-form label-position="top">
      <el-form-item label="评分">
        <el-rate v-model="reviewForm.rating" :max="5" />
      </el-form-item>
      <el-form-item label="评价内容">
        <el-input
          v-model="reviewForm.content"
          type="textarea"
          :rows="4"
          maxlength="120"
          show-word-limit
          placeholder="说说这次用餐体验吧"
        />
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="reviewForm.isAnonymous">匿名评价</el-checkbox>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="reviewDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submittingReview" @click="submitReview">提交评价</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import RevealOnScroll from '@/components/RevealOnScroll.vue'
import {
  cancelStudentOrder,
  fetchOrderReview,
  fetchStudentOrders,
  payStudentOrder,
  refundStudentOrder,
  reorderStudentOrder,
  submitOrderReview,
  type OrderDetailResponse,
  urgeStudentOrder,
} from '@/api/student'
import { emitStudentMessageChanged } from '@/utils/student-message-events'
import { parseTargetOrderQuery } from '@/views/student/student-touchpoints'
import {
  buildStudentOrderDetailPath,
  canRefundStudentOrder,
  formatStudentOrderPrice,
  formatStudentOrderTime,
  getStudentOrderStatusClass,
  getStudentOrderStatusLabel,
} from '@/views/student/student-orders'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const orders = ref<OrderDetailResponse[]>([])
const expandedOrderIds = ref<number[]>([])
const highlightedOrderId = ref<number | null>(null)
const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const reviewOrderId = ref<number>()
const reviewForm = reactive({
  rating: 5,
  content: '',
  isAnonymous: false,
})

const scrollToTargetOrder = async (orderId: number) => {
  await nextTick()
  const card = document.querySelector<HTMLElement>(`.order-card[data-order-id="${orderId}"]`)
  card?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const applyQueryState = async () => {
  const { expandOrderId, highlightOrderId: targetHighlightId } = parseTargetOrderQuery(route.query)
  highlightedOrderId.value = targetHighlightId

  if (!expandOrderId) {
    return
  }

  const exists = orders.value.some((order) => order.id === expandOrderId)
  if (!exists) {
    ElMessage.info('没有找到目标订单，先为你展示全部订单。')
    return
  }

  if (!expandedOrderIds.value.includes(expandOrderId)) {
    expandedOrderIds.value = [...expandedOrderIds.value, expandOrderId]
  }
  await scrollToTargetOrder(expandOrderId)
}

const loadOrders = async () => {
  loading.value = true
  try {
    orders.value = await fetchStudentOrders()
    await applyQueryState()
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载订单失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const isExpanded = (orderId: number) => expandedOrderIds.value.includes(orderId)

const toggleExpand = (orderId: number) => {
  expandedOrderIds.value = isExpanded(orderId)
    ? expandedOrderIds.value.filter((id) => id !== orderId)
    : [...expandedOrderIds.value, orderId]
}

const openDetail = (orderId: number) => {
  void router.push(buildStudentOrderDetailPath(orderId))
}

const pay = async (orderId: number) => {
  try {
    await payStudentOrder(orderId)
    ElMessage.success('支付成功')
    await loadOrders()
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '支付失败'
    ElMessage.error(message)
  }
}

const cancel = async (orderId: number) => {
  try {
    await cancelStudentOrder(orderId)
    ElMessage.success('订单已取消')
    await loadOrders()
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '取消失败'
    ElMessage.error(message)
  }
}

const refund = async (orderId: number) => {
  try {
    await refundStudentOrder(orderId)
    ElMessage.success('退款已处理完成')
    await loadOrders()
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '退款失败'
    ElMessage.error(message)
  }
}

const urge = async (orderId: number) => {
  try {
    await urgeStudentOrder(orderId)
    ElMessage.success('已向商家发送催单提醒')
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '催单失败'
    ElMessage.error(message)
  }
}

const reorder = async (orderId: number) => {
  try {
    const result = await reorderStudentOrder(orderId)
    ElMessage.success(`再来一单成功，订单号 ${result.orderNo}`)
    await loadOrders()
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '再来一单失败'
    ElMessage.error(message)
  }
}

const openReview = async (orderId: number) => {
  try {
    const existed = await fetchOrderReview(orderId)
    if (existed) {
      ElMessage.info('这个订单已经评价过了')
      return
    }
    reviewOrderId.value = orderId
    reviewForm.rating = 5
    reviewForm.content = ''
    reviewForm.isAnonymous = false
    reviewDialogVisible.value = true
  } catch (error) {
    const message = error instanceof Error ? error.message : '获取评价信息失败'
    ElMessage.error(message)
  }
}

const submitReview = async () => {
  if (!reviewOrderId.value) {
    return
  }

  submittingReview.value = true
  try {
    await submitOrderReview({
      orderId: reviewOrderId.value,
      rating: Number(reviewForm.rating || 5),
      content: reviewForm.content.trim(),
      isAnonymous: reviewForm.isAnonymous ? 1 : 0,
    })
    ElMessage.success('评价提交成功')
    reviewDialogVisible.value = false
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '评价提交失败'
    ElMessage.error(message)
  } finally {
    submittingReview.value = false
  }
}

watch(
  () => route.fullPath,
  async () => {
    if (orders.value.length > 0) {
      await applyQueryState()
    }
  },
)

onMounted(loadOrders)
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.list {
  margin-top: 16px;
  display: grid;
  gap: 14px;
}

.order-card {
  padding: 16px;
  transition: box-shadow 0.2s ease, border-color 0.2s ease;
}

.order-card.highlighted {
  border-color: #ffd475;
  box-shadow: 0 14px 30px rgba(255, 191, 47, 0.18);
}

.line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.line h3 {
  margin: 0;
  font-size: 18px;
}

.status {
  font-size: 12px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 999px;
}

.processing {
  color: #1559f5;
  background: #e8f0ff;
}

.completed {
  color: #118251;
  background: #eaf9f2;
}

.cancelled {
  color: #b42318;
  background: #fef3f2;
}

.meta {
  margin: 8px 0 10px;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.items {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-panel {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--cc-color-border);
  display: grid;
  gap: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-block {
  display: grid;
  gap: 6px;
}

.detail-label {
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

.detail-block p {
  margin: 0;
  color: var(--cc-color-text-secondary);
}

.detail-items {
  display: grid;
  gap: 8px;
}

.detail-item,
.detail-total,
.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.actions {
  margin-top: 12px;
}

@media (max-width: 768px) {
  .header,
  .line,
  .detail-item,
  .detail-total {
    align-items: flex-start;
    flex-direction: column;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
