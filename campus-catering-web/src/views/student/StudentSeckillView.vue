<template>
  <section class="hero glass-panel">
    <div>
      <p class="eyebrow">限时秒杀</p>
      <h1 class="cc-section-title">{{ storeDetail?.storeName || '门店秒杀专区' }}</h1>
    </div>
    <div class="hero-actions">
      <el-button plain @click="router.push(`/student/store/${storeId}`)">返回门店</el-button>
      <el-button plain @click="loadPageData">刷新活动</el-button>
    </div>
  </section>

  <section class="summary-grid">
    <article class="cc-card summary-card">
      <span>活动数量</span>
      <strong>{{ activityCards.length }}</strong>
      <p>当前门店已配置的秒杀活动都会展示在这里。</p>
    </article>
    <article class="cc-card summary-card accent">
      <span>抢购中</span>
      <strong>{{ ongoingCount }}</strong>
      <p>正在进行中的活动可以直接发起抢购。</p>
    </article>
    <article class="cc-card summary-card">
      <span>最近结果</span>
      <strong>{{ resultMeta.title }}</strong>
      <p>{{ latestResultMessage }}</p>
    </article>
  </section>

  <section class="grid">
    <article class="cc-card panel">
      <div class="panel-head">
        <div>
          <h2>收货信息</h2>
          <p>秒杀成功后会按这里的地址和联系方式创建订单。</p>
        </div>
        <el-button plain :disabled="!defaultAddress" @click="fillWithDefaultAddress">使用默认地址</el-button>
      </div>

      <div v-if="defaultAddress" class="default-address">
        <strong>{{ defaultAddress.contactName }} {{ defaultAddress.contactPhone }}</strong>
        <p>{{ defaultAddress.fullAddress }}</p>
      </div>

      <el-form label-position="top" class="apply-form">
        <el-form-item label="收货人">
          <el-input v-model="form.receiverName" maxlength="24" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.receiverPhone" maxlength="11" />
        </el-form-item>
        <el-form-item label="收货地址" class="full-width">
          <el-input v-model="form.receiverAddress" maxlength="120" />
        </el-form-item>
        <el-form-item label="备注" class="full-width">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="60"
            show-word-limit
            placeholder="例如：到宿舍楼下联系我"
          />
        </el-form-item>
      </el-form>
    </article>

    <article v-if="currentResult" class="cc-card panel result-panel">
      <div class="panel-head">
        <div>
          <h2>抢购结果</h2>
          <p>当前请求号：{{ currentResult.requestId }}</p>
        </div>
        <el-tag :type="resultMeta.tagType" round effect="plain">{{ resultMeta.title }}</el-tag>
      </div>

      <div class="result-body">
        <p>{{ currentResult.message }}</p>
        <div class="result-actions">
          <el-button v-if="shouldPolling" :loading="resultLoading" type="warning" plain>排队处理中</el-button>
          <el-button v-if="resultMeta.canPay && resultMeta.orderRoute" type="primary" @click="router.push(resultMeta.orderRoute)">
            查看订单
          </el-button>
          <el-button v-if="requestIdValue" plain @click="clearResult">清空结果</el-button>
        </div>
      </div>
    </article>
  </section>

  <section class="list">
    <el-skeleton v-if="loading" :rows="5" animated />

    <template v-else>
      <article v-for="activity in activityCards" :key="activity.id" class="cc-card activity-card">
        <div class="activity-main">
          <div class="activity-title">
            <div>
              <h3>{{ activity.activityName }}</h3>
              <p>{{ formatDateTime(activity.startTime) }} 至 {{ formatDateTime(activity.endTime) }}</p>
            </div>
            <div class="activity-tags">
              <el-tag :type="stageTagType(activity.stage)" effect="plain" round>{{ activity.stageLabel }}</el-tag>
              <el-tag type="warning" effect="plain" round>秒杀价 {{ toPrice(activity.seckillPrice) }}</el-tag>
            </div>
          </div>

          <div class="activity-meta">
            <span>剩余库存 {{ activity.stock }} / {{ activity.totalStock ?? activity.stock }}</span>
            <span>SKU {{ activity.skuId }}</span>
            <span>活动 ID {{ activity.id }}</span>
          </div>

          <el-progress :percentage="activity.progressPercent" :stroke-width="10" :show-text="false" />
        </div>

        <div class="activity-side">
          <strong>{{ activity.stage === 'ongoing' ? '正在开抢' : activity.stage === 'upcoming' ? '准备开抢' : '活动结束' }}</strong>
          <el-button
            :type="activity.stage === 'ongoing' ? 'primary' : 'info'"
            :disabled="activity.stage !== 'ongoing'"
            :loading="submittingActivityId === activity.id"
            @click="apply(activity.id)"
          >
            {{ activity.stage === 'ongoing' ? '立即抢购' : '暂不可抢' }}
          </el-button>
        </div>
      </article>

      <el-empty v-if="activityCards.length === 0" description="当前门店还没有可展示的秒杀活动" />
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchAddressList, fetchStoreDetail, type StoreSimpleResponse, type UserAddressResponse } from '@/api/student'
import {
  applyStudentSeckill,
  fetchStudentSeckillActivities,
  fetchStudentSeckillResult,
  type StudentSeckillActivityResponse,
  type StudentSeckillResultResponse,
} from '@/api/seckill'
import {
  buildStudentSeckillActivities,
  buildStudentSeckillApplyPayload,
  createStudentSeckillApplyFormState,
  getStudentSeckillResultMeta,
  shouldPollStudentSeckillResult,
} from '@/views/student/student-seckill'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const resultLoading = ref(false)
const submittingActivityId = ref<number>()
const storeDetail = ref<StoreSimpleResponse>()
const addresses = ref<UserAddressResponse[]>([])
const activities = ref<StudentSeckillActivityResponse[]>([])
const currentResult = ref<StudentSeckillResultResponse | null>(null)
const form = reactive(createStudentSeckillApplyFormState())

