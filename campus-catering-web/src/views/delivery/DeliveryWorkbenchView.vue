<template>
  <div class="page-shell">
    <section class="hero glass-panel">
      <div>
        <p class="eyebrow">配送工作台</p>
        <h1 class="cc-section-title">骑手接单中心</h1>
      </div>
      <div class="hero-actions">
        <el-button round @click="loadWorkbench">刷新</el-button>
        <el-button plain @click="logout">退出登录</el-button>
      </div>
    </section>

    <section class="summary-grid">
      <article class="cc-card summary-card accent">
        <span>待抢订单</span>
        <strong>{{ availableOrders.length }}</strong>
      </article>
      <article class="cc-card summary-card">
        <span>待取餐</span>
        <strong>{{ waitingPickupCount }}</strong>
      </article>
      <article class="cc-card summary-card">
        <span>配送中</span>
        <strong>{{ deliveringCount }}</strong>
      </article>
      <article class="cc-card summary-card">
        <span>已完成</span>
        <strong>{{ completedOrders.length }}</strong>
      </article>
    </section>

    <section class="workbench-grid">
      <article class="cc-card panel">
        <div class="panel-head">
          <div>
            <h2>待抢订单池</h2>
            <p>多个骑手可同时查看，当前以“立即抢单”为主动作。</p>
          </div>
          <el-tag round effect="plain">{{ availableOrders.length }} 单</el-tag>
        </div>

        <el-skeleton v-if="loading" :rows="5" animated />
        <template v-else>
          <div v-if="availableOrders.length > 0" class="order-list">
            <article
              v-for="order in availableOrders"
              :key="`available-${order.orderId}`"
              class="order-card available-card"
            >
              <div class="card-top">
                <div>
                  <p class="card-kicker">待抢订单</p>
                  <h3>订单号 {{ order.orderNo }}</h3>
                  <p class="order-subtitle">{{ order.storeName || `门店 ${order.storeId}` }} · {{ formatDateTime(order.createdAt) }}</p>
                </div>
                <div class="order-tags">
                  <el-tag type="info" effect="plain" round>待抢单</el-tag>
                  <el-tag type="warning" effect="plain" round>{{ toPrice(order.payAmount) }}</el-tag>
                </div>
              </div>

              <div class="address-block">
                <span class="meta-label">配送地址</span>
                <strong>{{ order.receiverAddress }}</strong>
              </div>

              <div class="person-row">
                <div class="person-meta">
                  <span class="meta-label">收货人</span>
                  <strong>{{ order.receiverName }}</strong>
                </div>
                <div class="person-meta">
                  <span class="meta-label">联系电话</span>
                  <strong>{{ order.receiverPhone }}</strong>
                </div>
              </div>

              <div v-if="order.dispatchRemark" class="remark-row">
                <span class="meta-label">调度备注</span>
                <strong>{{ order.dispatchRemark }}</strong>
              </div>

              <div class="quick-actions">
                <el-button plain @click="callReceiver(order.receiverPhone)">一键电话</el-button>
                <el-button plain @click="copyAddress(order.receiverAddress)">复制地址</el-button>
              </div>

              <div class="task-actions">
                <el-button
                  type="primary"
                  size="large"
                  :loading="actionOrderId === order.orderId && actionType === 'claim'"
                  @click="claim(order.orderId)"
                >
                  立即抢单
                </el-button>
              </div>
            </article>
          </div>
          <el-empty v-else description="当前没有待抢订单" />
        </template>
      </article>

      <article class="cc-card panel">
        <div class="panel-head">
          <div>
            <h2>当前执行单</h2>
            <p>按待取餐、配送中排序，优先显示你现在最需要处理的单。</p>
          </div>
          <el-tag round effect="plain">{{ activeOrders.length }} 单</el-tag>
        </div>

        <el-skeleton v-if="loading" :rows="5" animated />
        <template v-else>
          <div v-if="activeOrders.length > 0" class="order-list">
            <article v-for="order in activeOrders" :key="`active-${order.orderId}`" class="order-card active-card">
              <div class="card-top">
                <div>
                  <p class="card-kicker">{{ dispatchLabel(order.dispatchStatus) }}</p>
                  <h3>订单号 {{ order.orderNo }}</h3>
                  <p class="order-subtitle">{{ order.storeName || `门店 ${order.storeId}` }} · {{ formatDateTime(order.createdAt) }}</p>
                </div>
                <div class="order-tags">
                  <el-tag :type="dispatchType(order.dispatchStatus)" effect="plain" round>
                    {{ dispatchLabel(order.dispatchStatus) }}
                  </el-tag>
                  <el-tag type="warning" effect="plain" round>{{ toPrice(order.payAmount) }}</el-tag>
                </div>
              </div>

              <div class="address-block emphasis">
                <span class="meta-label">配送地址</span>
                <strong>{{ order.receiverAddress }}</strong>
              </div>

              <div class="person-row">
                <div class="person-meta">
                  <span class="meta-label">收货人</span>
                  <strong>{{ order.receiverName }}</strong>
                </div>
                <div class="person-meta">
                  <span class="meta-label">联系电话</span>
                  <strong>{{ order.receiverPhone }}</strong>
                </div>
              </div>

              <div v-if="order.dispatchRemark" class="remark-row">
                <span class="meta-label">调度备注</span>
                <strong>{{ order.dispatchRemark }}</strong>
              </div>

              <div class="progress-row">
                <div v-if="order.pickupTime" class="progress-chip">已取餐：{{ formatDateTime(order.pickupTime) }}</div>
                <div v-if="order.deliveredTime" class="progress-chip">已送达：{{ formatDateTime(order.deliveredTime) }}</div>
              </div>

              <div class="quick-actions">
                <el-button plain @click="callReceiver(order.receiverPhone)">一键电话</el-button>
                <el-button plain @click="copyAddress(order.receiverAddress)">复制地址</el-button>
              </div>

              <div class="task-actions">
                <el-button
                  v-if="order.dispatchStatus === 1"
                  type="primary"
                  size="large"
                  :loading="actionOrderId === order.orderId && actionType === 'pickup'"
                  @click="pickup(order.orderId)"
                >
                  已取餐
                </el-button>
                <el-button
                  v-if="order.dispatchStatus === 1 || order.dispatchStatus === 2"
                  type="success"
                  size="large"
                  :loading="actionOrderId === order.orderId && actionType === 'complete'"
                  @click="complete(order.orderId)"
                >
                  确认送达
                </el-button>
              </div>
            </article>
          </div>
          <el-empty v-else description="你当前没有进行中的配送任务" />
        </template>

        <div class="completed-wrap">
          <button type="button" class="completed-toggle" @click="completedCollapsed = !completedCollapsed">
            <span>已完成订单</span>
            <span>{{ completedOrders.length }} 单 · {{ completedCollapsed ? '展开' : '收起' }}</span>
          </button>

          <div v-if="!completedCollapsed" class="completed-list">
            <article v-for="order in completedOrders" :key="`completed-${order.orderId}`" class="order-card completed-card">
              <div class="card-top">
                <div>
                  <p class="card-kicker">已完成</p>
                  <h3>订单号 {{ order.orderNo }}</h3>
                  <p class="order-subtitle">{{ order.storeName || `门店 ${order.storeId}` }} · {{ formatDateTime(order.createdAt) }}</p>
                </div>
                <div class="order-tags">
                  <el-tag type="success" effect="plain" round>已完成</el-tag>
                  <el-tag type="warning" effect="plain" round>{{ toPrice(order.payAmount) }}</el-tag>
                </div>
              </div>

              <div class="address-block">
                <span class="meta-label">配送地址</span>
                <strong>{{ order.receiverAddress }}</strong>
              </div>

              <div class="person-row">
                <div class="person-meta">
                  <span class="meta-label">收货人</span>
                  <strong>{{ order.receiverName }}</strong>
                </div>
                <div class="person-meta">
                  <span class="meta-label">联系电话</span>
                  <strong>{{ order.receiverPhone }}</strong>
                </div>
              </div>

              <div class="progress-row">
                <div v-if="order.pickupTime" class="progress-chip">已取餐：{{ formatDateTime(order.pickupTime) }}</div>
                <div v-if="order.deliveredTime" class="progress-chip">已送达：{{ formatDateTime(order.deliveredTime) }}</div>
              </div>

              <div class="quick-actions">
                <el-button plain @click="callReceiver(order.receiverPhone)">一键电话</el-button>
                <el-button plain @click="copyAddress(order.receiverAddress)">复制地址</el-button>
              </div>
            </article>
            <el-empty v-if="completedOrders.length === 0" description="还没有已完成订单" />
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  claimDeliveryOrder,
  completeDeliveryOrder,
  fetchAvailableDeliveryOrders,
  fetchDeliveryOrders,
  pickupDeliveryOrder,
  type DeliveryOrderResponse,
} from '@/api/delivery'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const completedCollapsed = ref(true)
const actionOrderId = ref<number>()
const actionType = ref<'claim' | 'pickup' | 'complete' | null>(null)
const availableOrders = ref<DeliveryOrderResponse[]>([])
const myOrders = ref<DeliveryOrderResponse[]>([])

