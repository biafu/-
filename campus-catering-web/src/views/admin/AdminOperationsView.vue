<template>
  <section class="hero glass-panel">
    <div>
      <h1 class="cc-section-title">运营动作</h1>
    </div>
  </section>

  <section class="summary-grid">
    <article class="cc-card summary-card">
      <span>活动总数</span>
      <strong>{{ seckillActivities.length }}</strong>
      <p>包含启用、停用和已结束的秒杀活动。</p>
    </article>
    <article class="cc-card summary-card accent">
      <span>进行中</span>
      <strong>{{ runningActivityCount }}</strong>
      <p>当前已经开始且仍处于启用状态的活动数量。</p>
    </article>
    <article class="cc-card summary-card">
      <span>待开始</span>
      <strong>{{ upcomingActivityCount }}</strong>
      <p>尚未开始的活动可以随时回到表单继续调整。</p>
    </article>
  </section>

  <section class="grid">
    <article class="cc-card panel">
      <div class="panel-head">
        <h2>门店状态</h2>
        <p>按门店 ID 执行启用或停用。</p>
      </div>
      <el-form label-position="top">
        <el-form-item label="门店 ID">
          <el-input v-model.number="storeAction.storeId" type="number" placeholder="例如：1001" />
        </el-form-item>
      </el-form>
      <div class="panel-actions">
        <el-button type="primary" :loading="storeSubmitting === 'enable'" @click="submitStoreAction('enable')">
          启用门店
        </el-button>
        <el-button
          type="danger"
          plain
          :loading="storeSubmitting === 'disable'"
          @click="submitStoreAction('disable')"
        >
          停用门店
        </el-button>
      </div>
    </article>

    <article class="cc-card panel">
      <div class="panel-head">
        <h2>手动派单</h2>
        <p>当自动流转异常时，可直接指定订单和骑手完成调度。</p>
      </div>
      <el-form label-position="top">
        <el-form-item label="订单 ID">
          <el-input v-model.number="dispatchForm.orderId" type="number" placeholder="例如：18" />
        </el-form-item>
        <el-form-item label="骑手用户 ID">
          <el-input v-model.number="dispatchForm.deliveryUserId" type="number" placeholder="例如：1" />
        </el-form-item>
        <el-form-item label="调度备注">
          <el-input
            v-model="dispatchForm.dispatchRemark"
            type="textarea"
            :rows="3"
            maxlength="80"
            show-word-limit
            placeholder="例如：优先配送到图书馆门口"
          />
        </el-form-item>
      </el-form>
      <div class="dispatch-tip">当前种子数据可直接使用骑手 ID 1（rider01）验证派单链路。</div>
      <div class="panel-actions">
        <el-button type="primary" :loading="dispatchSubmitting" @click="submitDispatch">执行派单</el-button>
        <el-button plain :disabled="dispatchSubmitting" @click="resetDispatchForm">清空</el-button>
      </div>
    </article>

    <article class="cc-card panel">
      <div class="panel-head">
        <h2>重算日报</h2>
        <p>手动重建指定日期的平台统计。</p>
      </div>
      <el-form label-position="top">
        <el-form-item label="统计日期">
          <el-date-picker v-model="rebuildDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
      </el-form>
      <div class="panel-actions">
        <el-button type="primary" :loading="rebuildSubmitting" @click="submitRebuild">执行重算</el-button>
      </div>
    </article>

    <article class="cc-card panel wide">
      <div class="panel-head">
        <div>
          <h2>{{ seckillForm.id ? '编辑秒杀活动' : '创建秒杀活动' }}</h2>
          <p>活动创建后仍可继续编辑库存、时间和状态，停用后也能重新启用。</p>
        </div>
        <el-button v-if="seckillForm.id" plain @click="resetSeckillForm">取消编辑</el-button>
      </div>
      <el-form label-position="top" class="seckill-form">
        <el-form-item label="活动名称">
          <el-input v-model="seckillForm.activityName" maxlength="64" />
        </el-form-item>
        <el-form-item label="门店 ID">
          <el-input v-model.number="seckillForm.storeId" type="number" />
        </el-form-item>
        <el-form-item label="SKU ID">
          <el-input v-model.number="seckillForm.skuId" type="number" />
        </el-form-item>
        <el-form-item label="秒杀价">
          <el-input v-model.number="seckillForm.seckillPrice" type="number" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input v-model.number="seckillForm.stock" type="number" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="seckillForm.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择开始时间"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="seckillForm.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择结束时间"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-segmented
            v-model="seckillForm.status"
            :options="[
              { label: '启用', value: 1 },
              { label: '停用', value: 0 },
            ]"
          />
        </el-form-item>
      </el-form>
      <div class="panel-actions">
        <el-button type="primary" :loading="seckillSubmitting" @click="submitSeckill">
          {{ seckillForm.id ? '保存活动' : '创建活动' }}
        </el-button>
      </div>
    </article>

    <article class="cc-card panel wide">
      <div class="panel-head">
        <div>
          <h2>秒杀活动列表</h2>
          <p>支持查看、编辑、停用和删除，方便统一维护活动状态。</p>
        </div>
        <el-button round :loading="activityLoading" @click="loadSeckillActivities">刷新列表</el-button>
      </div>

      <el-skeleton v-if="activityLoading" :rows="5" animated />
      <div v-else-if="seckillActivities.length === 0" class="empty-wrap">
        <el-empty description="当前还没有秒杀活动" />
      </div>
      <div v-else class="activity-list">
        <article v-for="activity in seckillActivities" :key="activity.id" class="activity-card">
          <div class="activity-main">
            <div class="activity-title">
              <h3>{{ activity.activityName }}</h3>
              <div class="activity-tags">
                <el-tag :type="activity.status === 1 ? 'success' : 'info'" effect="plain" round>
                  {{ activity.status === 1 ? '已启用' : '已停用' }}
                </el-tag>
                <el-tag :type="activityStageType(activity)" effect="plain" round>
                  {{ activityStageLabel(activity) }}
                </el-tag>
              </div>
            </div>
            <div class="activity-meta">
              <span>活动 ID {{ activity.id }}</span>
              <span>门店 {{ activity.storeId }}</span>
              <span>SKU {{ activity.skuId }}</span>
              <span>剩余库存 {{ activity.stock }} / 总库存 {{ activity.totalStock ?? activity.stock }}</span>
              <span>秒杀价 {{ toPrice(activity.seckillPrice) }}</span>
            </div>
            <p class="activity-time">{{ formatDateTime(activity.startTime) }} 至 {{ formatDateTime(activity.endTime) }}</p>
          </div>
          <div class="activity-actions">
            <el-button size="small" @click="editSeckill(activity)">编辑</el-button>
            <el-button size="small" plain @click="toggleSeckillStatus(activity)">
              {{ activity.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="removeSeckill(activity.id)">删除</el-button>
          </div>
        </article>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteAdminSeckillActivity,
  disableAdminStore,
  dispatchAdminDelivery,
  enableAdminStore,
  fetchAdminSeckillActivities,
  rebuildAdminDailyStatistics,
  saveAdminSeckillActivity,
  type SeckillActivityResponse,
  type SeckillActivitySaveRequest,
} from '@/api/admin'
import { buildAdminDispatchPayload, createAdminDispatchForm } from '@/views/admin/admin-operations'

type EditableSeckillForm = {
  id?: number
  activityName: string
  storeId: number | undefined
  skuId: number | undefined
  seckillPrice: number | undefined
  stock: number | undefined
  startTime: string
  endTime: string
  status: number
}

const storeSubmitting = ref<'enable' | 'disable' | null>(null)
const dispatchSubmitting = ref(false)
const rebuildSubmitting = ref(false)
const seckillSubmitting = ref(false)
const activityLoading = ref(false)
const rebuildDate = ref('')
const seckillActivities = ref<SeckillActivityResponse[]>([])

const storeAction = reactive({
  storeId: undefined as number | undefined,
})

const dispatchForm = reactive(createAdminDispatchForm())

const createEmptySeckillForm = (): EditableSeckillForm => ({
  id: undefined,
  activityName: '',
  storeId: undefined,
  skuId: undefined,
  seckillPrice: undefined,
  stock: undefined,
  startTime: '',
  endTime: '',
  status: 1,
})

const seckillForm = reactive<EditableSeckillForm>(createEmptySeckillForm())

const nowTime = () => new Date().getTime()

const activityStageLabel = (activity: SeckillActivityResponse) => {
  const now = nowTime()
  const start = new Date(activity.startTime).getTime()
  const end = new Date(activity.endTime).getTime()
  if (now < start) {
    return '待开始'
  }
  if (now > end) {
    return '已结束'
  }
  return '进行中'
}

const activityStageType = (activity: SeckillActivityResponse) => {
  const label = activityStageLabel(activity)
  if (label === '进行中') {
    return 'danger'
  }
  if (label === '待开始') {
    return 'warning'
  }
  return 'info'
}

const runningActivityCount = computed(
  () => seckillActivities.value.filter((activity) => activity.status === 1 && activityStageLabel(activity) === '进行中').length,
)

const upcomingActivityCount = computed(
  () => seckillActivities.value.filter((activity) => activityStageLabel(activity) === '待开始').length,
)

const toPrice = (value?: number) => `${Number(value ?? 0).toFixed(2)}元`
const formatDateTime = (raw: string) => String(raw || '').replace('T', ' ').slice(0, 16)
const normalizeDateTime = (raw?: string) => String(raw || '').replace('T', ' ').slice(0, 19)

const resetDispatchForm = () => {
  Object.assign(dispatchForm, createAdminDispatchForm())
}

const resetSeckillForm = () => {
  Object.assign(seckillForm, createEmptySeckillForm())
}

const buildSeckillPayload = (input: EditableSeckillForm): SeckillActivitySaveRequest | null => {
  if (
    !input.activityName.trim() ||
    !input.storeId ||
    !input.skuId ||
    !input.seckillPrice ||
    input.seckillPrice <= 0 ||
    !input.stock ||
    input.stock <= 0 ||
    !input.startTime ||
    !input.endTime
  ) {
    return null
  }

  if (new Date(input.endTime).getTime() <= new Date(input.startTime).getTime()) {
    throw new Error('结束时间必须晚于开始时间')
  }

  return {
    id: input.id,
    activityName: input.activityName.trim(),
    storeId: input.storeId,
    skuId: input.skuId,
    seckillPrice: input.seckillPrice,
    stock: input.stock,
    startTime: input.startTime,
    endTime: input.endTime,
    status: input.status,
  }
}

const loadSeckillActivities = async () => {
  activityLoading.value = true
  try {
    seckillActivities.value = await fetchAdminSeckillActivities()
  } catch (error) {
    const message = error instanceof Error ? error.message : '秒杀活动加载失败'
    ElMessage.error(message)
  } finally {
    activityLoading.value = false
  }
}

const submitStoreAction = async (action: 'enable' | 'disable') => {
  if (!storeAction.storeId) {
    ElMessage.warning('请先填写门店 ID')
    return
  }

  storeSubmitting.value = action
  try {
    if (action === 'enable') {
      await enableAdminStore(storeAction.storeId)
      ElMessage.success('门店已启用')
    } else {
      await disableAdminStore(storeAction.storeId)
      ElMessage.success('门店已停用')
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '门店状态操作失败'
    ElMessage.error(message)
  } finally {
    storeSubmitting.value = null
  }
}

const submitDispatch = async () => {
  const payload = buildAdminDispatchPayload(dispatchForm)
  if (!payload) {
    ElMessage.warning('请填写有效的订单 ID 和骑手用户 ID')
    return
  }

  dispatchSubmitting.value = true
  try {
    await dispatchAdminDelivery(payload)
    ElMessage.success(`订单 ${payload.orderId} 已派给骑手 ${payload.deliveryUserId}`)
    resetDispatchForm()
  } catch (error) {
    const message = error instanceof Error ? error.message : '派单失败'
    ElMessage.error(message)
  } finally {
    dispatchSubmitting.value = false
  }
}

const submitRebuild = async () => {
  if (!rebuildDate.value) {
    ElMessage.warning('请先选择统计日期')
    return
  }

  rebuildSubmitting.value = true
  try {
    await rebuildAdminDailyStatistics(rebuildDate.value)
    ElMessage.success('日报已触发重算')
  } catch (error) {
    const message = error instanceof Error ? error.message : '日报重算失败'
    ElMessage.error(message)
  } finally {
    rebuildSubmitting.value = false
  }
}

const submitSeckill = async () => {
  let payload: SeckillActivitySaveRequest | null
  try {
    payload = buildSeckillPayload(seckillForm)
  } catch (error) {
    const message = error instanceof Error ? error.message : '秒杀活动时间配置不正确'
    ElMessage.warning(message)
    return
  }

  if (!payload) {
    ElMessage.warning('请先填写完整的秒杀活动信息')
    return
  }

  seckillSubmitting.value = true
  try {
    const activityId = await saveAdminSeckillActivity(payload)
    ElMessage.success(seckillForm.id ? `活动已更新，ID ${activityId}` : `活动已创建，ID ${activityId}`)
    resetSeckillForm()
    await loadSeckillActivities()
  } catch (error) {
    const message = error instanceof Error ? error.message : '秒杀活动保存失败'
    ElMessage.error(message)
  } finally {
    seckillSubmitting.value = false
  }
}

const editSeckill = (activity: SeckillActivityResponse) => {
  Object.assign(seckillForm, {
    id: activity.id,
    activityName: activity.activityName,
    storeId: activity.storeId,
    skuId: activity.skuId,
    seckillPrice: Number(activity.seckillPrice),
    stock: Number(activity.totalStock ?? activity.stock),
    startTime: normalizeDateTime(activity.startTime),
    endTime: normalizeDateTime(activity.endTime),
    status: activity.status,
  })
}

const toggleSeckillStatus = async (activity: SeckillActivityResponse) => {
  try {
    await saveAdminSeckillActivity({
      id: activity.id,
      activityName: activity.activityName,
      storeId: activity.storeId,
      skuId: activity.skuId,
      seckillPrice: Number(activity.seckillPrice),
      stock: Number(activity.totalStock ?? activity.stock),
      startTime: normalizeDateTime(activity.startTime),
      endTime: normalizeDateTime(activity.endTime),
      status: activity.status === 1 ? 0 : 1,
    })
    ElMessage.success(activity.status === 1 ? '活动已停用' : '活动已启用')
    await loadSeckillActivities()
  } catch (error) {
    const message = error instanceof Error ? error.message : '活动状态更新失败'
    ElMessage.error(message)
  }
}

const removeSeckill = async (activityId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个秒杀活动吗？删除后无法恢复。', '删除确认', {
      type: 'warning',
    })
    await deleteAdminSeckillActivity(activityId)
    ElMessage.success('活动已删除')
    if (seckillForm.id === activityId) {
      resetSeckillForm()
    }
    await loadSeckillActivities()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    const message = error instanceof Error ? error.message : '删除活动失败'
    ElMessage.error(message)
  }
}

onMounted(() => {
  void loadSeckillActivities()
})
</script>

<style scoped>
.hero {
  padding: 20px;
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
  display: block;
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
    radial-gradient(circle at right top, rgba(255, 216, 122, 0.38), transparent 34%),
    linear-gradient(180deg, #fffef7, #fff8e7);
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

.panel.wide {
  grid-column: 1 / -1;
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

.panel-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.dispatch-tip {
  margin-top: -6px;
  color: var(--cc-color-text-secondary);
  font-size: 12px;
}

.seckill-form {
  display: grid;
  gap: 4px 14px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.activity-list {
  display: grid;
  gap: 12px;
}

.activity-card {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid var(--cc-color-border);
  background: color-mix(in srgb, white 85%, #f5f1e7 15%);
}

.activity-main {
  display: grid;
  gap: 10px;
}

.activity-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.activity-title h3,
.activity-time {
  margin: 0;
}

.activity-tags,
.activity-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.activity-meta,
.activity-time {
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.activity-actions {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-wrap {
  padding: 12px 0;
}

@media (max-width: 960px) {
  .summary-grid,
  .grid,
  .seckill-form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .panel-head,
  .activity-card,
  .activity-title {
    flex-direction: column;
  }
}
</style>
