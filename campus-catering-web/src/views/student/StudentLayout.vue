<template>
  <div class="page-shell">
    <div class="page-content">
      <header class="header glass-panel">
        <div class="logo">{{ STUDENT_LAYOUT_BRAND }}</div>
        <nav class="nav">
          <RouterLink to="/student/home" class="tab">首页</RouterLink>
          <RouterLink :to="studentLinks.profile" class="tab">我的</RouterLink>
          <RouterLink to="/student/cart" class="tab">
            购物车
            <span v-if="checkoutStore.totalCount > 0" class="badge">
              {{ checkoutStore.totalCount > 99 ? '99+' : checkoutStore.totalCount }}
            </span>
          </RouterLink>
          <RouterLink to="/student/orders" class="tab">订单</RouterLink>
          <RouterLink to="/student/messages" class="tab">
            消息
            <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </RouterLink>
          <RouterLink to="/student/addresses" class="tab">地址</RouterLink>
          <button class="tab btn-logout" @click="logout">退出</button>
        </nav>
      </header>
      <main class="main page-enter">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchCartList, fetchStudentUnreadCount } from '@/api/student'
import { useAuthStore } from '@/stores/auth'
import { useStudentCheckoutStore } from '@/stores/student-checkout'
import { STUDENT_MESSAGE_EVENT } from '@/utils/student-message-events'
import { STUDENT_LAYOUT_BRAND } from '@/views/student/student-layout'
import { buildStudentProfileLinks } from '@/views/student/student-profile'

const router = useRouter()
const authStore = useAuthStore()
const checkoutStore = useStudentCheckoutStore()
const studentLinks = buildStudentProfileLinks()
const unreadCount = ref(0)
let unreadTimer: ReturnType<typeof setInterval> | undefined
let hasPrivateListeners = false

const hasStudentSession = () => Boolean(authStore.token && authStore.role === 'STUDENT')

const clearPrivateState = () => {
  unreadCount.value = 0
  checkoutStore.setCartItems([])
}

const logout = async () => {
  authStore.logout()
  clearPrivateState()
  await router.replace('/auth/login')
}

const loadUnreadCount = async () => {
  if (!hasStudentSession()) {
    unreadCount.value = 0
    return
  }

  try {
    unreadCount.value = Number(await fetchStudentUnreadCount())
  } catch {
    unreadCount.value = 0
  }
}

const loadCartSummary = async () => {
  if (!hasStudentSession()) {
    checkoutStore.setCartItems([])
    return
  }

  try {
    checkoutStore.setCartItems(await fetchCartList())
  } catch {
    checkoutStore.setCartItems([])
  }
}

const onMessageChanged = () => {
  if (!hasStudentSession()) {
    unreadCount.value = 0
    return
  }
  void loadUnreadCount()
}

onMounted(() => {
  if (!hasStudentSession()) {
    clearPrivateState()
    return
  }

  void loadUnreadCount()
  void loadCartSummary()
  window.addEventListener(STUDENT_MESSAGE_EVENT, onMessageChanged)
  hasPrivateListeners = true
  unreadTimer = setInterval(() => {
    void loadUnreadCount()
  }, 30_000)
})

onBeforeUnmount(() => {
  if (hasPrivateListeners) {
    window.removeEventListener(STUDENT_MESSAGE_EVENT, onMessageChanged)
    hasPrivateListeners = false
  }
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = undefined
  }
})
</script>

<style scoped>
.header {
  position: sticky;
  top: 16px;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 18px;
  margin-bottom: 18px;
}

.logo {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.01em;
}

.nav {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.tab {
  height: 34px;
  border: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
  border-radius: 9px;
  color: var(--cc-color-text-secondary);
  background: transparent;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab.router-link-active {
  background: #fff4d6;
  color: #8b6513;
  font-weight: 600;
}

.tab:hover {
  color: #8b6513;
  background: #fff8e9;
}

.badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: #d92d20;
  color: white;
  font-size: 11px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.main {
  display: grid;
  gap: 18px;
}

@media (max-width: 768px) {
  .header {
    padding: 12px;
    top: 10px;
    align-items: flex-start;
    flex-direction: column;
  }

  .logo {
    font-size: 16px;
  }

  .nav {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