const statusWeight = (status: number) => {
  if (status === 1) {
    return 0
  }
  if (status === 2) {
    return 1
  }
  return 2
}

const sortedMyOrders = computed(() =>
  [...myOrders.value].sort((left, right) => {
    const statusGap = statusWeight(left.dispatchStatus) - statusWeight(right.dispatchStatus)
    if (statusGap !== 0) {
      return statusGap
    }
    return String(right.createdAt).localeCompare(String(left.createdAt))
  }),
)

const activeOrders = computed(() => sortedMyOrders.value.filter((order) => order.dispatchStatus === 1 || order.dispatchStatus === 2))
const completedOrders = computed(() => sortedMyOrders.value.filter((order) => order.dispatchStatus === 3))
const waitingPickupCount = computed(() => activeOrders.value.filter((order) => order.dispatchStatus === 1).length)
const deliveringCount = computed(() => activeOrders.value.filter((order) => order.dispatchStatus === 2).length)

const dispatchLabel = (status: number) => {
  if (status === 1) {
    return '待取餐'
  }
  if (status === 2) {
    return '配送中'
  }
  if (status === 3) {
    return '已完成'
  }
  return '待抢单'
}

const dispatchType = (status: number) => {
  if (status === 1) {
    return 'warning'
  }
  if (status === 2) {
    return 'primary'
  }
  if (status === 3) {
    return 'success'
  }
  return 'info'
}

