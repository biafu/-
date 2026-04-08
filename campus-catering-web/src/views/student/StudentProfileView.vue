<template>
  <section class="header glass-panel">
    <div>
      <p class="eyebrow">Student Hub</p>
      <h1 class="cc-section-title">我的</h1>
    </div>
  </section>

  <section class="summary-grid">
    <article v-for="card in summaryCards" :key="card.key" class="cc-card summary-card">
      <span class="summary-label">{{ card.label }}</span>
      <strong>{{ card.value }}</strong>
    </article>
  </section>

  <section class="quick-grid">
    <RouterLink v-for="entry in quickLinks" :key="entry.key" :to="entry.to" class="cc-card quick-card">
      <strong>{{ entry.title }}</strong>
    </RouterLink>
  </section>

  <section class="cc-card activity-card">
    <div class="section-head">
      <div>
        <h2>最近动态</h2>
      </div>
    </div>
    <div v-if="activities.length > 0" class="activity-list">
      <RouterLink v-for="item in activities" :key="item.key" :to="item.to" class="activity-item">
        <strong>{{ item.title }}</strong>
        <span>{{ formatTime(item.meta) }}</span>
      </RouterLink>
    </div>
    <el-empty v-else description="还没有最近动态" />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
  fetchAddressList,
  fetchMyCoupons,
  fetchStudentMessages,
  fetchStudentOrders,
  type MyCouponResponse,
  type OrderDetailResponse,
  type StudentMessageResponse,
  type UserAddressResponse,
} from '@/api/student'
import {
  buildDefaultAddressText,
  buildProfileQuickLinks,
  buildStudentProfileActivity,
  buildStudentProfileSummary,
  countAvailableCoupons,
} from '@/views/student/student-profile'

const authStore = useAuthStore()
const orders = ref<OrderDetailResponse[]>([])
const messages = ref<StudentMessageResponse[]>([])
const addresses = ref<UserAddressResponse[]>([])
const coupons = ref<MyCouponResponse[]>([])

const unpaidOrderCount = computed(
  () => orders.value.filter((order) => order.orderStatus === 'PENDING_PAYMENT').length,
)

const unreadCount = computed(() => messages.value.filter((message) => Number(message.isRead) === 0).length)

const summaryCards = computed(() =>
  buildStudentProfileSummary({
    unpaidOrderCount: unpaidOrderCount.value,
    unreadCount: unreadCount.value,
    defaultAddressText: buildDefaultAddressText(addresses.value),
    availableCouponCount: countAvailableCoupons(coupons.value),
  }),
)

const quickLinks = buildProfileQuickLinks()

const activities = computed(() =>
  buildStudentProfileActivity({
    orders: orders.value,
    messages: messages.value,
  }),
)

const loadData = async () => {
  try {
    const [orderData, messageData, addressData, couponData] = await Promise.all([
      fetchStudentOrders(),
      fetchStudentMessages({ limit: 3 }),
      fetchAddressList(),
      fetchMyCoupons(),
    ])
    orders.value = orderData
    messages.value = messageData
    addresses.value = addressData
    coupons.value = couponData
  } catch (error) {
    const message = error instanceof Error ? error.message : '个人中心加载失败'
    ElMessage.error(message)
  }
}

const formatTime = (raw: string) => String(raw || '').replace('T', ' ').slice(0, 16)

onMounted(loadData)
</script>

<style scoped>
.header {
  padding: 20px;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #9a7a36;
}

.summary-grid,
.quick-grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.summary-card,
.quick-card,
.activity-card {
  padding: 18px;
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

.quick-card {
  color: inherit;
  text-decoration: none;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.quick-card:hover {
  transform: translateY(-2px);
}

.quick-card strong {
  display: block;
  margin-bottom: 8px;
}

.section-head {
  margin-bottom: 14px;
}

.section-head h2 {
  margin: 0;
}

.activity-list {
  display: grid;
  gap: 12px;
}

.activity-item {
  display: grid;
  gap: 6px;
  color: inherit;
  text-decoration: none;
  padding: 14px 16px;
  border-radius: 16px;
  background: #fffaf2;
}

.activity-item span {
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

@media (max-width: 960px) {
  .summary-grid,
  .quick-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .summary-grid,
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>
