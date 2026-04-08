<template>
  <section class="head glass-panel">
    <div>
      <h1 class="cc-section-title">经营总览</h1>
    </div>
    <button class="cc-btn-primary refresh" @click="loadDashboard">刷新数据</button>
  </section>

  <section class="stats">
    <RevealOnScroll v-for="card in stats" :key="card.label">
      <article class="cc-card stat hover-lift">
        <p>{{ card.label }}</p>
        <h3>{{ card.value }}</h3>
        <span>{{ card.tip }}</span>
      </article>
    </RevealOnScroll>
  </section>

  <section class="cc-card chart">
    <h3>待处理提醒</h3>
    <p>当前待处理订单 {{ pendingCount }} 单，建议优先处理 10 分钟内下单的订单。</p>
  </section>

  <section class="charts-grid">
    <article class="cc-card chart-card">
      <h3>近7日订单趋势</h3>
      <div v-if="trend.length === 0" class="empty">暂无趋势数据</div>
      <div v-else class="trend-chart">
        <svg viewBox="0 0 360 180" preserveAspectRatio="none" class="trend-svg">
          <defs>
            <linearGradient id="trendGradient" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="#4f8dff" stop-opacity="0.35" />
              <stop offset="100%" stop-color="#4f8dff" stop-opacity="0.05" />
            </linearGradient>
          </defs>
          <path v-if="trendAreaPath" :d="trendAreaPath" fill="url(#trendGradient)" />
          <polyline :points="trendPolyline" fill="none" stroke="#2f6df6" stroke-width="3" stroke-linecap="round" />
          <circle
            v-for="point in trendPoints"
            :key="point.statDate"
            :cx="point.x"
            :cy="point.y"
            r="4"
            fill="#2f6df6"
          />
        </svg>
        <div class="trend-axis">
          <span v-for="row in trend" :key="`axis-${row.statDate}`">{{ shortDate(row.statDate) }}</span>
        </div>
      </div>
    </article>

    <article class="cc-card chart-card">
      <h3>订单状态分布</h3>
      <div v-if="statusDistribution.length === 0" class="empty">暂无状态数据</div>
      <ul v-else class="bar-list">
        <li v-for="row in statusBars" :key="row.orderStatus">
          <div class="bar-meta">
            <span>{{ statusLabel(row.orderStatus) }}</span>
            <strong>{{ row.orderCount }}单 · {{ row.ratioText }}</strong>
          </div>
          <div class="bar-track">
            <span class="bar-fill" :style="{ width: `${row.ratio}%` }" />
          </div>
        </li>
      </ul>
    </article>

    <article class="cc-card chart-card">
      <h3>热销商品排行</h3>
      <div v-if="hotProducts.length === 0" class="empty">暂无热销数据</div>
      <ul v-else class="bar-list">
        <li v-for="row in hotProductBars" :key="`${row.spuName}-${row.skuName}`">
          <div class="bar-meta">
            <span>{{ row.spuName }} {{ row.skuName }}</span>
            <strong>{{ row.soldQuantity }}份 · {{ Number(row.gmv ?? 0).toFixed(2) }}元</strong>
          </div>
          <div class="bar-track hot-track">
            <span class="bar-fill hot-fill" :style="{ width: `${row.ratio}%` }" />
          </div>
        </li>
      </ul>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import RevealOnScroll from '@/components/RevealOnScroll.vue'
import {
  fetchMerchantHotProducts,
  fetchMerchantOrders,
  fetchMerchantOverview,
  fetchMerchantStatusDistribution,
  fetchMerchantTrend,
  type MerchantHotProductResponse,
  type MerchantStatusDistributionResponse,
  type MerchantTrendPointResponse,
} from '@/api/merchant'

const todayOrderCount = ref(0)
const totalOrderCount = ref(0)
const todayRevenue = ref(0)
const pendingCount = ref(0)
const trend = ref<MerchantTrendPointResponse[]>([])
const statusDistribution = ref<MerchantStatusDistributionResponse[]>([])
const hotProducts = ref<MerchantHotProductResponse[]>([])

const stats = computed(() => [
  { label: '今日订单', value: String(todayOrderCount.value), tip: '商家接口' },
  { label: '总订单数', value: String(totalOrderCount.value), tip: '历史累计' },
  { label: '今日营业额', value: `${todayRevenue.value.toFixed(2)}元`, tip: '已支付订单' },
  { label: '待处理订单', value: String(pendingCount.value), tip: '待接单/备餐/配送' },
])

const trendPoints = computed(() => {
  const rows = trend.value
  if (rows.length === 0) {
    return []
  }
  const width = 360
  const height = 180
  const paddingX = 20
  const paddingTop = 16
  const paddingBottom = 24
  const chartHeight = height - paddingTop - paddingBottom
  const maxValue = Math.max(...rows.map((row) => Number(row.orderCount ?? 0)), 1)
  return rows.map((row, index) => {
    const x =
      rows.length === 1
        ? width / 2
        : paddingX + (index * (width - paddingX * 2)) / (rows.length - 1)
    const y = paddingTop + (1 - Number(row.orderCount ?? 0) / maxValue) * chartHeight
    return {
      statDate: row.statDate,
      x: Number(x.toFixed(2)),
      y: Number(y.toFixed(2)),
    }
  })
})

