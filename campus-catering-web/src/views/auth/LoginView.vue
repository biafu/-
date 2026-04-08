<template>
  <div class="page-shell login-page">
    <div class="page-content">
      <section class="login-wrap glass-panel page-enter">
        <div class="brand">
          <p class="badge">校园外卖系统</p>
          <h1>智能化生活平台</h1>
          <p></p>

          <div class="brand-actions">
            <el-button type="warning" plain round @click="router.push('/auth/merchant-apply')">
              商户申请入驻
            </el-button>
          </div>
        </div>

        <div class="login-card cc-card">
          <h2 style="text-align: center;">登录</h2>

          

          <el-radio-group v-model="role" class="role-group">
            <el-radio-button label="STUDENT">学生</el-radio-button>
            <el-radio-button label="MERCHANT">商家</el-radio-button>
            <el-radio-button label="ADMIN">管理员</el-radio-button>
            <el-radio-button label="DELIVERY">配送员</el-radio-button>
          </el-radio-group>

          <el-form label-position="top" class="form">
            <el-form-item label="账号">
              <el-input v-model="form.username" size="large" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="form.password" type="password" show-password size="large" />
            </el-form-item>
          </el-form>

          <button class="cc-btn-primary btn" :disabled="loading" @click="login">
            {{ loading ? '登录中...' : '登录并进入系统' }}
          </button>

          <div class="secondary-actions">
            <el-button text @click="router.push('/auth/merchant-apply')">没有商家账号？先提交入驻申请</el-button>
          </div>

          <p class="hint">
            默认学生：`13800000001 / 123456`
            默认商家：`merchant01 / 123456`
            默认管理员：`admin / 123456`
            默认配送员：`rider01 / 123456`
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { loginByRole, type UserRole } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const role = ref<UserRole>('STUDENT')
const loading = ref(false)
const form = reactive({
  username: '13800000001',
  password: '123456',
})

watch(
  role,
  (next) => {
    if (next === 'STUDENT') {
      form.username = '13800000001'
      form.password = '123456'
      return
    }
    if (next === 'MERCHANT') {
      form.username = 'merchant01'
      form.password = '123456'
      return
    }
    if (next === 'ADMIN') {
      form.username = 'admin'
      form.password = '123456'
      return
    }
    form.username = 'rider01'
    form.password = '123456'
  },
  { immediate: true },
)

const login = async () => {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const result = await loginByRole(role.value, {
      username: form.username.trim(),
      password: form.password.trim(),
    })
    authStore.setSession(result)

    if (result.role === 'MERCHANT') {
      await router.replace('/workspace/dashboard')
      return
    }
    if (result.role === 'STUDENT') {
      await router.replace('/student/home')
      return
    }
    if (result.role === 'DELIVERY') {
      await router.replace('/delivery')
      return
    }

    ElMessage.success('管理员登录成功')
    await router.replace('/admin/dashboard')
  } catch (error) {
    const message = error instanceof Error ? error.message : '登录失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  align-items: center;
}

.login-wrap {
  display: grid;
  gap: 28px;
  padding: 36px;
  grid-template-columns: 1.1fr 1fr;
}

.brand {
  padding: 12px 8px;
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

.brand h1 {
  margin: 0;
  font-size: clamp(30px, 4vw, 42px);
  line-height: 1.15;
  letter-spacing: -0.02em;
}

.brand p {
  margin: 16px 0 0;
  max-width: 420px;
  color: var(--cc-color-text-secondary);
  font-size: 16px;
}

.brand-actions {
  margin-top: 20px;
}

.login-card {
  padding: 24px;
}

.login-card h2 {
  margin: 0;
  font-size: 26px;
}

.tip {
  margin: 8px 0 12px;
  color: var(--cc-color-text-secondary);
}

.role-group {
  margin: 8px 0 14px;
}

.btn {
  width: 100%;
  height: 44px;
}

.secondary-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.hint {
  margin: 12px 0 0;
  color: var(--cc-color-text-tertiary);
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 940px) {
  .login-wrap {
    grid-template-columns: 1fr;
    padding: 24px;
  }

  .brand h1 {
    font-size: 30px;
  }
}
</style>
