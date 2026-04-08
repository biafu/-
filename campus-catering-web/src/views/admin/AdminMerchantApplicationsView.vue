<template>
  <section class="hero glass-panel">
    <div>
      <h1 class="cc-section-title">商户审核</h1>
    </div>
    <el-button type="primary" round :loading="loading" @click="loadApplications">刷新</el-button>
  </section>

  <section class="list">
    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <article v-for="item in applications" :key="item.id" class="cc-card application-card">
        <div class="card-head">
          <div>
            <h2>{{ item.merchantName }}</h2>
            <p>{{ item.contactName }} · {{ item.contactPhone }}</p>
          </div>
          <el-tag :type="statusMeta(item.status).type" effect="plain" round>
            {{ statusMeta(item.status).text }}
          </el-tag>
        </div>

        <dl class="info-grid">
          <div>
            <dt>校区编码</dt>
            <dd>{{ item.campusCode || '-' }}</dd>
          </div>
          <div>
            <dt>营业执照</dt>
            <dd>{{ item.licenseNo || '-' }}</dd>
          </div>
          <div>
            <dt>提交时间</dt>
            <dd>{{ formatTime(item.createdAt) }}</dd>
          </div>
          <div>
            <dt>审核备注</dt>
            <dd>{{ item.auditRemark || '暂无' }}</dd>
          </div>
        </dl>

        <div v-if="item.status === 0" class="actions">
          <el-button type="primary" @click="openAudit(item, true)">通过</el-button>
          <el-button type="danger" plain @click="openAudit(item, false)">驳回</el-button>
        </div>
      </article>
      <el-empty v-if="applications.length === 0" description="暂无商户申请" />
    </template>
  </section>

  <el-dialog v-model="dialogVisible" :title="auditApproved ? '通过申请' : '驳回申请'" width="520px">
    <div class="dialog-body">
      <p v-if="activeApplication">
        正在处理：<strong>{{ activeApplication.merchantName }}</strong>
      </p>
      <el-input
        v-model="auditRemark"
        type="textarea"
        :rows="4"
        maxlength="120"
        show-word-limit
        resize="none"
        :placeholder="auditApproved ? '可选：补充审核说明' : '请输入驳回原因，方便后续沟通'"
      />
    </div>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submitAudit">提交审核</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  auditMerchantApplication,
  fetchMerchantApplications,
  type MerchantApplicationResponse,
} from '@/api/admin'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const auditApproved = ref(true)
const auditRemark = ref('')
const applications = ref<MerchantApplicationResponse[]>([])
const activeApplication = ref<MerchantApplicationResponse | null>(null)

const formatTime = (value: string) => String(value || '').replace('T', ' ').slice(0, 16)

const statusMeta = (status: number) => {
  if (status === 1) {
    return { text: '已通过', type: 'success' as const }
  }
  if (status === 2) {
    return { text: '已驳回', type: 'danger' as const }
  }
  return { text: '待审核', type: 'warning' as const }
}

const loadApplications = async () => {
  loading.value = true
  try {
    applications.value = await fetchMerchantApplications()
  } catch (error) {
    const message = error instanceof Error ? error.message : '商户申请加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const openAudit = (item: MerchantApplicationResponse, approved: boolean) => {
  activeApplication.value = item
  auditApproved.value = approved
  auditRemark.value = approved ? '' : item.auditRemark || ''
  dialogVisible.value = true
}

const submitAudit = async () => {
  if (!activeApplication.value) {
    return
  }
  if (!auditApproved.value && !auditRemark.value.trim()) {
    ElMessage.warning('驳回时请填写审核说明')
    return
  }

  submitting.value = true
  try {
    await auditMerchantApplication({
      applyId: activeApplication.value.id,
      approved: auditApproved.value,
      auditRemark: auditRemark.value.trim() || undefined,
    })
    ElMessage.success('审核结果已提交')
    dialogVisible.value = false
    await loadApplications()
  } catch (error) {
    const message = error instanceof Error ? error.message : '审核提交失败'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}

void loadApplications()
</script>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
}

.list {
  display: grid;
  gap: 14px;
}

.application-card {
  display: grid;
  gap: 18px;
  padding: 20px;
}

.card-head,
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-head h2,
.card-head p,
.dialog-body p {
  margin: 0;
}

.card-head p {
  margin-top: 6px;
  color: var(--cc-color-text-secondary);
}

.info-grid {
  margin: 0;
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.info-grid dt {
  color: var(--cc-color-text-tertiary);
  font-size: 12px;
  margin-bottom: 6px;
}

.info-grid dd {
  margin: 0;
  color: var(--cc-color-text);
}

.dialog-body {
  display: grid;
  gap: 14px;
}

@media (max-width: 768px) {
  .hero,
  .card-head,
  .actions {
    align-items: flex-start;
    flex-direction: column;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