const formatDateTime = (raw?: string) => String(raw || '').replace('T', ' ').slice(0, 16)
const toPrice = (value?: number) => `${Number(value ?? 0).toFixed(2)}元`

const loadWorkbench = async () => {
  loading.value = true
  try {
    const [availableResult, myOrderResult] = await Promise.all([
      fetchAvailableDeliveryOrders(),
      fetchDeliveryOrders(),
    ])
    availableOrders.value = availableResult
    myOrders.value = myOrderResult
  } catch (error) {
    const message = error instanceof Error ? error.message : '配送工作台加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const copyAddress = async (address: string) => {
  if (!address.trim()) {
    ElMessage.warning('当前订单没有可复制的地址')
    return
  }

  try {
    await navigator.clipboard.writeText(address)
    ElMessage.success('地址已复制')
  } catch {
    ElMessage.error('复制失败，请检查浏览器权限')
  }
}

const callReceiver = (phone: string) => {
  if (!phone.trim()) {
    ElMessage.warning('当前订单没有联系电话')
    return
  }
  window.location.href = `tel:${phone}`
}

const claim = async (orderId: number) => {
  actionOrderId.value = orderId
  actionType.value = 'claim'
  try {
    await claimDeliveryOrder(orderId)
    ElMessage.success('抢单成功，订单已进入当前执行单')
    await loadWorkbench()
  } catch (error) {
    const message = error instanceof Error ? error.message : '抢单失败'
    ElMessage.error(message)
    await loadWorkbench()
  } finally {
    actionOrderId.value = undefined
    actionType.value = null
  }
}

const pickup = async (orderId: number) => {
  actionOrderId.value = orderId
  actionType.value = 'pickup'
  try {
    await pickupDeliveryOrder(orderId)
    ElMessage.success('已标记为取餐完成')
    await loadWorkbench()
  } catch (error) {
    const message = error instanceof Error ? error.message : '取餐状态更新失败'
    ElMessage.error(message)
  } finally {
    actionOrderId.value = undefined
    actionType.value = null
  }
}

const complete = async (orderId: number) => {
  actionOrderId.value = orderId
  actionType.value = 'complete'
  try {
    await completeDeliveryOrder(orderId)
    ElMessage.success('订单已确认送达')
    await loadWorkbench()
  } catch (error) {
    const message = error instanceof Error ? error.message : '送达状态更新失败'
    ElMessage.error(message)
  } finally {
    actionOrderId.value = undefined
    actionType.value = null
  }
}

const logout = async () => {
  authStore.logout()
  await router.replace('/auth/login')
}

onMounted(() => {
  void loadWorkbench()
})
</script>

<style scoped>
.hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #9a7a36;
}

