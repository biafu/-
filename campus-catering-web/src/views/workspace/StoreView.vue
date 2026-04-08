<template>
  <section class="head glass-panel">
    <div>
      <h1 class="cc-section-title">门店设置</h1>
    </div>
    <el-switch
      v-model="businessOpen"
      inline-prompt
      active-text="营业中"
      inactive-text="休息中"
      :disabled="loading"
      @change="syncBusinessStatus"
    />
  </section>

  <section class="cc-card form-wrap">
    <div class="toolbar">
      <span class="hint">保存前可先检查配送范围、起送门槛和营业时间是否同步。</span>
      <div class="toolbar-actions">
        <el-button round :disabled="loading || saving" @click="resetForm">重置</el-button>
        <el-button type="primary" round :loading="saving" :disabled="loading" @click="save">保存</el-button>
      </div>
    </div>

    <el-skeleton v-if="loading" :rows="12" animated />

    <el-form v-else label-position="top" size="large">
      <div class="grid">
        <el-form-item label="门店名称">
          <el-input v-model="form.storeName" />
        </el-form-item>
        <el-form-item label="门店地址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="配送范围说明">
          <el-input v-model="form.deliveryScopeDesc" placeholder="例如：主校区、东区宿舍、西区宿舍" />
        </el-form-item>
        <el-form-item label="配送半径（公里）">
          <el-input-number v-model="form.deliveryRadiusKm" :min="0" :step="0.5" :precision="2" />
        </el-form-item>
        <el-form-item label="起送价（元）">
          <el-input-number v-model="form.minOrderAmount" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="配送费（元）">
          <el-input-number v-model="form.deliveryFee" :min="0" :step="1" />
        </el-form-item>
      </div>

      <el-form-item label="营业公告">
        <el-input v-model="form.notice" type="textarea" :rows="4" maxlength="120" show-word-limit />
      </el-form-item>

      <section class="hours-panel">
        <div class="hours-head">
          <div>
            <h2>营业时间</h2>
            <p>按星期配置营业时间，关闭的日期不会出现在学生端可营业状态中。</p>
          </div>
          <el-tag type="info" round>本周营业 {{ enabledBusinessHoursCount }} 天</el-tag>
        </div>

        <div class="hours-grid">
          <article v-for="row in businessHours" :key="row.dayOfWeek" class="hours-row">
            <div class="hours-day">
              <strong>{{ row.label }}</strong>
              <span>{{ row.status === 1 ? '营业中' : '休息中' }}</span>
            </div>
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="开"
              inactive-text="关"
            />
            <div class="hours-time">
              <el-time-picker
                v-model="row.startTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="开始时间"
                :disabled="row.status !== 1"
              />
              <span>至</span>
              <el-time-picker
                v-model="row.endTime"
                format="HH:mm"
                value-format="HH:mm"
                placeholder="结束时间"
                :disabled="row.status !== 1"
              />
            </div>
          </article>
        </div>
      </section>
    </el-form>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  fetchMerchantStoreDetail,
  fetchStoreBusinessHours,
  saveStoreBusinessHours,
  updateMerchantStore,
} from '@/api/merchant'
import {
  applyMerchantStoreSnapshot,
  buildMerchantStoreSnapshot,
  buildMerchantStoreUpdatePayload,
  createMerchantStoreFormState,
  hasMerchantStoreRequiredFields,
  type MerchantStoreFormState,
} from '@/views/workspace/merchant-store'
import {
  buildMerchantBusinessHoursPayload,
  buildMerchantBusinessHoursRows,
  getMerchantBusinessHoursValidationError,
  type MerchantBusinessHourFormRow,
} from '@/views/workspace/merchant-business-hours'

const loading = ref(false)
const saving = ref(false)
const businessOpen = ref(true)
const form = reactive(createMerchantStoreFormState())
const businessHours = ref<MerchantBusinessHourFormRow[]>(buildMerchantBusinessHoursRows([]))
const lastSnapshot = ref<MerchantStoreFormState>(createMerchantStoreFormState())
const lastBusinessHoursSnapshot = ref<MerchantBusinessHourFormRow[]>(buildMerchantBusinessHoursRows([]))

const enabledBusinessHoursCount = computed(() => businessHours.value.filter((row) => Number(row.status) === 1).length)

const cloneBusinessHours = (rows: MerchantBusinessHourFormRow[]) => rows.map((row) => ({ ...row }))

const syncBusinessStatus = () => {
  form.businessStatus = businessOpen.value ? 1 : 0
}

const loadStoreDetail = async () => {
  loading.value = true
  try {
    const [detail, hours] = await Promise.all([fetchMerchantStoreDetail(), fetchStoreBusinessHours()])
    const snapshot = buildMerchantStoreSnapshot(detail)
    const hourRows = buildMerchantBusinessHoursRows(hours)
    lastSnapshot.value = snapshot
    lastBusinessHoursSnapshot.value = cloneBusinessHours(hourRows)
    applyMerchantStoreSnapshot(form, snapshot)
    businessHours.value = cloneBusinessHours(hourRows)
    businessOpen.value = snapshot.businessStatus === 1
  } catch (error) {
    const message = error instanceof Error ? error.message : '门店信息加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  applyMerchantStoreSnapshot(form, lastSnapshot.value)
  businessHours.value = cloneBusinessHours(lastBusinessHoursSnapshot.value)
  businessOpen.value = form.businessStatus === 1
}

const save = async () => {
  syncBusinessStatus()

  if (!hasMerchantStoreRequiredFields(form)) {
    ElMessage.warning('请填写门店名称和地址')
    return
  }

  const businessHoursError = getMerchantBusinessHoursValidationError(businessHours.value)
  if (businessHoursError) {
    ElMessage.warning(businessHoursError)
    return
  }

  saving.value = true
  try {
    await Promise.all([
      updateMerchantStore(buildMerchantStoreUpdatePayload(form)),
      saveStoreBusinessHours(buildMerchantBusinessHoursPayload(businessHours.value)),
    ])
    ElMessage.success('门店设置已保存')
    await loadStoreDetail()
  } catch (error) {
    const message = error instanceof Error ? error.message : '保存失败'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void loadStoreDetail()
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

.form-wrap {
  margin-top: 16px;
  padding: 18px;
  display: grid;
  gap: 18px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.hint {
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.hours-panel {
  margin-top: 8px;
  padding: 18px;
  border: 1px solid var(--cc-color-border);
  border-radius: 20px;
  background:
    radial-gradient(circle at top right, rgba(255, 214, 160, 0.24), transparent 34%),
    linear-gradient(180deg, #fffdf8 0%, #ffffff 100%);
}

.hours-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.hours-head h2,
.hours-head p {
  margin: 0;
}

.hours-head p {
  margin-top: 6px;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.hours-grid {
  display: grid;
  gap: 12px;
}

.hours-row {
  display: grid;
  grid-template-columns: 110px 88px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(218, 223, 232, 0.9);
}

.hours-day {
  display: grid;
  gap: 4px;
}

.hours-day strong {
  font-size: 15px;
}

.hours-day span {
  color: var(--cc-color-text-secondary);
  font-size: 12px;
}

.hours-time {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.hours-time > span {
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .hours-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .head,
  .toolbar,
  .hours-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
