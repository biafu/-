<template>
  <section class="hero glass-panel">
    <div>
      <h1 class="cc-section-title">我的购物车</h1>
    </div>
    <div class="hero-actions">
      <el-button plain @click="router.push('/student/home')">继续逛逛</el-button>
      <el-button type="primary" round :loading="loading" @click="loadPageData">刷新</el-button>
    </div>
  </section>

  <section v-if="loading" class="list">
    <el-skeleton :rows="6" animated />
  </section>

  <section v-else-if="cartGroups.length === 0" class="cc-card empty-state">
    <el-empty description="购物车还是空的，去挑点喜欢的吧" />
    <div class="empty-actions">
      <el-button type="primary" round @click="router.push('/student/home')">去逛门店</el-button>
    </div>
  </section>

  <section v-else class="list">
    <article class="summary cc-card">
      <div>
        <h2>购物车概览</h2>
        <p>{{ cartGroups.length }} 个门店分组 · 共 {{ checkoutStore.totalCount }} 件商品</p>
      </div>
      <el-tag effect="plain" size="large">一次仅支持结算一家门店</el-tag>
    </article>

    <StudentCartStoreSection
      v-for="group in cartGroups"
      :key="group.storeId"
      :store-id="group.storeId"
      :store-name="resolveStoreName(group.storeId)"
      :items="group.items"
      :busy-item-ids="busyItemIds"
      :clearing="clearingStoreId === group.storeId"
      :checkouting="checkoutingStoreId === group.storeId"
      @increase="handleIncrease"
      @decrease="handleDecrease"
      @delete="handleDelete"
      @clear="handleClearStore"
      @checkout="handleCheckout"
    />
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  clearStoreCart as clearStoreCartRequest,
  deleteCartItem as deleteCartItemRequest,
  fetchCartList,
  fetchStoreList,
  updateCartQuantity,
  type StoreSimpleResponse,
} from '@/api/student'
import StudentCartStoreSection from '@/components/student/StudentCartStoreSection.vue'
import { type StudentCartItem, useStudentCheckoutStore } from '@/stores/student-checkout'

const router = useRouter()
const checkoutStore = useStudentCheckoutStore()
const loading = ref(false)
const busyItemIds = ref<number[]>([])
const clearingStoreId = ref<number>()
const checkoutingStoreId = ref<number>()
const storeNames = ref<Record<number, string>>({})

const cartGroups = computed(() => checkoutStore.cartGroups)

const buildStoreNameMap = (stores: StoreSimpleResponse[]) => {
  storeNames.value = stores.reduce<Record<number, string>>((result, item) => {
    result[item.id] = item.storeName
    return result
  }, {})
}

const resolveStoreName = (storeId: number) => storeNames.value[storeId] ?? `门店 #${storeId}`

const setItemBusy = (id: number, busy: boolean) => {
  if (busy) {
    if (!busyItemIds.value.includes(id)) {
      busyItemIds.value = [...busyItemIds.value, id]
    }
    return
  }
  busyItemIds.value = busyItemIds.value.filter((itemId) => itemId !== id)
}

const loadPageData = async () => {
  loading.value = true
  try {
    const [cartResult, storeResult] = await Promise.allSettled([fetchCartList(), fetchStoreList()])

    if (cartResult.status === 'fulfilled') {
      checkoutStore.setCartItems(cartResult.value)
    } else {
      checkoutStore.setCartItems([])
      throw cartResult.reason
    }

    if (storeResult.status === 'fulfilled') {
      buildStoreNameMap(storeResult.value)
    } else {
      storeNames.value = {}
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '购物车加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const applyQuantityChange = async (item: StudentCartItem, nextQuantity: number) => {
  setItemBusy(item.id, true)
  try {
    await updateCartQuantity(item.id, nextQuantity)
    checkoutStore.updateCartItemQuantity(item.id, nextQuantity)
  } catch (error) {
    const message = error instanceof Error ? error.message : '更新商品数量失败'
    ElMessage.error(message)
  } finally {
    setItemBusy(item.id, false)
  }
}

const handleIncrease = async (item: StudentCartItem) => {
  await applyQuantityChange(item, Number(item.quantity ?? 0) + 1)
}

const handleDecrease = async (item: StudentCartItem) => {
  const nextQuantity = Number(item.quantity ?? 0) - 1
  if (nextQuantity < 1) {
    return
  }
  await applyQuantityChange(item, nextQuantity)
}

const handleDelete = async (item: StudentCartItem) => {
  try {
    await ElMessageBox.confirm(`确认从购物车中移除“${item.productName}”吗？`, '移除商品', {
      type: 'warning',
    })
  } catch {
    return
  }

  setItemBusy(item.id, true)
  try {
    await deleteCartItemRequest(item.id)
    checkoutStore.removeCartItem(item.id)
    ElMessage.success('商品已移除')
  } catch (error) {
    const message = error instanceof Error ? error.message : '移除商品失败'
    ElMessage.error(message)
  } finally {
    setItemBusy(item.id, false)
  }
}

const handleClearStore = async (storeId: number) => {
  try {
    await ElMessageBox.confirm('确认清空该门店下的所有商品吗？', '清空门店购物车', {
      type: 'warning',
    })
  } catch {
    return
  }

  clearingStoreId.value = storeId
  try {
    await clearStoreCartRequest(storeId)
    checkoutStore.clearStoreCart(storeId)
    ElMessage.success('门店购物车已清空')
  } catch (error) {
    const message = error instanceof Error ? error.message : '清空门店购物车失败'
    ElMessage.error(message)
  } finally {
    clearingStoreId.value = undefined
  }
}

const handleCheckout = async (storeId: number) => {
  if (!cartGroups.value.some((group) => group.storeId === storeId && group.items.length > 0)) {
    ElMessage.warning('该门店当前没有可结算商品')
    return
  }

  checkoutingStoreId.value = storeId
  try {
    checkoutStore.beginCheckout(storeId)
    await router.push('/student/checkout')
  } finally {
    checkoutingStoreId.value = undefined
  }
}

onMounted(loadPageData)
</script>

<style scoped>
.hero,
.summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
}

.hero-actions,
.empty-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.list {
  display: grid;
  gap: 16px;
}

.summary h2 {
  margin: 0;
  font-size: 18px;
}

.summary p {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
}

.empty-state {
  display: grid;
  justify-items: center;
  gap: 12px;
  padding: 28px 20px;
}

@media (max-width: 768px) {
  .hero,
  .summary {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
