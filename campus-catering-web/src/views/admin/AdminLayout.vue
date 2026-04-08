<template>
  <div class="page-shell">
    <div class="page-content wrap">
      <aside class="side glass-panel">
        <div class="title-block">
          <p class="eyebrow">Campus Catering</p>
          <h2>平台管理台</h2>
        </div>
        <nav class="menu">
          <RouterLink to="/admin/dashboard" class="menu-item">平台总览</RouterLink>
          <RouterLink to="/admin/merchants" class="menu-item">商户审核</RouterLink>
          <RouterLink to="/admin/operations" class="menu-item">运营动作</RouterLink>
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
  grid-template-columns: 270px 1fr;
  gap: 16px;
}

.side {
  position: sticky;
  top: 16px;
  height: fit-content;
  padding: 18px;
}

.title-block {
  display: grid;
  gap: 6px;
  margin-bottom: 16px;
}

.eyebrow {
  margin: 0;
  color: var(--cc-color-text-tertiary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.title-block h2 {
  margin: 0;
  font-size: 20px;
}

.menu {
  display: grid;
  gap: 8px;
}

.menu-item {
  border: none;
  width: 100%;
  border-radius: 12px;
  padding: 11px 12px;
  text-align: left;
  color: var(--cc-color-text-secondary);
  background: transparent;
  transition: all 0.2s ease;
}

.menu-item.router-link-active {
  background: #eef4ff;
  color: #2256c7;
  font-weight: 600;
}

.menu-item:hover {
  background: #f7f9ff;
  color: #2256c7;
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
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
