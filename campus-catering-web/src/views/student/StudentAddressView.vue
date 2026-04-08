<template>
  <section class="header glass-panel">
    <div>
      <p class="eyebrow">地址管理</p>
      <h1 class="cc-section-title">收货地址</h1>
    </div>
    <div class="header-actions">
      <el-button v-if="routeIntent.returnToCheckout" plain @click="router.push('/student/checkout')">返回结算</el-button>
      <el-button type="primary" round @click="openCreate">新增地址</el-button>
    </div>
  </section>

  <section class="summary-grid">
    <article class="cc-card summary-card">
      <span class="summary-label">地址数量</span>
      <strong>{{ addresses.length }}</strong>
      <p>地址信息越完整，确认订单时切换和提交就越顺畅。</p>
    </article>
    <article class="cc-card summary-card accent">
      <span class="summary-label">默认地址</span>
      <strong>{{ defaultAddress?.contactName || '暂未设置' }}</strong>
      <p>{{ defaultAddress?.fullAddress || '建议至少维护一个默认收货地址。' }}</p>
    </article>
  </section>

  <section class="list">
    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <article
        v-for="item in addresses"
        :key="item.id"
        class="cc-card address-card"
        :class="{ featured: item.isDefault === 1 }"
      >
        <div class="top">
          <div>
            <h3>
              {{ item.contactName }}
              <span>{{ item.contactPhone }}</span>
              <el-tag v-if="item.isDefault === 1" type="success" effect="dark" round>默认地址</el-tag>
            </h3>
            <p class="updated">最近更新：{{ formatStudentAddressTime(item.updatedAt) }}</p>
          </div>
          <div class="actions">
            <el-button v-if="item.isDefault !== 1" size="small" @click="setDefault(item.id)">设为默认</el-button>
            <el-button size="small" @click="openEdit(item)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(item.id)">删除</el-button>
          </div>
        </div>

        <div class="address-body">
          <div class="address-line">
            <span class="label">校区</span>
            <strong>{{ item.campusName }}</strong>
          </div>
          <div class="address-line">
            <span class="label">楼栋 / 房间号</span>
            <strong>{{ item.buildingName }} {{ item.roomNo }}</strong>
          </div>
          <div class="address-line block">
            <span class="label">完整地址</span>
            <p>{{ item.fullAddress }}</p>
          </div>
        </div>
      </article>
      <el-empty v-if="addresses.length === 0" description="还没有保存的收货地址" />
    </template>
  </section>

  <el-drawer
    v-model="dialogVisible"
    :title="editingId ? '编辑地址' : '新增地址'"
    size="640px"
    class="address-editor-drawer"
    destroy-on-close
  >
    <div class="address-drawer-body">
      <el-form label-position="top" class="address-form">
        <section class="address-form-card">
          <h3>联系人信息</h3>
          <el-form-item label="联系人">
            <el-input v-model="form.contactName" placeholder="例如：张三" />
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="form.contactPhone" placeholder="例如：13800000000" />
          </el-form-item>
        </section>

        <section class="address-form-card">
          <h3>地址信息</h3>
          <el-form-item label="校区">
            <el-input v-model="form.campusName" placeholder="例如：南校区" />
          </el-form-item>
          <el-form-item label="楼栋">
            <el-input v-model="form.buildingName" placeholder="例如：5号宿舍" />
          </el-form-item>
          <el-form-item label="房间号">
            <el-input v-model="form.roomNo" placeholder="例如：302" />
          </el-form-item>
          <el-form-item label="详细说明">
            <el-input v-model="form.detailAddress" placeholder="例如：靠近电梯口" />
          </el-form-item>
        </section>

        <section class="address-form-card">
          <h3>保存设置</h3>
          <el-form-item>
            <el-checkbox v-model="isDefaultChecked">设为默认地址</el-checkbox>
          </el-form-item>
        </section>

        <section class="address-preview-card">
          <p class="preview-label">地址预览</p>
          <div class="preview-card">
            <h3>{{ form.contactName || '联系人' }}</h3>
            <span>{{ form.contactPhone || '联系电话' }}</span>
            <p>{{ previewAddress }}</p>
            <el-tag :type="isDefaultChecked ? 'success' : 'info'" effect="plain" round>
              {{ isDefaultChecked ? '下单时优先使用' : '普通地址' }}
            </el-tag>
          </div>
        </section>
      </el-form>
    </div>

    <template #footer>
      <div class="address-drawer-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteAddress,
  fetchAddressList,
  saveAddress,
  setDefaultAddress,
  type UserAddressResponse,
} from '@/api/student'
import {
  buildStudentAddressPreview,
  buildStudentAddressSavePayload,
  formatStudentAddressTime,
  parseStudentAddressRouteQuery,
  validateStudentAddressForm,
} from '@/views/student/student-address'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number>()
const addresses = ref<UserAddressResponse[]>([])
const isDefaultChecked = ref(true)

const form = reactive({
  campusName: '',
  buildingName: '',
  roomNo: '',
  detailAddress: '',
  contactName: '',
  contactPhone: '',
})

const routeIntent = computed(() => parseStudentAddressRouteQuery(route.query as Record<string, unknown>))
const defaultAddress = computed(() => addresses.value.find((item) => item.isDefault === 1))
const previewAddress = computed(() => buildStudentAddressPreview(form))

