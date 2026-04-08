<template>
  <section class="header glass-panel">
    <div>
      <p class="eyebrow">Coupon Center</p>
      <h1 class="cc-section-title">优惠券中心</h1>
    </div>
    <el-button round @click="loadData">刷新</el-button>
  </section>

  <section class="summary-grid">
    <article v-for="section in sections" :key="section.key" class="cc-card summary-card">
      <span class="summary-label">{{ section.label }}</span>
      <strong>{{ section.count }}</strong>
    </article>
  </section>

  <section class="coupon-grid">
    <article class="cc-card coupon-panel">
      <div class="section-head">
        <h2>可领取优惠券</h2>
        <p>领取后会立刻出现在“我的优惠券”中。</p>
      </div>
      <div v-if="centerCoupons.length > 0" class="coupon-list">
        <div v-for="coupon in centerCoupons" :key="coupon.couponId" class="coupon-card claimable">
          <strong>{{ coupon.couponName }}</strong>
          <p>满 {{ toPrice(coupon.thresholdAmount) }} 减 {{ toPrice(coupon.discountAmount) }}</p>
          <span>有效期至 {{ formatTime(coupon.endTime) }}</span>
          <el-button type="primary" plain size="small" @click="claim(coupon.couponId)">立即领取</el-button>
        </div>
      </div>
      <el-empty v-else description="当前没有可领取优惠券" />
    </article>

    <article class="cc-card coupon-panel">
      <div class="section-head">
        <h2>我的优惠券</h2>
        <p>可以在结算页自动选择适用优惠券。</p>
      </div>
      <div v-if="myCoupons.length > 0" class="coupon-list">
        <div v-for="coupon in myCoupons" :key="coupon.userCouponId" class="coupon-card">
          <strong>{{ coupon.couponName }}</strong>
          <p>满 {{ toPrice(coupon.thresholdAmount) }} 减 {{ toPrice(coupon.discountAmount) }}</p>
          <span>{{ couponStatus(coupon.status) }} · {{ formatTime(coupon.endTime) }} 到期</span>
        </div>
      </div>
      <el-empty v-else description="还没有领取优惠券" />
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  claimCoupon,
  fetchCouponCenter,
  fetchMyCoupons,
  type CouponCenterResponse,
  type MyCouponResponse,
} from '@/api/student'
import { buildCouponSections } from '@/views/student/student-profile'

const centerCoupons = ref<CouponCenterResponse[]>([])
const myCoupons = ref<MyCouponResponse[]>([])

const sections = computed(() =>
  buildCouponSections({
    claimableCount: centerCoupons.value.length,
    ownedCount: myCoupons.value.length,
  }),
)

const loadData = async () => {
  try {
    const [centerData, myData] = await Promise.all([fetchCouponCenter(), fetchMyCoupons()])
    centerCoupons.value = centerData
    myCoupons.value = myData
  } catch (error) {
    const message = error instanceof Error ? error.message : '优惠券加载失败'
    ElMessage.error(message)
  }
}

const claim = async (couponId: number) => {
  try {
    await claimCoupon(couponId)
    ElMessage.success('优惠券已领取')
    await loadData()
  } catch (error) {
    const message = error instanceof Error ? error.message : '领取失败'
    ElMessage.error(message)
  }
}

const couponStatus = (status: number) => {
  if (Number(status) === 1) return '可用'
  if (Number(status) === 2) return '已使用'
  return '已失效'
}

const formatTime = (raw: string) => String(raw || '').replace('T', ' ').slice(0, 16)
const toPrice = (value: number) => Number(value ?? 0).toFixed(2)

onMounted(loadData)
</script>

<style scoped>
.header,
.summary-card,
.coupon-panel {
  padding: 18px;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #9a7a36;
}

.summary-grid,
.coupon-grid {
  display: grid;
  gap: 14px;
}

.summary-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.coupon-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
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

.section-head {
  margin-bottom: 14px;
}

.section-head h2 {
  margin: 0;
}

.section-head p {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
}

.coupon-list {
  display: grid;
  gap: 12px;
}

.coupon-card {
  display: grid;
  gap: 8px;
  padding: 16px;
  border-radius: 16px;
  background: #fffaf2;
}

.coupon-card.claimable {
  border: 1px solid rgba(255, 185, 64, 0.45);
}

.coupon-card p,
.coupon-card span {
  margin: 0;
  color: var(--cc-color-text-secondary);
}

@media (max-width: 768px) {
  .summary-grid,
  .coupon-grid {
    grid-template-columns: 1fr;
  }
}
</style>
