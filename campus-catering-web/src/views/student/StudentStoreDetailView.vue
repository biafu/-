<template>
  <section class="glass-panel head">
    <div>
      <h1 class="cc-section-title">{{ storeDetail?.storeName ?? '门店详情' }}</h1>
    </div>
    <div class="head-actions">
      <el-button type="warning" plain @click="goToSeckill">秒杀专区</el-button>
      <el-button plain @click="goToCart">去购物车</el-button>
      <el-button plain @click="router.back()">返回</el-button>
    </div>
  </section>

  <section class="list">
    <el-skeleton v-if="loading" :rows="5" animated />

    <template v-else>
      <RevealOnScroll v-for="item in items" :key="item.skuId">
        <article class="cc-card product hover-lift">
          <div class="media">
            <el-image :src="currentImage(item)" fit="cover" class="thumb" @error="markImageError(item.skuId)">
              <template #error>
                <div class="thumb-fallback">
                  <span>{{ item.productName.slice(0, 2) }}</span>
                </div>
              </template>
            </el-image>
          </div>
          <div class="left">
            <h3>
              {{ item.productName }}
              <el-tag size="small" effect="plain" :type="item.productType === 2 ? 'warning' : 'success'">
                {{ item.productType === 2 ? '套餐' : '单品' }}
              </el-tag>
            </h3>
            <p>{{ item.skuName }}</p>
            <p v-if="item.comboSummary" class="combo-summary">包含：{{ item.comboSummary }}</p>
            <div class="stats">
              <span>库存 {{ item.stock }}</span>
              <span>规格 {{ item.skuName }}</span>
            </div>
          </div>
          <div class="right">
            <strong>{{ toPrice(item.price) }}</strong>
            <el-button type="primary" round @click="handleAddToCart(item.skuId)">加入购物车</el-button>
          </div>
        </article>
      </RevealOnScroll>

      <el-empty v-if="items.length === 0" description="当前门店暂无在售商品" />
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import RevealOnScroll from '@/components/RevealOnScroll.vue'
import { addToCart, fetchCartList, fetchProductList, fetchStoreDetail, type StoreSimpleResponse } from '@/api/student'
import { loadSessionFromStorage } from '@/stores/auth'
import { useStudentCheckoutStore } from '@/stores/student-checkout'
import { buildStudentStoreProductRows, type StudentStoreProductRow } from '@/views/student/student-store-products'

const route = useRoute()
const router = useRouter()
const checkoutStore = useStudentCheckoutStore()
const storeDetail = ref<StoreSimpleResponse>()
const items = ref<StudentStoreProductRow[]>([])
const loading = ref(false)
const failedImageSkuIds = ref<number[]>([])

const storeId = computed(() => Number(route.params.id))

const toPrice = (value: number) => `${Number(value ?? 0).toFixed(2)}元`

const currentImage = (item: StudentStoreProductRow) =>
  failedImageSkuIds.value.includes(item.skuId) || !item.imageUrl ? item.fallbackImageUrl : item.imageUrl

const markImageError = (skuId: number) => {
  if (!failedImageSkuIds.value.includes(skuId)) {
    failedImageSkuIds.value = [...failedImageSkuIds.value, skuId]
  }
}

const loadPageData = async () => {
  if (!Number.isFinite(storeId.value) || storeId.value <= 0) {
    storeDetail.value = undefined
    items.value = []
    ElMessage.error('门店参数错误')
    return
  }

  loading.value = true
  try {
    const [storeResponse, products] = await Promise.all([fetchStoreDetail(storeId.value), fetchProductList(storeId.value)])

    storeDetail.value = storeResponse
    items.value = buildStudentStoreProductRows(products)
    failedImageSkuIds.value = []
  } catch (error) {
    storeDetail.value = undefined
    items.value = []
    const message = error instanceof Error ? error.message : '门店数据加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const goToCart = async () => {
  await router.push('/student/cart')
}

const goToSeckill = async () => {
  await router.push(`/student/store/${storeId.value}/seckill`)
}

const refreshCartBadge = async () => {
  try {
    checkoutStore.setCartItems(await fetchCartList())
  } catch {
    // 保持加购成功提示，不额外打断用户。
  }
}

const handleAddToCart = async (skuId: number) => {
  const session = loadSessionFromStorage()
  if (!session?.token || session.role !== 'STUDENT') {
    ElMessage.warning('请先使用学生账号登录后再加购')
    await router.push('/auth/login')
    return
  }

  try {
    await addToCart({
      storeId: storeId.value,
      skuId,
      quantity: 1,
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '加入购物车失败'
    ElMessage.error(message)
    return
  }

  ElMessage.success('已加入购物车')
  void refreshCartBadge()
}

watch(storeId, (nextId, previousId) => {
  if (nextId !== previousId) {
    void loadPageData()
  }
})

onMounted(loadPageData)
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.head-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.list {
  margin-top: 16px;
  display: grid;
  gap: 14px;
}

.product {
  display: grid;
  grid-template-columns: 104px 1fr auto;
  align-items: center;
  gap: 16px;
  padding: 16px;
}

.media {
  display: flex;
  align-items: center;
}

.thumb,
.thumb-fallback {
  width: 104px;
  height: 104px;
  border-radius: 18px;
  overflow: hidden;
}

.thumb-fallback {
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #ffe4b5, #ffb36b);
  color: #7a3b00;
  font-size: 24px;
  font-weight: 700;
}

.left h3 {
  margin: 0;
  font-size: 18px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.left p {
  margin: 8px 0;
  color: var(--cc-color-text-secondary);
}

.combo-summary {
  margin-top: -2px;
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

.stats {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 12px;
  color: var(--cc-color-text-tertiary);
}

.right {
  display: grid;
  justify-items: end;
  gap: 10px;
}

.right strong {
  font-size: 22px;
  color: var(--cc-color-primary);
}

@media (max-width: 768px) {
  .head {
    align-items: flex-start;
    flex-direction: column;
  }

  .product {
    grid-template-columns: 1fr;
    justify-items: start;
  }

  .right {
    justify-items: start;
  }
}
</style>