const storeId = computed(() => Number(route.params.id))
const requestIdValue = computed(() => String(route.query.requestId || ''))
const defaultAddress = computed(() => addresses.value.find((item) => item.isDefault === 1))
const activityCards = computed(() => buildStudentSeckillActivities(activities.value))
const ongoingCount = computed(() => activityCards.value.filter((item) => item.stage === 'ongoing').length)
const resultMeta = computed(() => getStudentSeckillResultMeta(currentResult.value))
const shouldPolling = computed(() => shouldPollStudentSeckillResult(currentResult.value))
const latestResultMessage = computed(() => currentResult.value?.message || '还没有秒杀请求')

let resultTimer: ReturnType<typeof setTimeout> | null = null

const toPrice = (value?: number) => `${Number(value ?? 0).toFixed(2)}元`
const formatDateTime = (raw: string) => String(raw || '').replace('T', ' ').slice(0, 16)

const stageTagType = (stage: 'ongoing' | 'upcoming' | 'finished') => {
  if (stage === 'ongoing') {
    return 'danger'
  }
  if (stage === 'upcoming') {
    return 'warning'
  }
  return 'info'
}

const fillWithDefaultAddress = () => {
  if (!defaultAddress.value) {
    ElMessage.info('还没有默认地址，请先去地址簿配置')
    return
  }

  form.receiverName = defaultAddress.value.contactName
  form.receiverPhone = defaultAddress.value.contactPhone
  form.receiverAddress = defaultAddress.value.fullAddress
}

const clearResultTimer = () => {
  if (resultTimer) {
    clearTimeout(resultTimer)
    resultTimer = null
  }
}

