<template>
  <section class="mt-top glass-panel">
    <div class="location-row">
      <div>
        <p class="campus">{{ campusLabel }}</p>
        <h1 class="title">校园外卖</h1>
      </div>
      <el-tag round effect="dark" class="tag-weather">{{ contextTag }}</el-tag>
    </div>
    <el-input v-model="keyword" class="search" placeholder="搜索商家、菜品、套餐" size="large" />
    <div class="benefits">
      <span>首单立减</span>
      <span>满减专区</span>
      <span>学生专享价</span>
      <span>极速送达</span>
    </div>
  </section>

  <section class="category cc-card">
    <button
      v-for="item in categories"
      :key="item"
      class="category-item"
      :class="{ active: selectedCategory === item }"
      type="button"
      @click="selectedCategory = item"
    >
      <span class="icon">{{ item.slice(0, 1) }}</span>
      <span>{{ item }}</span>
    </button>
  </section>

  <section class="filter-row">
    <h2>附近商家</h2>
    <div class="chips">
      <el-tag
        v-for="item in sortOptions"
        :key="item.value"
        round
        :effect="selectedSort === item.value ? 'dark' : 'plain'"
        class="sort-chip"
        @click="selectedSort = item.value"
      >
        {{ item.label }}
      </el-tag>
    </div>
  </section>

  <section class="list">
    <el-skeleton v-if="loading" :rows="4" animated />

    <template v-else>
      <RevealOnScroll v-for="store in filteredStores" :key="store.id">
        <article class="cc-card store-card hover-lift" @click="openStore(store.id)">
          <div class="cover" :style="{ background: store.cover }"></div>
          <div class="store-main">
            <div class="name-row">
              <h3>{{ store.storeName }}</h3>
              <span class="score">{{ store.score.toFixed(1) }}分</span>
            </div>
            <p class="desc">{{ store.notice || '欢迎下单，感谢支持' }}</p>
            <p class="meta">
              月售 {{ store.monthlySales }} 单 · 人均 {{ store.avgPrice }} 元 · 起送 {{ store.minOrderAmount }} 元 ·
              配送 {{ store.deliveryFee }} 元
            </p>
            <p class="meta">预计 {{ store.deliveryTime }} 送达 · {{ store.distance }}</p>
            <div class="tags">
              <span v-for="tag in store.tags" :key="tag" class="cc-tag">{{ tag }}</span>
              <span class="cc-tag category-tag">{{ deriveStoreCategory(store) }}</span>
              <span class="cc-tag flash-tag">秒杀专区</span>
            </div>
            <p class="notice">{{ store.address }}</p>
          </div>
          <div class="card-actions">
            <el-button type="warning" plain round class="flash-btn" @click.stop="openSeckill(store.id)">去秒杀</el-button>
            <el-button type="primary" round class="order-btn" @click.stop="openStore(store.id)">去点餐</el-button>
          </div>
        </article>
      </RevealOnScroll>

      <el-empty v-if="filteredStores.length === 0" description="没有匹配的商家，换个关键词或分类试试" />
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import RevealOnScroll from '@/components/RevealOnScroll.vue'
import { fetchAddressList, fetchStoreList, type StoreSimpleResponse, type UserAddressResponse } from '@/api/student'
import { buildStudentHomeCampus, buildStudentHomeContextTag } from '@/views/student/student-home'
import {
  deriveStoreCategory,
  filterAndSortStores,
  type StudentHomeStore,
  type StudentStoreSortMode,
} from '@/views/student/student-touchpoints'

const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const selectedCategory = ref('全部')
const selectedSort = ref<StudentStoreSortMode>('default')
const stores = ref<StudentHomeStore[]>([])
const addresses = ref<UserAddressResponse[]>([])

const coverPalettes = [
  'linear-gradient(145deg, #ffd86b, #ffb84d)',
  'linear-gradient(145deg, #f4b27f, #ef8f67)',
  'linear-gradient(145deg, #a8dfba, #73c6a4)',
  'linear-gradient(145deg, #92ccff, #5fa2ff)',
]

const sortOptions: Array<{ label: string; value: StudentStoreSortMode }> = [
  { label: '综合排序', value: 'default' },
  { label: '距离最近', value: 'distance' },
  { label: '起送价最低', value: 'minOrderAmount' },
  { label: '配送费最低', value: 'deliveryFee' },
]

const categories = computed(() => {
  const dynamicCategories = Array.from(new Set(stores.value.map((item) => deriveStoreCategory(item))))
  return ['全部', ...dynamicCategories]
})

const filteredStores = computed(() =>
  filterAndSortStores(stores.value, {
    keyword: keyword.value,
    category: selectedCategory.value,
    sortMode: selectedSort.value,
  }),
)

const campusLabel = computed(() => buildStudentHomeCampus(addresses.value))
const contextTag = computed(() => buildStudentHomeContextTag(new Date()))

