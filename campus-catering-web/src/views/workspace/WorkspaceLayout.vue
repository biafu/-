<template>
  <div class="page-shell">
    <div class="page-content wrap">
      <aside class="side glass-panel">
        <h2>商家工作台</h2>
        <nav class="menu">
          <RouterLink to="/workspace/dashboard" class="menu-item">经营总览</RouterLink>
          <RouterLink to="/workspace/products" class="menu-item">商品中心</RouterLink>
          <RouterLink to="/workspace/orders" class="menu-item">订单管理</RouterLink>
          <RouterLink to="/workspace/reviews" class="menu-item">评价中心</RouterLink>
          <RouterLink to="/workspace/store" class="menu-item">门店设置</RouterLink>
          <button class="menu-item menu-button" @click="logout">退出登录</button>
        </nav>
      </aside>
      <main class="content page-enter">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const logout = async () => {
  authStore.logout()
  await router.replace('/auth/login')
}
</script>

<style scoped>
.wrap {
  display: grid;
  grid-template-columns: 250px 1fr;
  gap: 16px;
}

.side {
  padding: 16px;
  height: fit-content;
  position: sticky;
  top: 16px;
}

.side h2 {
  margin: 6px 0 14px;
  font-size: 18px;
}

.menu {
  display: grid;
  gap: 8px;
}

.menu-item {
  border: none;
  width: 100%;
  border-radius: 10px;
  padding: 10px 12px;
  text-align: left;
  color: var(--cc-color-text-secondary);
  background: transparent;
  transition: all 0.2s ease;
}

.menu-item.router-link-active {
  background: var(--cc-color-primary-soft);
  color: var(--cc-color-primary);
  font-weight: 600;
}

.menu-item:hover {
  background: #f7f9ff;
  color: var(--cc-color-primary);
}

.menu-button {
  cursor: pointer;
  font: inherit;
}

@media (max-width: 960px) {
  .wrap {
    grid-template-columns: 1fr;
  }

  .side {
    position: static;
  }

  .menu {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .menu {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