.summary-grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.summary-card {
  padding: 18px;
}

.summary-card span {
  color: var(--cc-color-text-tertiary);
  font-size: 12px;
}

.summary-card strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
}

.summary-card.accent {
  background:
    radial-gradient(circle at right top, rgba(255, 216, 122, 0.38), transparent 34%),
    linear-gradient(180deg, #fffef7, #fff8e7);
}

.workbench-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.panel {
  padding: 18px;
  display: grid;
  gap: 16px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.panel-head h2,
.panel-head p {
  margin: 0;
}

.panel-head p {
  margin-top: 6px;
  color: var(--cc-color-text-secondary);
}

.order-list,
.completed-list {
  display: grid;
  gap: 14px;
}

.order-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 18px;
  background: #fffaf2;
  border: 1px solid rgba(255, 216, 122, 0.28);
}

.available-card {
  background:
    radial-gradient(circle at right top, rgba(255, 216, 122, 0.22), transparent 36%),
    #fffdf7;
}

.active-card {
  background:
    radial-gradient(circle at right top, rgba(166, 219, 255, 0.22), transparent 38%),
    #fff;
  border-color: rgba(130, 188, 255, 0.35);
}

.completed-card {
  background: #fcfcfc;
  border-color: rgba(212, 220, 231, 0.9);
}

.card-top,
.person-row,
.task-actions,
.quick-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.card-kicker {
  margin: 0 0 6px;
  color: #9a7a36;
  font-size: 12px;
  letter-spacing: 0.08em;
}

.card-top h3,
.order-subtitle {
  margin: 0;
}

.order-subtitle {
  margin-top: 8px;
  color: var(--cc-color-text-secondary);
}

.order-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.address-block,
.remark-row,
.person-meta {
  display: grid;
  gap: 4px;
}

.address-block strong,
.remark-row strong {
  line-height: 1.7;
}

.address-block.emphasis {
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff7df;
}

.meta-label {
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

.progress-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.progress-chip {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #4a5c73;
  background: #f4f7fb;
}

.quick-actions {
  justify-content: flex-start;
  flex-wrap: wrap;
}

.task-actions {
  justify-content: flex-end;
}

.completed-wrap {
  display: grid;
  gap: 12px;
  margin-top: 4px;
  padding-top: 12px;
  border-top: 1px solid rgba(214, 220, 231, 0.9);
}

.completed-toggle {
  border: none;
  background: #f7f9fc;
  border-radius: 14px;
  padding: 12px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  color: var(--cc-color-text);
  font: inherit;
}

@media (max-width: 1080px) {
  .summary-grid,
  .workbench-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero,
  .panel-head,
  .card-top,
  .person-row,
  .task-actions {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-grid,
  .workbench-grid {
    grid-template-columns: 1fr;
  }

  .task-actions {
    width: 100%;
  }

  .task-actions :deep(.el-button) {
    width: 100%;
  }
}
</style>
