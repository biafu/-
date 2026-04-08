<template>
  <section class="header glass-panel">
    <div>
      <h1 class="cc-section-title">确认订单</h1>
    </div>
    <el-button plain @click="router.push('/student/cart')">返回购物车</el-button>
  </section>

  <StudentCheckoutAddressCard :address="defaultAddress" @manage-address="goToAddressManager(addresses.length === 0)" />

  <section class="cc-card quick-actions">
    <el-button round plain :disabled="addresses.length === 0" @click="addressDialogVisible = true">切换地址</el-button>
    <el-button plain @click="goToAddressManager(addresses.length === 0)">
      {{ addresses.length === 0 ? '去新增地址' : '管理地址' }}
    </el-button>
  </section>

  <section class="cc-card items-card">
    <h2>商品清单</h2>
    <div v-if="displayItems.length === 0" class="empty-text">当前没有可结算商品。</div>
    <ul v-else class="items-list">
      <li v-for="item in displayItems" :key="`${item.skuId}-${item.skuName}`">
        <div>
          <strong>{{ item.productName }}</strong>
          <p>{{ item.skuName }}</p>
        </div>
        <div class="item-side">
          <span>x{{ item.quantity }}</span>
          <strong>{{ toPrice(item.totalAmount) }}</strong>
        </div>
      </li>
    </ul>
  </section>

  <section class="cc-card coupon-card">
    <div class="section-head">
      <div>
        <h2>优惠券</h2>
        <p>切换优惠券后会重新试算订单金额。</p>
      </div>
      <el-tag v-if="selectedCoupon" type="success" effect="plain" round>已选择</el-tag>
    </div>

    <div v-if="couponLoading" class="coupon-loading">
      <el-skeleton :rows="3" animated />
    </div>
    <template v-else>
      <div class="coupon-grid">
        <button type="button" class="coupon-option" :class="{ active: !selectedCouponId }" @click="selectCoupon()">
          <strong>不使用优惠券</strong>
          <p>按原价下单</p>
        </button>
        <button
          v-for="coupon in coupons"
          :key="coupon.userCouponId"
          type="button"
          class="coupon-option"
          :class="{ active: selectedCouponId === coupon.userCouponId }"
          @click="selectCoupon(coupon.userCouponId)"
        >
          <strong>{{ coupon.couponName }}</strong>
          <p>满 {{ toPrice(coupon.thresholdAmount) }} 减 {{ toPrice(coupon.discountAmount) }}</p>
        </button>
      </div>
      <p v-if="coupons.length === 0" class="empty-text">当前门店暂无可用优惠券。</p>
    </template>
  </section>

  <section class="cc-card remark-card">
    <div class="section-head">
      <div>
        <h2>订单备注</h2>
        <p>口味偏好、配送提醒等会随订单一起提交给商家。</p>
      </div>
      <span>{{ remarkLength }}/60</span>
    </div>
    <el-input
      :model-value="checkoutDraft?.remark ?? ''"
      type="textarea"
      :rows="3"
      maxlength="60"
      show-word-limit
      resize="none"
      placeholder="例如：少辣、不加香菜，送到楼下电话联系"
      @update:model-value="updateRemark"
    />
  </section>

  <StudentOrderAmountCard :preview="previewResult" />

  <section class="submit-bar cc-card">
    <div>
      <strong>待支付 {{ toPrice(previewResult?.payAmount ?? 0) }}</strong>
      <p v-if="!defaultAddress">请先配置默认收货地址</p>
      <p v-else>下单后会跳转到支付结果页</p>
    </div>
    <el-button type="primary" round :loading="submitting" :disabled="!canSubmit" @click="submitOrder">
      提交订单
    </el-button>
  </section>

  <el-dialog v-model="addressDialogVisible" title="选择收货地址" width="560px">
    <div v-if="addresses.length === 0" class="dialog-empty">
      <el-empty description="还没有可选地址" />
    </div>
    <div v-else class="address-options">
      <button
        v-for="item in addresses"
        :key="item.id"
        type="button"
        class="address-option"
        :class="{ active: selectedAddressId === item.id }"
        @click="selectAddress(item.id)"
      >
        <div class="address-option-head">
          <strong>{{ item.contactName }} {{ item.contactPhone }}</strong>
          <el-tag v-if="item.isDefault === 1" size="small" type="success" effect="plain" round>默认</el-tag>
        </div>
        <p>{{ item.fullAddress }}</p>
      </button>
    </div>
    <template #footer>
      <el-button @click="addressDialogVisible = false">关闭</el-button>
      <el-button type="primary" plain @click="goToAddressManager(addresses.length === 0)">去地址管理</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  createStudentOrder,
  fetchAddressList,
  fetchMyCoupons,
  previewStudentOrder,
  type MyCouponResponse,
  type OrderItemRequest,
  type UserAddressResponse,
} from '@/api/student'
import StudentCheckoutAddressCard from '@/components/student/StudentCheckoutAddressCard.vue'
import StudentOrderAmountCard from '@/components/student/StudentOrderAmountCard.vue'
import { useStudentCheckoutStore } from '@/stores/student-checkout'
import { buildStudentAddressRouteQuery } from '@/views/student/student-address'

