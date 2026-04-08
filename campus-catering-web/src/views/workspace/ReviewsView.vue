<template>
  <section class="head glass-panel">
    <div>
      <h1 class="cc-section-title">评价中心</h1>
    </div>
    <el-button round :loading="loading" @click="loadReviews">刷新评价</el-button>
  </section>

  <section class="summary-grid">
    <article class="cc-card summary-card">
      <span>评价总数</span>
      <strong>{{ reviewSummary.totalCount }}</strong>
      <p>商家后台累计收到的学生评价。</p>
    </article>
    <article class="cc-card summary-card accent">
      <span>平均评分</span>
      <strong>{{ reviewSummary.averageRating.toFixed(1) }}</strong>
      <p>按所有评价平均后的当前口碑分数。</p>
    </article>
    <article class="cc-card summary-card">
      <span>好评率</span>
      <strong>{{ reviewSummary.positiveRate }}%</strong>
      <p>评分大于等于 4 分的评价占比。</p>
    </article>
  </section>

  <section class="grid">
    <article class="cc-card panel">
      <div class="panel-head">
        <h2>星级分布</h2>
        <p>快速判断问题集中在哪个评分区间。</p>
      </div>

      <div v-if="loading" class="panel-loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="reviewSummary.totalCount === 0" class="empty-wrap">
        <el-empty description="当前还没有学生评价" />
      </div>
      <ul v-else class="bar-list">
        <li v-for="bucket in reviewBuckets" :key="bucket.rating">
          <div class="bar-meta">
            <span>{{ bucket.rating }} 星</span>
            <strong>{{ bucket.count }} 条</strong>
          </div>
          <div class="bar-track">
            <span class="bar-fill" :style="{ width: bucketWidth(bucket.count) }" />
          </div>
        </li>
      </ul>
    </article>

    <article class="cc-card panel wide">
      <div class="panel-head">
        <h2>最新评价</h2>
        <p>按时间倒序展示，匿名评价也会在这里保留真实星级。</p>
      </div>

      <div v-if="loading" class="panel-loading">
        <el-skeleton :rows="6" animated />
      </div>
      <div v-else-if="reviewFeed.length === 0" class="empty-wrap">
        <el-empty description="还没有可展示的评价内容" />
      </div>
      <div v-else class="review-list">
        <article v-for="review in reviewFeed" :key="review.orderId" class="review-card">
          <div class="review-head">
            <div>
              <div class="review-meta">
                <strong>{{ review.reviewerName }}</strong>
                <span>订单号 {{ review.orderNo }}</span>
                <span>{{ review.storeName }}</span>
              </div>
              <el-rate :model-value="review.rating" disabled show-score text-color="#ff8f1f" />
            </div>
            <small>{{ formatMerchantReviewTime(review.createdAt) }}</small>
          </div>
          <p>{{ review.displayContent }}</p>
        </article>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchMerchantReviews } from '@/api/merchant'
import {
  buildMerchantReviewBuckets,
  buildMerchantReviewFeed,
  buildMerchantReviewSummary,
  formatMerchantReviewTime,
} from '@/views/workspace/merchant-reviews'

const loading = ref(false)
const reviews = ref(awaitPromiseSafe())

function awaitPromiseSafe() {
  return [] as Awaited<ReturnType<typeof fetchMerchantReviews>>
}

const reviewSummary = computed(() => buildMerchantReviewSummary(reviews.value))
const reviewBuckets = computed(() => buildMerchantReviewBuckets(reviews.value))
const reviewFeed = computed(() => buildMerchantReviewFeed(reviews.value))

const bucketWidth = (count: number) => {
  const maxCount = Math.max(...reviewBuckets.value.map((item) => item.count), 1)
  return `${Math.max(8, Math.round((count / maxCount) * 100))}%`
}

const loadReviews = async () => {
  loading.value = true
  try {
    reviews.value = await fetchMerchantReviews()
  } catch (error) {
    const message = error instanceof Error ? error.message : '评价加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

void loadReviews()
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.summary-grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.summary-card {
  padding: 18px;
}

.summary-card span {
  display: block;
  color: var(--cc-color-text-tertiary);
  font-size: 12px;
}

.summary-card strong {
  display: block;
  margin-top: 8px;
  font-size: 28px;
}

.summary-card p {
  margin: 10px 0 0;
  color: var(--cc-color-text-secondary);
}

.summary-card.accent {
  background:
    radial-gradient(circle at right top, rgba(255, 210, 118, 0.32), transparent 34%),
    linear-gradient(180deg, #fffef7, #fff8ea);
}

.grid {
  display: grid;
  gap: 16px;
  grid-template-columns: 320px 1fr;
}

.panel {
  display: grid;
  gap: 16px;
  padding: 20px;
}

.wide {
  min-width: 0;
}

.panel-head h2,
.panel-head p {
  margin: 0;
}

.panel-head p {
  margin-top: 6px;
  color: var(--cc-color-text-secondary);
}

.bar-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.bar-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.bar-track {
  height: 10px;
  border-radius: 999px;
  background: #f2f5fb;
  overflow: hidden;
}

.bar-fill {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #ffb347, #ff8f1f);
}

.review-list {
  display: grid;
  gap: 12px;
}

.review-card {
  padding: 16px;
  border: 1px solid var(--cc-color-border);
  border-radius: 16px;
  background: #fff;
}

.review-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.review-meta {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 8px;
  color: var(--cc-color-text-secondary);
  font-size: 13px;
}

.review-meta strong {
  color: var(--cc-color-text-primary);
}

.review-card p {
  margin: 10px 0 0;
  color: var(--cc-color-text-secondary);
  line-height: 1.7;
}

.empty-wrap,
.panel-loading {
  padding: 6px 0;
}

@media (max-width: 960px) {
  .summary-grid,
  .grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .head,
  .review-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