const normalizeStore = (item: StoreSimpleResponse, index: number): StudentHomeStore => {
  const minOrderAmount = Number(item.minOrderAmount ?? 0)
  const deliveryFee = Number(item.deliveryFee ?? 0)
  return {
    ...item,
    minOrderAmount,
    deliveryFee,
    score: Number((4.6 + (index % 3) * 0.1).toFixed(1)),
    deliveryTime: `${22 + (index % 4) * 3}分钟`,
    distance: `${320 + index * 130}m`,
    tags:
      item.businessStatus === 1
        ? ['营业中', '校园配送', '品质商家']
        : ['暂时休息', '可提前预约'],
    monthlySales: 860 + index * 237,
    avgPrice: Math.max(minOrderAmount + 5, 12),
    cover: coverPalettes[index % coverPalettes.length]!,
  }
}

const loadStores = async () => {
  loading.value = true
  try {
    const [storeResult, addressResult] = await Promise.allSettled([fetchStoreList(), fetchAddressList()])

    if (storeResult.status === 'fulfilled') {
      stores.value = storeResult.value.map((item, index) => normalizeStore(item, index))
    } else {
      throw storeResult.reason
    }

    if (addressResult.status === 'fulfilled') {
      addresses.value = addressResult.value
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '加载门店失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const openStore = (storeId: number) => {
  router.push(`/student/store/${storeId}`)
}

const openSeckill = (storeId: number) => {
  router.push(`/student/store/${storeId}/seckill`)
}

onMounted(loadStores)
</script>

<style scoped>
.mt-top {
  padding: 18px;
  border-color: rgba(255, 184, 26, 0.35);
  background:
    radial-gradient(circle at right top, rgba(255, 222, 130, 0.55), transparent 32%),
    linear-gradient(180deg, #fffef8, #fffdf5);
}

.location-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.campus {
  margin: 0;
  font-size: 13px;
  color: #6c5b2e;
}

.title {
  margin: 6px 0 0;
  font-size: 28px;
  line-height: 1.2;
  letter-spacing: -0.02em;
}

.tag-weather {
  background: #ffbe2e;
  border-color: #ffbe2e;
  color: #2c2c2c;
}

.search {
  margin-top: 14px;
}

.benefits {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.benefits span {
  height: 28px;
  display: inline-flex;
  align-items: center;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  color: #5e4b24;
  background: #fff2cc;
  border: 1px solid #ffe2a1;
}

.category {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 8px;
  padding: 12px;
}

.category-item {
  border: none;
  background: #fff;
  border-radius: 10px;
  display: grid;
  justify-items: center;
  gap: 6px;
  padding: 10px 6px;
  cursor: pointer;
  color: var(--cc-color-text);
  transition: transform 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
}

.category-item:hover,
.category-item.active {
  transform: translateY(-1px);
  background: #fff8e5;
  box-shadow: inset 0 0 0 1px #ffd475;
}

.icon {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  color: #8b6513;
  background: #fff3d8;
}

.filter-row {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.filter-row h2 {
  margin: 0;
  font-size: 22px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sort-chip {
  cursor: pointer;
}

.list {
  margin-top: 14px;
  display: grid;
  gap: 14px;
}

.store-card {
  display: grid;
  grid-template-columns: 104px 1fr auto;
  align-items: start;
  gap: 14px;
  padding: 14px;
  cursor: pointer;
}

.cover {
  width: 104px;
  height: 104px;
  border-radius: 12px;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.4);
}

.store-main {
  min-width: 0;
}

.name-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
}

.name-row h3 {
  margin: 0;
  font-size: 20px;
  line-height: 1.25;
}

.score {
  font-size: 12px;
  color: #664f1e;
  background: #ffefc5;
  border-radius: 999px;
  padding: 3px 9px;
}

.desc {
  margin: 8px 0 0;
  color: var(--cc-color-text-secondary);
}

.meta {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--cc-color-text-secondary);
}

.tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.cc-tag {
  color: #7b5914;
  background: #fff4d8;
}

.category-tag {
  background: #fff0c2;
}

.flash-tag {
  background: #ffe5d6;
  color: #b84f1f;
}

.notice {
  margin: 8px 0 0;
  font-size: 12px;
  color: #9a7a36;
}

.card-actions {
  align-self: center;
  display: grid;
  gap: 10px;
}

.flash-btn {
  --el-button-hover-bg-color: #fff2e8;
  --el-button-hover-border-color: #ffb884;
}

.order-btn {
  --el-button-bg-color: #ffbf2f;
  --el-button-border-color: #ffbf2f;
  --el-button-hover-bg-color: #ffb621;
  --el-button-hover-border-color: #ffb621;
  --el-button-text-color: #2d250f;
}

@media (max-width: 1080px) {
  .category {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 24px;
  }

  .filter-row {
    display: grid;
    gap: 10px;
  }

  .store-card {
    grid-template-columns: 88px 1fr;
  }

  .cover {
    width: 88px;
    height: 88px;
  }

  .card-actions {
    grid-column: 2;
    justify-self: end;
  }
}
</style>