type CheckoutDisplayItem = {
  skuId: number
  productName: string
  skuName: string
  quantity: number
  totalAmount: number
}

const router = useRouter()
const checkoutStore = useStudentCheckoutStore()
const submitting = ref(false)
const couponLoading = ref(false)
const defaultAddress = ref<UserAddressResponse | null>(null)
const addresses = ref<UserAddressResponse[]>([])
const coupons = ref<MyCouponResponse[]>([])
const selectedAddressId = ref<number>()
const addressDialogVisible = ref(false)

const checkoutDraft = computed(() => checkoutStore.checkoutDraft)
const previewResult = computed(() => checkoutStore.previewResult)
const selectedCouponId = computed(() => checkoutDraft.value?.userCouponId)
const selectedCoupon = computed(() => coupons.value.find((item) => item.userCouponId === selectedCouponId.value))
const remarkLength = computed(() => (checkoutDraft.value?.remark ?? '').length)
const displayItems = computed<CheckoutDisplayItem[]>(() => {
  if (previewResult.value?.items?.length) {
    return previewResult.value.items.map((item) => ({
      skuId: item.skuId,
      productName: item.productName,
      skuName: item.skuName,
      quantity: item.quantity,
      totalAmount: Number(item.totalAmount ?? 0),
    }))
  }

  if (!checkoutDraft.value) {
    return []
  }

  const cartMap = new Map(checkoutStore.cartItems.map((item) => [item.skuId, item]))
  return checkoutDraft.value.items.map((item) => {
    const matched = cartMap.get(item.skuId)
    return {
      skuId: item.skuId,
      productName: matched?.productName ?? '商品',
      skuName: matched?.skuName ?? '默认规格',
      quantity: item.quantity,
      totalAmount: Number(matched?.price ?? 0) * item.quantity,
    }
  })
})

const canSubmit = computed(() => Boolean(defaultAddress.value && checkoutDraft.value?.items.length && previewResult.value))

const toPrice = (value: number) => `${Number(value ?? 0).toFixed(2)}元`

const goToAddressManager = async (openCreate = false) => {
  await router.push({
    path: '/student/addresses',
    query: buildStudentAddressRouteQuery({
      returnToCheckout: true,
      openCreate,
    }),
  })
}

const buildPayload = () => {
  if (!checkoutDraft.value) {
    return null
  }

  const items = checkoutDraft.value.items.filter((item) => Number(item.quantity ?? 0) > 0) as OrderItemRequest[]
  if (items.length === 0) {
    return null
  }

  const receiverName = checkoutDraft.value.receiverName?.trim()
  const receiverPhone = checkoutDraft.value.receiverPhone?.trim()
  const receiverAddress = checkoutDraft.value.receiverAddress?.trim()

  if (!receiverName || !receiverPhone || !receiverAddress) {
    return null
  }

  return {
    storeId: checkoutDraft.value.storeId,
    items,
    receiverName,
    receiverPhone,
    receiverAddress,
    userCouponId: checkoutDraft.value.userCouponId,
    remark: checkoutDraft.value.remark?.trim() || undefined,
  }
}

const loadCoupons = async () => {
  if (!checkoutDraft.value) {
    coupons.value = []
    return
  }

  couponLoading.value = true
  try {
    coupons.value = await fetchMyCoupons(checkoutDraft.value.storeId)
    if (selectedCouponId.value && !coupons.value.some((item) => item.userCouponId === selectedCouponId.value)) {
      checkoutStore.setCoupon()
    }
  } catch (error) {
    coupons.value = []
    const message = error instanceof Error ? error.message : '优惠券加载失败'
    ElMessage.error(message)
  } finally {
    couponLoading.value = false
  }
}

