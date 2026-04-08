<template>
  <section class="hero glass-panel">
    <div>
      <h1 class="cc-section-title">平台总览</h1>
    </div>
    <el-button type="primary" round :loading="loading" @click="loadData">刷新</el-button>
  </section>

  <section class="metrics">
    <article class="cc-card metric-card">
      <span>平台订单</span>
      <strong>{{ dashboard?.orderCount ?? 0 }}</strong>
    </article>
    <article class="cc-card metric-card">
      <span>平台营收</span>
      <strong>{{ toPrice(dashboard?.totalRevenue ?? 0) }}</strong>
    </article>
    <article class="cc-card metric-card">
      <span>活跃商户</span>
      <strong>{{ dashboard?.merchantCount ?? 0 }}</strong>
    </article>
    <article class="cc-card metric-card">
      <span>活跃用户</span>
      <strong>{{ dashboard?.activeUserCount ?? 0 }}</strong>
    </article>
  </section>

  <section class="panels">
    <article class="cc-card panel">
      <div class="panel-head">
        <div>
          <h2>近 7 日统计</h2>
          <p>用于快速判断订单、履约和取消情况。</p>
        </div>
      </div>
      <el-table :data="dailyStats" size="small" empty-text="暂无统计数据">
        <el-table-column prop="statDate" label="日期" min-width="110" />
        <el-table-column prop="orderCount" label="订单数" min-width="90" />
        <el-table-column prop="completedOrderCount" label="完成数" min-width="90" />
        <el-table-column prop="cancelledOrderCount" label="取消数" min-width="90" />
        <el-table-column label="GMV" min-width="110">
          <template #default="{ row }">{{ toPrice(row.gmv) }}</template>
        </el-table-column>
        <el-table-column label="取消率" min-width="90">
          <template #default="{ row }">{{ toPercent(row.cancelRate) }}</template>
        </el-table-column>
      </el-table>
    </article>

    <article class="cc-card panel">
      <div class="panel-head">
        <div>
          <h2>商户排行</h2>
          <p>按近 7 日订单和 GMV 看平台头部商户。</p>
        </div>
      </div>
      <el-table :data="merchantRank" size="small" empty-text="暂无排行数据">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="merchantName" label="商户" min-width="150" />
        <el-table-column prop="orderCount" label="订单数" min-width="90" />
        <el-table-column label="GMV" min-width="110">
          <template #default="{ row }">{{ toPrice(row.gmv) }}</template>
        </el-table-column>
      </el-table>
    </article>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchAdminDashboard,
  fetchAdminDailyStatistics,
  fetchAdminMerchantRank,
  type AdminDashboardResponse,
  type DailyStatisticsResponse,
  type MerchantRankResponse,
} from '@/api/admin'

const loading = ref(false)
const dashboard = ref<AdminDashboardResponse | null>(null)
const dailyStats = ref<DailyStatisticsResponse[]>([])
const merchantRank = ref<MerchantRankResponse[]>([])

const toPrice = (value: number) => `${Number(value ?? 0).toFixed(2)}元`
const toPercent = (value: number) => `${(Number(value ?? 0) * 100).toFixed(1)}%`

const loadData = async () => {
  loading.value = true
  try {
    const [dashboardResult, dailyResult, rankResult] = await Promise.all([
      fetchAdminDashboard(),
      fetchAdminDailyStatistics(),
      fetchAdminMerchantRank(),
    ])
    dashboard.value = dashboardResult
    dailyStats.value = dailyResult
    merchantRank.value = rankResult
  } catch (error) {
    const message = error instanceof Error ? error.message : '管理员总览加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

void loadData()
</script>

<style scoped>
.hero,
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
}

.metrics {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.metric-card,
.panel {
  padding: 18px 20px;
}

.metric-card {
  display: grid;
  gap: 10px;
}

.metric-card span,
.panel-head p {
  color: var(--cc-color-text-secondary);
}

.metric-card strong {
  font-size: 30px;
}

.panels {
  display: grid;
  gap: 16px;
}

.panel h2 {
  margin: 0;
  font-size: 18px;
}

.panel-head {
  padding: 0 0 16px;
}

@media (max-width: 960px) {
  .metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .hero,
  .panel-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .metrics {
    grid-template-columns: 1fr;
  }
}
</style>
