<template>
  <section class="head glass-panel">
    <div>
      <h1 class="cc-section-title">订单管理</h1>
    </div>
    <div class="head-actions">
      <div class="realtime-status">
        <el-tag :type="realtimeStatusType" round effect="plain">
          {{ realtimeStatusLabel }}
        </el-tag>
        <span>{{ realtimeStatusHint }}</span>
      </div>
      <el-button plain round :loading="loading || exceptionsLoading" @click="refreshAll">刷新订单</el-button>
    </div>
  </section>

  <section class="board-strip">
    <button
      v-for="board in orderBoards"
      :key="board.key"
      type="button"
      class="board-card"
      :class="{ active: board.active }"
      @click="setActiveBoard(board.key)"
    >
      <span>{{ board.label }}</span>
      <strong>{{ board.count }}</strong>
    </button>
  </section>

  <section class="content-shell cc-card">
    <template v-if="activeBoard !== 'exception'">
      <el-skeleton v-if="loading" :rows="6" animated />

      <template v-else>
        <div v-if="visibleOrders.length > 0" class="order-list">
          <article v-for="row in visibleOrders" :key="row.id" class="order-card">
            <header class="order-card__head">
              <div>
                <h3>订单号 {{ row.orderNo }}</h3>
                <p>{{ row.storeName }} · {{ formatMerchantOrderTime(row.createdAt) }}</p>
                <p class="order-address">{{ row.receiverAddress }}</p>
              </div>
              <div class="order-card__meta">
                <strong>{{ formatMerchantOrderPrice(row.payAmount) }}</strong>
                <el-tag :type="getMerchantOrderStatusType(row.orderStatus)" round>
                  {{ getMerchantOrderStatusLabel(row.orderStatus) }}
                </el-tag>
              </div>
            </header>

            <div class="order-card__body">
              <div class="order-block">
                <div class="order-block__title">商品明细</div>
                <ul v-if="row.items.length > 0" class="item-list">
                  <li v-for="item in row.items" :key="`${row.id}-${item.skuId}`" class="item-row">
                    <span>{{ item.spuName }} / {{ item.skuName }} × {{ item.quantity }}</span>
                    <span>{{ formatMerchantOrderPrice(item.totalAmount) }}</span>
                  </li>
                </ul>
                <div v-else class="item-empty">暂无商品</div>
              </div>

              <div v-if="row.remark" class="order-block order-block--remark">
                <div class="order-block__title">备注</div>
                <p>{{ row.remark }}</p>
              </div>
            </div>

            <footer class="order-card__actions">
              <template v-for="action in actionsFor(row.orderStatus)" :key="action.key">
                <el-button size="small" round :type="action.type" @click="handleAction(action.key, row.id)">
                  {{ action.label }}
                </el-button>
              </template>
              <el-button
                v-if="canReportException(row.orderStatus)"
                size="small"
                round
                plain
                type="warning"
                @click="reportException(row.id)"
              >
                上报异常
              </el-button>
            </footer>
          </article>
        </div>

        <el-empty v-else :description="getMerchantOrderEmptyDescription(activeBoard)" />
      </template>
    </template>

    <section v-else class="exception-panel">
      <div class="exception-head">
        <h3>异常订单</h3>
        <el-button size="small" round :loading="exceptionsLoading" @click="loadExceptions">刷新异常</el-button>
      </div>

      <el-skeleton v-if="exceptionsLoading" :rows="4" animated />

      <template v-else>
        <div v-if="exceptions.length > 0" class="exception-list">
          <article v-for="row in exceptions" :key="row.id" class="exception-card">
            <div class="exception-card__main">
              <h4>订单号 {{ row.orderNo }}</h4>
              <p>{{ row.reason }}</p>
              <small>提交时间 {{ formatMerchantOrderTime(row.createdAt) }}</small>
            </div>
            <div class="exception-card__meta">
              <el-tag :type="getMerchantOrderStatusType(row.orderStatus)" round>
                {{ getMerchantOrderStatusLabel(row.orderStatus) }}
              </el-tag>
              <el-tag :type="row.status === 'RESOLVED' ? 'success' : 'warning'" round>
                {{ row.status === 'RESOLVED' ? '已处理' : '处理中' }}
              </el-tag>
            </div>
            <el-button
              v-if="row.status !== 'RESOLVED'"
              size="small"
              type="primary"
              @click="resolveException(row.orderId)"
            >
              处理完成
            </el-button>
          </article>
        </div>

        <el-empty v-else :description="getMerchantOrderEmptyDescription(activeBoard)" />
      </template>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  acceptOrder,
  fetchMerchantExceptionOrders,
  fetchMerchantOrders,
  finishOrder,
  prepareOrder,
  rejectOrder,
  reportMerchantException,
  resolveMerchantException,
  type OrderExceptionResponse,
} from '@/api/merchant'
import type { OrderDetailResponse } from '@/api/student'
import { useAuthStore } from '@/stores/auth'
import {
  buildMerchantOrderBoards,
  filterMerchantOrdersByBoard,
  formatMerchantOrderPrice,
  formatMerchantOrderTime,
  getMerchantOrderEmptyDescription,
  getMerchantOrderStatusLabel,
  getMerchantOrderStatusType,
  sortMerchantOrdersByPriority,
  type MerchantOrderBoardKey,
} from '@/views/workspace/merchant-orders'
import {
  MerchantOrdersWsClient,
  type MerchantOrdersWsPayload,
  type MerchantOrdersWsStatus,
} from '@/utils/merchant-order-ws'