const previewOrder = async () => {
  const payload = buildPayload()
  if (!payload) {
    checkoutStore.previewResult = null
    return
  }

  try {
    checkoutStore.previewResult = await previewStudentOrder(payload)
  } catch (error) {
    checkoutStore.previewResult = null
    const message = error instanceof Error ? error.message : '订单试算失败'
    ElMessage.error(message)
  }
}

const selectAddress = async (addressId: number) => {
  const matched = addresses.value.find((item) => item.id === addressId)
  if (!matched) {
    return
  }

  selectedAddressId.value = addressId
  defaultAddress.value = matched
  checkoutStore.fillReceiver(matched)
  addressDialogVisible.value = false
  await previewOrder()
}

const selectCoupon = async (userCouponId?: number) => {
  checkoutStore.setCoupon(userCouponId)
  await previewOrder()
}

const updateRemark = (value: string | number) => {
  checkoutStore.setRemark(String(value ?? '').slice(0, 60))
}

const loadCheckoutData = async () => {
  if (!checkoutDraft.value || checkoutDraft.value.items.length === 0) {
    ElMessage.warning('请先从购物车选择要结算的商品')
    await router.replace('/student/cart')
    return
  }

  try {
    const [addressResult] = await Promise.all([fetchAddressList(), loadCoupons()])
    addresses.value = addressResult
    defaultAddress.value = addressResult.find((item) => item.isDefault === 1) ?? addressResult[0] ?? null
    if (defaultAddress.value) {
      selectedAddressId.value = defaultAddress.value.id
      checkoutStore.fillReceiver(defaultAddress.value)
      await previewOrder()
    } else {
      checkoutStore.previewResult = null
    }
  } catch (error) {
    checkoutStore.previewResult = null
    const message = error instanceof Error ? error.message : '加载结算信息失败'
    ElMessage.error(message)
  }
}

const submitOrder = async () => {
  const payload = buildPayload()
  if (!payload) {
    ElMessage.warning('请先补全收货地址后再提交订单')
    return
  }

  submitting.value = true
  try {
    const result = await createStudentOrder(payload)
    checkoutStore.clearStoreCart(payload.storeId)
    await router.replace({
      path: '/student/payment-result',
      query: {
        orderId: String(result.orderId),
        orderNo: result.orderNo,
        payAmount: String(result.payAmount),
        orderStatus: result.orderStatus,
      },
    })
  } catch (error) {
    const message = error instanceof Error ? error.message : '提交订单失败'
    ElMessage.error(message)
    await previewOrder()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void loadCheckoutData()
})
</script>

<style scoped>
.header,
.submit-bar,
.quick-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px;
}

.items-card {
  display: grid;
  gap: 14px;
  padding: 20px;
}

.coupon-card,
.remark-card {
  display: grid;
  gap: 14px;
  padding: 20px;
}

.items-card h2,
.coupon-card h2,
.remark-card h2 {
  margin: 0;
  font-size: 18px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-head p,
.section-head span {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
}

.items-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 12px;
}

.items-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid var(--cc-color-border);
}

.items-list li:last-child {
  border-bottom: none;
}

.items-list p,
.submit-bar p,
.empty-text {
  margin: 6px 0 0;
  color: var(--cc-color-text-secondary);
}

.item-side {
  display: grid;
  justify-items: end;
  gap: 6px;
}

.coupon-grid,
.address-options {
  display: grid;
  gap: 12px;
}

.coupon-option,
.address-option {
  width: 100%;
  border: 1px solid var(--cc-color-border);
  border-radius: 18px;
  padding: 14px 16px;
  background: color-mix(in srgb, white 82%, #f4f1e8 18%);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, transform 0.2s ease, box-shadow 0.2s ease;
}

.coupon-option:hover,
.address-option:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--cc-color-primary) 55%, white 45%);
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.06);
}

.coupon-option.active,
.address-option.active {
  border-color: var(--cc-color-primary);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--cc-color-primary) 40%, white 60%);
}

.coupon-option strong,
.address-option strong {
  display: block;
}

.coupon-option p,
.address-option p,
.dialog-empty {
  margin: 8px 0 0;
  color: var(--cc-color-text-secondary);
}

.address-option-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

@media (max-width: 768px) {
  .header,
  .submit-bar,
  .quick-actions,
  .section-head,
  .items-list li {
    align-items: flex-start;
    flex-direction: column;
  }

  .item-side {
    justify-items: start;
  }
}
</style>
