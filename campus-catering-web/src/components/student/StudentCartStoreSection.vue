<template>
  <section class="cc-card section">
    <header class="section-head">
      <div>
        <h2>{{ storeName }}</h2>
        <p>共 {{ itemCount }} 件商品 · 小计 {{ toPrice(subtotal) }}</p>
      </div>
      <el-button text :loading="clearing" @click="emit('clear', storeId)">清空门店</el-button>
    </header>

    <div class="items">
      <article v-for="item in items" :key="item.id" class="item-row">
        <div class="thumb-wrap">
          <el-image :src="buildStudentCartImageUrl(item)" fit="cover" class="thumb">
            <template #error>
              <div class="thumb-fallback">
                <span>{{ item.productName.slice(0, 2) }}</span>
              </div>
            </template>
          </el-image>
        </div>
        <div class="item-main">
          <h3>{{ item.productName }}</h3>
          <p>{{ item.skuName }}</p>
        </div>
        <div class="item-side">
          <strong>{{ toPrice(item.totalAmount) }}</strong>
          <span class="item-price">{{ toPrice(item.price) }}/份</span>
          <div class="item-actions">
            <el-button circle :disabled="item.quantity <= 1 || isItemBusy(item.id)" @click="emit('decrease', item)">-</el-button>
            <span class="quantity">{{ item.quantity }}</span>
            <el-button circle :loading="isItemBusy(item.id)" @click="emit('increase', item)">+</el-button>
            <el-button text type="danger" :disabled="isItemBusy(item.id)" @click="emit('delete', item)">删除</el-button>
          </div>
        </div>
      </article>
    </div>

    <footer class="section-foot">
      <p>当前仅支持按单门店发起结算。</p>
      <el-button type="primary" round :loading="checkouting" @click="emit('checkout', storeId)">去结算</el-button>
    </footer>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StudentCartItem } from '@/stores/student-checkout'
import { buildStudentCartImageUrl } from '@/views/student/student-cart-items'

const props = withDefaults(
  defineProps<{
    storeId: number
    storeName: string
    items: StudentCartItem[]
    busyItemIds?: number[]
    clearing?: boolean
    checkouting?: boolean
  }>(),
  {
    busyItemIds: () => [],
    clearing: false,
    checkouting: false,
  },
)

const emit = defineEmits<{
  increase: [item: StudentCartItem]
  decrease: [item: StudentCartItem]
  delete: [item: StudentCartItem]
  clear: [storeId: number]
  checkout: [storeId: number]
}>()

const subtotal = computed(() => props.items.reduce((sum, item) => sum + Number(item.totalAmount ?? 0), 0))
const itemCount = computed(() => props.items.reduce((sum, item) => sum + Number(item.quantity ?? 0), 0))

const toPrice = (value: number) => `${Number(value ?? 0).toFixed(2)}元`
const isItemBusy = (id: number) => props.busyItemIds.includes(id)
</script>

<style scoped>
.section {
  display: grid;
  gap: 18px;
  padding: 20px;
}

.section-head,
.section-foot,
.item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head h2,
.item-main h3 {
  margin: 0;
}

.section-head p,
.section-foot p,
.item-main p,
.item-price {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
}

.items {
  display: grid;
  gap: 12px;
}

.item-row {
  padding: 16px;
  border-radius: 14px;
  background: var(--cc-color-secondary-soft);
  border: 1px solid var(--cc-color-border);
}

.thumb-wrap {
  flex-shrink: 0;
}

.thumb,
.thumb-fallback {
  width: 84px;
  height: 84px;
  border-radius: 16px;
  overflow: hidden;
}

.thumb-fallback {
  display: grid;
  place-items: center;
  background: linear-gradient(145deg, #ffe4b5, #ffb36b);
  color: #7a3b00;
  font-size: 20px;
  font-weight: 700;
}

.item-main {
  min-width: 0;
  flex: 1;
}

.item-main p {
  font-size: 13px;
}

.item-side {
  display: grid;
  gap: 6px;
  justify-items: end;
}

.item-side strong {
  font-size: 18px;
  color: var(--cc-color-primary);
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity {
  min-width: 24px;
  text-align: center;
  font-weight: 600;
}

.section-foot p {
  margin: 0;
}

@media (max-width: 768px) {
  .section-head,
  .section-foot,
  .item-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .item-side {
    width: 100%;
    justify-items: start;
  }

  .item-actions {
    flex-wrap: wrap;
  }
}
</style>