const pollResult = async (requestId: string) => {
  clearResultTimer()
  if (!requestId) {
    currentResult.value = null
    resultLoading.value = false
    return
  }

  resultLoading.value = true
  try {
    const result = await fetchStudentSeckillResult(requestId)
    currentResult.value = result
    if (shouldPollStudentSeckillResult(result)) {
      resultTimer = setTimeout(() => {
        void pollResult(requestId)
      }, 1500)
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '秒杀结果查询失败'
    ElMessage.error(message)
  } finally {
    resultLoading.value = false
  }
}

const loadPageData = async () => {
  if (!Number.isFinite(storeId.value) || storeId.value <= 0) {
    ElMessage.error('门店参数错误')
    return
  }

  loading.value = true
  try {
    const [detail, seckillActivities] = await Promise.all([
      fetchStoreDetail(storeId.value),
      fetchStudentSeckillActivities(storeId.value),
    ])
    storeDetail.value = detail
    activities.value = seckillActivities
  } catch (error) {
    const message = error instanceof Error ? error.message : '秒杀活动加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }

  try {
    addresses.value = await fetchAddressList()
    if (!form.receiverName && defaultAddress.value) {
      fillWithDefaultAddress()
    }
  } catch {
    addresses.value = []
  }
}

const apply = async (activityId: number) => {
  const payload = buildStudentSeckillApplyPayload(form, activityId)
  if (!payload) {
    ElMessage.warning('请先填写完整的收货信息')
    return
  }

  submittingActivityId.value = activityId
  try {
    const response = await applyStudentSeckill(payload)
    currentResult.value = {
      requestId: response.requestId,
      status: response.status,
      message: response.message,
    }
    ElMessage.success(response.message)
    await router.replace({
      path: route.path,
      query: {
        requestId: response.requestId,
      },
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '秒杀请求提交失败'
    ElMessage.error(message)
  } finally {
    submittingActivityId.value = undefined
  }
}

const clearResult = async () => {
  clearResultTimer()
  currentResult.value = null
  await router.replace({
    path: route.path,
    query: {},
  })
}

watch(
  () => requestIdValue.value,
  (next) => {
    if (!next) {
      clearResultTimer()
      currentResult.value = null
      return
    }
    void pollResult(next)
  },
  { immediate: true },
)

watch(
  () => storeId.value,
  (next, previous) => {
    if (next !== previous) {
      void loadPageData()
    }
  },
)

onMounted(() => {
  void loadPageData()
})

onBeforeUnmount(() => {
  clearResultTimer()
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
  color: #b24c14;
}

.summary-grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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

.summary-card p {
  margin: 10px 0 0;
  color: var(--cc-color-text-secondary);
}

.summary-card.accent {
  background:
    radial-gradient(circle at right top, rgba(255, 179, 102, 0.36), transparent 34%),
    linear-gradient(180deg, #fff7ef, #fffdf9);
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel {
  display: grid;
  gap: 16px;
  padding: 20px;
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

.default-address {
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff8eb;
  border: 1px solid #ffd49c;
}

.default-address p {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
}

.apply-form {
  display: grid;
  gap: 4px 14px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.full-width {
  grid-column: 1 / -1;
}

.result-panel {
  align-content: start;
}

.result-body p {
  margin: 0;
  color: var(--cc-color-text-secondary);
  line-height: 1.7;
}

.result-actions {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.list {
  display: grid;
  gap: 14px;
}

.activity-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 18px;
}

.activity-main {
  min-width: 0;
  flex: 1;
  display: grid;
  gap: 12px;
}

.activity-title {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.activity-title h3,
.activity-title p {
  margin: 0;
}

.activity-title p {
  margin-top: 6px;
  color: var(--cc-color-text-secondary);
}

.activity-tags,
.activity-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.activity-meta {
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.activity-side {
  min-width: 120px;
  display: grid;
  justify-items: end;
  gap: 12px;
}

.activity-side strong {
  color: #b24c14;
}

@media (max-width: 960px) {
  .summary-grid,
  .grid,
  .apply-form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero,
  .panel-head,
  .activity-card,
  .activity-title {
    flex-direction: column;
    align-items: flex-start;
  }

  .activity-side {
    justify-items: start;
  }
}
</style>