type ActionKey = 'accept' | 'reject' | 'prepare' | 'finish'

const authStore = useAuthStore()
const loading = ref(false)
const exceptionsLoading = ref(false)
const activeBoard = ref<MerchantOrderBoardKey>('paid')
const orders = ref<OrderDetailResponse[]>([])
const exceptions = ref<OrderExceptionResponse[]>([])
const realtimeStatus = ref<MerchantOrdersWsStatus>('idle')
const realtimeNotice = ref('新订单会自动刷新列表')

const orderBoards = computed(() => buildMerchantOrderBoards(orders.value, exceptions.value, activeBoard.value))
const visibleOrders = computed(() =>
  sortMerchantOrdersByPriority(filterMerchantOrdersByBoard(orders.value, activeBoard.value)),
)

const realtimeStatusLabel = computed(() => {
  if (realtimeStatus.value === 'connecting') {
    return '连接中'
  }
  if (realtimeStatus.value === 'connected') {
    return '实时已连接'
  }
  if (realtimeStatus.value === 'reconnecting') {
    return '重连中'
  }
  if (realtimeStatus.value === 'disconnected') {
    return '已断开'
  }
  return '未启动'
})

const realtimeStatusType = computed(() => {
  if (realtimeStatus.value === 'connected') {
    return 'success'
  }
  if (realtimeStatus.value === 'reconnecting') {
    return 'warning'
  }
  if (realtimeStatus.value === 'disconnected') {
    return 'danger'
  }
  return 'info'
})

const realtimeStatusHint = computed(() => {
  if (realtimeStatus.value === 'connected') {
    return realtimeNotice.value
  }
  if (realtimeStatus.value === 'reconnecting') {
    return '连接波动时会自动重试，无需手动刷新。'
  }
  if (realtimeStatus.value === 'disconnected') {
    return '实时连接已停止，请刷新页面重新建立连接。'
  }
  if (realtimeStatus.value === 'connecting') {
    return '正在建立商家订单推送通道。'
  }
  return '进入页面后会自动建立实时连接。'
})

const exceptionOrderStatuses = new Set(['PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY', 'DELIVERING'])

let refreshPromise: Promise<void> | null = null
let realtimeRefreshTimer: ReturnType<typeof setTimeout> | null = null
let merchantOrdersWsClient: MerchantOrdersWsClient | null = null