const trendPolyline = computed(() => trendPoints.value.map((point) => `${point.x},${point.y}`).join(' '))

const trendAreaPath = computed(() => {
  if (trendPoints.value.length === 0) {
    return ''
  }
  const baseline = 180
  const first = trendPoints.value[0]
  const path = trendPoints.value.map((point) => `L ${point.x} ${point.y}`).join(' ')
  const last = trendPoints.value[trendPoints.value.length - 1]
  return `M ${first.x} ${baseline} ${path} L ${last.x} ${baseline} Z`
})

const statusBars = computed(() => {
  const total = statusDistribution.value.reduce((sum, row) => sum + Number(row.orderCount ?? 0), 0)
  return statusDistribution.value.map((row) => {
    const count = Number(row.orderCount ?? 0)
    const ratio = total > 0 ? Math.round((count / total) * 1000) / 10 : 0
    return {
      ...row,
      orderCount: count,
      ratio,
      ratioText: `${ratio.toFixed(1)}%`,
    }
  })
})

const hotProductBars = computed(() => {
  const rows = hotProducts.value
  const maxSold = Math.max(...rows.map((row) => Number(row.soldQuantity ?? 0)), 1)
  return rows.map((row) => {
    const soldQuantity = Number(row.soldQuantity ?? 0)
    const ratio = Math.max(8, Math.round((soldQuantity / maxSold) * 100))
    return {
      ...row,
      soldQuantity,
      ratio,
    }
  })
})

const loadDashboard = async () => {
  try {
    const [overview, orders, trendRows, statusRows, hotRows] = await Promise.all([
      fetchMerchantOverview(),
      fetchMerchantOrders(),
      fetchMerchantTrend(7),
      fetchMerchantStatusDistribution(7),
      fetchMerchantHotProducts(7, 8),
    ])
    todayOrderCount.value = Number(overview.todayOrderCount ?? 0)
    totalOrderCount.value = Number(overview.totalOrderCount ?? 0)
    todayRevenue.value = Number(overview.todayRevenue ?? 0)
    pendingCount.value = orders.filter((order) =>
      ['PAID', 'ACCEPTED', 'PREPARING', 'WAITING_DELIVERY'].includes(order.orderStatus),
    ).length
    trend.value = trendRows
    statusDistribution.value = statusRows
    hotProducts.value = hotRows
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载经营数据失败'
    ElMessage.error(message)
  }
}

const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING_PAYMENT: '待支付',
    PAID: '待接单',
    ACCEPTED: '已接单',
    PREPARING: '备餐中',
    WAITING_DELIVERY: '待配送',
    DELIVERING: '配送中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
  }
  return map[status] ?? status
}

const shortDate = (value: string) => {
  if (!value) {
    return '-'
  }
  return value.length >= 10 ? value.slice(5, 10) : value
}

onMounted(loadDashboard)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 20px;
}

.refresh {
  width: 108px;
  height: 40px;
}

.stats {
  margin-top: 16px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.stat {
  padding: 16px;
}

.stat p {
  margin: 0;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.stat h3 {
  margin: 8px 0 6px;
  font-size: 28px;
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.stat span {
  color: var(--cc-color-success);
  font-weight: 600;
  font-size: 13px;
}

.chart {
  margin-top: 16px;
  padding: 20px;
}

.chart h3 {
  margin: 0;
  font-size: 20px;
}

.chart p {
  margin: 10px 0 0;
  color: var(--cc-color-text-secondary);
}

.charts-grid {
  margin-top: 16px;
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.chart-card {
  padding: 16px;
}

.chart-card h3 {
  margin: 0 0 12px;
  font-size: 17px;
}

.trend-chart {
  display: grid;
  gap: 8px;
}

.trend-svg {
  width: 100%;
  height: 180px;
  background: linear-gradient(180deg, #f6f9ff 0%, #ffffff 100%);
  border-radius: 10px;
}

.trend-axis {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

.bar-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 8px;
}

.bar-list li {
  display: grid;
  gap: 6px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f9fbff;
}

.bar-meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.bar-meta span {
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.bar-meta strong {
  color: #0f1728;
  font-size: 13px;
}

.bar-track {
  height: 8px;
  border-radius: 999px;
  background: #e8efff;
  overflow: hidden;
}

.bar-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2f6df6, #78a2ff);
}

.hot-track {
  background: #fff1d8;
}

.hot-fill {
  background: linear-gradient(90deg, #ff8a38, #ffc34d);
}

.empty {
  color: var(--cc-color-text-tertiary);
  font-size: 13px;
}

@media (max-width: 1100px) {
  .stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .head {
    flex-direction: column;
    align-items: flex-start;
  }

  .stats {
    grid-template-columns: 1fr;
  }
}
</style>
