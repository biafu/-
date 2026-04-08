<template>
  <div class="page-shell apply-page">
    <div class="page-content">
      <section class="apply-wrap glass-panel page-enter">
        <div class="intro">
          <p class="badge">商户入驻申请</p>
          <h1>先提交基础资料，再由平台完成审核开通</h1>
          <p>
            当前页面用于补齐商户侧的入驻申请入口。提交后，管理员可以直接在后台审核，你通过后就能使用商家工作台。
          </p>

          <div class="steps">
            <article class="step-card cc-card">
              <strong>1. 填写资料</strong>
              <p>商户名称、联系人、联系电话、校区编码和营业执照号都需要一次性填完整。</p>
            </article>
            <article class="step-card cc-card">
              <strong>2. 平台审核</strong>
              <p>管理员会在平台端查看申请并给出通过或驳回意见。</p>
            </article>
            <article class="step-card cc-card">
              <strong>3. 开始经营</strong>
              <p>审核通过后即可使用商户账号登录，继续配置门店、商品、订单与营业时间。</p>
            </article>
          </div>
        </div>

        <div class="form-card cc-card">
          <div class="form-head">
            <div>
              <h2>提交申请</h2>
              <p>示例校区编码：`SOUTH`、`NORTH`、`EAST`。</p>
            </div>
            <el-button plain @click="router.push('/auth/login')">返回登录</el-button>
          </div>

          <el-form label-position="top" class="form-grid">
            <el-form-item label="商户名称">
              <el-input v-model="form.merchantName" maxlength="64" placeholder="例如：食堂一楼轻食铺" />
            </el-form-item>
            <el-form-item label="联系人">
              <el-input v-model="form.contactName" maxlength="32" placeholder="例如：李店长" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" maxlength="11" placeholder="例如：13800000001" />
            </el-form-item>
            <el-form-item label="校区编码">
              <el-input v-model="form.campusCode" maxlength="32" placeholder="例如：SOUTH" />
            </el-form-item>
            <el-form-item label="营业执照号" class="full-width">
              <el-input v-model="form.licenseNo" maxlength="64" placeholder="例如：LIC-2026-001" />
            </el-form-item>
          </el-form>

          <div class="actions">
            <el-button plain :disabled="submitting" @click="resetForm">重置</el-button>
            <el-button type="primary" :loading="submitting" @click="submit">提交申请</el-button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitMerchantApplication } from '@/api/merchant'
import {
  buildMerchantApplyPayload,
  createMerchantApplyFormState,
  getMerchantApplyValidationError,
} from '@/views/auth/merchant-apply'

const router = useRouter()
const submitting = ref(false)
const form = reactive(createMerchantApplyFormState())

const resetForm = () => {
  Object.assign(form, createMerchantApplyFormState())
}

const submit = async () => {
  const validationError = getMerchantApplyValidationError(form)
  if (validationError) {
    ElMessage.warning(validationError)
    return
  }

  submitting.value = true
  try {
    const applyId = await submitMerchantApplication(buildMerchantApplyPayload(form))
    ElMessage.success(`入驻申请已提交，申请编号 ${applyId}`)
    resetForm()
    await router.push('/auth/login')
  } catch (error) {
    const message = error instanceof Error ? error.message : '提交申请失败'
    ElMessage.error(message)
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.apply-page {
  display: grid;
  align-items: center;
}

.apply-wrap {
  display: grid;
  gap: 28px;
  padding: 32px;
  grid-template-columns: 1.05fr 1fr;
}

.badge {
  display: inline-flex;
  align-items: center;
  height: 30px;
  margin: 0 0 12px;
  padding: 0 12px;
  border-radius: 999px;
  color: #7f5d15;
  background: #fff1cc;
  font-size: 12px;
  font-weight: 700;
}

.intro h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.intro > p:last-of-type {
  margin: 16px 0 0;
  max-width: 460px;
  color: var(--cc-color-text-secondary);
  font-size: 16px;
}

.steps {
  margin-top: 20px;
  display: grid;
  gap: 12px;
}

.step-card {
  padding: 18px;
}

.step-card strong {
  display: block;
  font-size: 16px;
}

.step-card p {
  margin: 8px 0 0;
  color: var(--cc-color-text-secondary);
  line-height: 1.7;
}

.form-card {
  padding: 24px;
}

.form-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.form-head h2,
.form-head p {
  margin: 0;
}

.form-head p {
  margin-top: 6px;
  color: var(--cc-color-text-secondary);
}

.form-grid {
  margin-top: 18px;
  display: grid;
  gap: 4px 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.full-width {
  grid-column: 1 / -1;
}

.actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 960px) {
  .apply-wrap,
  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .apply-wrap {
    padding: 24px;
  }

  .form-head {
    flex-direction: column;
  }
}
</style>