const setActiveBoard = (board: MerchantOrderBoardKey) => {
  activeBoard.value = board
}

const actionsFor = (status: string) => {
  if (status === 'PAID') {
    return [
      { key: 'accept' as const, label: '接单', type: 'primary' as const },
      { key: 'reject' as const, label: '拒单', type: 'danger' as const },
    ]
  }
  if (status === 'ACCEPTED') {
    return [{ key: 'prepare' as const, label: '开始备餐', type: 'primary' as const }]
  }
  if (status === 'PREPARING') {
    return [{ key: 'finish' as const, label: '备餐完成', type: 'primary' as const }]
  }
  return []
}

const canReportException = (status: string) => exceptionOrderStatuses.has(status)

const loadOrders = async () => {
  loading.value = true
  try {
    orders.value = await fetchMerchantOrders()
  } catch (error) {
    const message = error instanceof Error ? error.message : '订单加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const loadExceptions = async () => {
  exceptionsLoading.value = true
  try {
    exceptions.value = await fetchMerchantExceptionOrders()
  } catch (error) {
    const message = error instanceof Error ? error.message : '异常订单加载失败'
    ElMessage.error(message)
  } finally {
    exceptionsLoading.value = false
  }
}

const refreshAll = async () => {
  if (refreshPromise) {
    return refreshPromise
  }

  refreshPromise = Promise.all([loadOrders(), loadExceptions()])
    .then(() => undefined)
    .finally(() => {
      refreshPromise = null
    })

  return refreshPromise
}

const scheduleRealtimeRefresh = () => {
  if (realtimeRefreshTimer) {
    return
  }

  realtimeRefreshTimer = setTimeout(() => {
    realtimeRefreshTimer = null
    void refreshAll()
  }, 350)
}

const handleRealtimeEvent = (payload: MerchantOrdersWsPayload) => {
  if (payload.event === 'CONNECTED') {
    realtimeNotice.value = '实时连接已建立，新订单会自动刷新。'
    scheduleRealtimeRefresh()
    return
  }

  const orderHint = payload.orderNo ? `订单号 ${payload.orderNo}` : payload.orderId ? `订单 ${payload.orderId}` : '订单'
  if (payload.event === 'ORDER_CREATED') {
    realtimeNotice.value = `${orderHint} 已推送到当前页面。`
    ElMessage.success(`收到新订单：${orderHint}`)
  } else if (payload.message) {
    realtimeNotice.value = payload.message
    ElMessage.info(payload.message)
  } else {
    realtimeNotice.value = `${orderHint} 状态已更新。`
  }

  scheduleRealtimeRefresh()
}

const startRealtime = () => {
  if (!authStore.token) {
    realtimeStatus.value = 'disconnected'
    realtimeNotice.value = '未读取到登录令牌，实时提醒暂不可用。'
    return
  }

  merchantOrdersWsClient = new MerchantOrdersWsClient({
    token: authStore.token,
    baseUrl: import.meta.env.VITE_API_BASE_URL,
    onStatusChange: (status) => {
      realtimeStatus.value = status
      if (status === 'connected') {
        realtimeNotice.value = '实时连接已建立，新订单会自动刷新。'
      }
    },
    onEvent: handleRealtimeEvent,
    onError: () => {
      realtimeNotice.value = '实时连接异常，系统正在尝试恢复。'
    },
  })

  merchantOrdersWsClient.connect()
}

const stopRealtime = () => {
  if (realtimeRefreshTimer) {
    clearTimeout(realtimeRefreshTimer)
    realtimeRefreshTimer = null
  }

  merchantOrdersWsClient?.disconnect()
  merchantOrdersWsClient = null
}

const handleAction = async (action: ActionKey, orderId: number) => {
  try {
    if (action === 'accept') {
      await acceptOrder(orderId)
    } else if (action === 'reject') {
      await rejectOrder(orderId)
    } else if (action === 'prepare') {
      await prepareOrder(orderId)
    } else if (action === 'finish') {
      await finishOrder(orderId)
    }
    ElMessage.success('操作成功')
    await refreshAll()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(message)
  }
}

const reportException = async (orderId: number) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入异常原因', '上报异常', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '异常原因不能为空',
    })
    await reportMerchantException({ orderId, reason: String(value || '').trim() })
    ElMessage.success('异常已上报')
    await loadExceptions()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    if (error instanceof Error && error.message) {
      ElMessage.error(error.message)
    }
  }
}