const resetForm = () => {
  form.campusName = ''
  form.buildingName = ''
  form.roomNo = ''
  form.detailAddress = ''
  form.contactName = ''
  form.contactPhone = ''
  isDefaultChecked.value = true
}

const loadAddresses = async () => {
  loading.value = true
  try {
    addresses.value = await fetchAddressList()
  } catch (error) {
    const message = error instanceof Error ? error.message : '地址加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const returnToCheckoutIfNeeded = async () => {
  if (!routeIntent.value.returnToCheckout) {
    return false
  }
  await router.push('/student/checkout')
  return true
}

const openCreate = () => {
  editingId.value = undefined
  resetForm()
  dialogVisible.value = true
}

const openEdit = (item: UserAddressResponse) => {
  editingId.value = item.id
  form.campusName = item.campusName
  form.buildingName = item.buildingName
  form.roomNo = item.roomNo
  form.detailAddress = item.detailAddress || ''
  form.contactName = item.contactName
  form.contactPhone = item.contactPhone
  isDefaultChecked.value = item.isDefault === 1
  dialogVisible.value = true
}

const submit = async () => {
  const validationMessage = validateStudentAddressForm(form)
  if (validationMessage) {
    ElMessage.warning(validationMessage)
    return
  }

  saving.value = true
  try {
    await saveAddress(
      buildStudentAddressSavePayload({
        editingId: editingId.value,
        isDefaultChecked: isDefaultChecked.value,
        form,
      }),
    )
    await loadAddresses()
    dialogVisible.value = false
    ElMessage.success('地址已保存')
    if (await returnToCheckoutIfNeeded()) {
      return
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '地址保存失败'
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}

const setDefault = async (id: number) => {
  try {
    await setDefaultAddress(id)
    await loadAddresses()
    ElMessage.success('默认地址已更新')
    if (await returnToCheckoutIfNeeded()) {
      return
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(message)
  }
}

const remove = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个地址吗？', '删除确认', { type: 'warning' })
    await deleteAddress(id)
    await loadAddresses()
    ElMessage.success('地址已删除')
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
  await loadAddresses()
  if (routeIntent.value.openCreate && addresses.value.length === 0) {
    openCreate()
  }
})
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.header-actions {
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
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  padding: 18px;
}

.summary-card.accent {
  background:
    radial-gradient(circle at right top, rgba(255, 216, 122, 0.38), transparent 34%),
    linear-gradient(180deg, #fffef7, #fff8e7);
}

.summary-label {
  display: inline-block;
  margin-bottom: 8px;
  color: var(--cc-color-text-tertiary);
  font-size: 12px;
}

.summary-card strong {
  display: block;
  font-size: 24px;
}

.summary-card p {
  margin: 10px 0 0;
  color: var(--cc-color-text-secondary);
}

.list {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.address-card {
  padding: 18px;
}

.address-card.featured {
  border-color: rgba(17, 130, 81, 0.28);
  background:
    radial-gradient(circle at top right, rgba(167, 230, 198, 0.2), transparent 28%),
    #fff;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.top h3 {
  margin: 0;
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.top h3 span {
  font-weight: 500;
  color: var(--cc-color-text-secondary);
}

.updated {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.address-body {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.address-line {
  display: grid;
  gap: 4px;
}

.address-line.block {
  grid-column: 1 / -1;
}

.label,
.preview-label {
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

.address-line p {
  margin: 0;
  color: var(--cc-color-text-secondary);
}

.address-editor-drawer :deep(.el-drawer__body) {
  display: flex;
  flex-direction: column;
  padding: 0 20px 20px;
  overflow: hidden;
}

.address-drawer-body {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding-top: 4px;
}

.address-form {
  display: grid;
  gap: 14px;
}

.address-form-card,
.address-preview-card {
  border-radius: 18px;
  padding: 16px;
  background: #fff;
  border: 1px solid rgba(214, 220, 231, 0.9);
  box-shadow: 0 8px 24px rgba(18, 38, 63, 0.04);
}

.address-form-card h3 {
  margin: 0 0 14px;
  font-size: 16px;
}

.address-form-card :deep(.el-form-item) {
  margin-bottom: 14px;
}

.address-form-card :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.preview-card {
  margin-top: 10px;
  border-radius: 18px;
  padding: 18px;
  background:
    radial-gradient(circle at right top, rgba(255, 216, 122, 0.4), transparent 35%),
    linear-gradient(180deg, #fffef7, #fff8e9);
  border: 1px solid rgba(255, 208, 98, 0.45);
}

.preview-card h3,
.preview-card p {
  margin: 0;
}

.preview-card span {
  display: block;
  margin-top: 8px;
  color: var(--cc-color-text-secondary);
}

.preview-card p {
  margin: 14px 0 18px;
  color: var(--cc-color-text);
  line-height: 1.7;
}

.address-drawer-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid rgba(214, 220, 231, 0.9);
  background: #fff;
}

.address-editor-drawer :deep(.el-drawer__footer) {
  position: sticky;
  bottom: 0;
  z-index: 1;
  background: #fff;
}

@media (max-width: 768px) {
  .header,
  .top {
    flex-direction: column;
    align-items: flex-start;
  }

  .summary-grid,
  .address-body {
    grid-template-columns: 1fr;
  }

  .address-editor-drawer :deep(.el-drawer__body) {
    padding: 0 14px 14px;
  }
}
</style>