const resolveException = async (orderId: number) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入处理说明', '异常处理完成', {
      confirmButtonText: '提交',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '处理说明不能为空',
    })
    await resolveMerchantException({ orderId, resolvedRemark: String(value || '').trim() })
    ElMessage.success('异常处理状态已更新')
    await loadExceptions()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    if (error instanceof Error && error.message) {
      ElMessage.error(error.message)
    }
  }
}

onMounted(async () => {
  await refreshAll()
  startRealtime()
})

onUnmounted(() => {
  stopRealtime()
})
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.realtime-status {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(218, 223, 232, 0.9);
}

.realtime-status span {
  color: var(--cc-color-text-secondary);
  font-size: 12px;
}

.board-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.board-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border: 1px solid var(--cc-color-border);
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  color: var(--cc-color-text-primary);
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(29, 65, 108, 0.04);
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.board-card:hover {
  transform: translateY(-1px);
  border-color: rgba(24, 144, 255, 0.35);
  box-shadow: 0 12px 28px rgba(29, 65, 108, 0.08);
}

.board-card.active {
  border-color: var(--cc-color-primary);
  box-shadow: 0 14px 32px rgba(24, 144, 255, 0.16);
}

.board-card span {
  font-size: 14px;
  font-weight: 600;
}

.board-card strong {
  font-size: 24px;
  line-height: 1;
  color: var(--cc-color-primary);
}

.content-shell {
  margin-top: 16px;
  padding: 18px;
}

.order-list {
  display: grid;
  gap: 14px;
}

.order-card {
  padding: 16px;
  border: 1px solid var(--cc-color-border);
  border-radius: 16px;
  background: #ffffff;
}

.order-card__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.order-card__head h3 {
  margin: 0;
  font-size: 16px;
}

.order-card__head p {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.order-address {
  margin-top: 2px;
  color: var(--cc-color-text-tertiary);
}

.order-card__meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.order-card__meta strong {
  color: var(--cc-color-primary);
  font-size: 16px;
}

.order-card__body {
  display: grid;
  gap: 12px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--cc-color-border);
}

.order-block__title {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--cc-color-text-secondary);
}

.item-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
  color: var(--cc-color-text-primary);
}

.item-row span:last-child {
  flex-shrink: 0;
  color: var(--cc-color-text-secondary);
}

.item-empty {
  color: var(--cc-color-text-tertiary);
  font-size: 13px;
}

.order-block--remark p {
  margin: 0;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.order-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.exception-panel {
  display: grid;
  gap: 12px;
}

.exception-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.exception-head h3 {
  margin: 0;
}

.exception-list {
  display: grid;
  gap: 10px;
}

.exception-card {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 12px;
  align-items: center;
  padding: 14px;
  border-radius: 14px;
  background: #f9fbff;
}

.exception-card__main h4 {
  margin: 0;
  font-size: 15px;
}

.exception-card__main p {
  margin: 6px 0 4px;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.exception-card__main small {
  color: var(--cc-color-text-tertiary);
}

.exception-card__meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 1100px) {
  .board-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .head,
  .order-card__head,
  .exception-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .head-actions,
  .order-card__meta {
    justify-content: flex-start;
  }

  .exception-card {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .board-strip {
    grid-template-columns: 1fr;
  }

  .realtime-status {
    border-radius: 20px;
  }
}
</style>
