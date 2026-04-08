<template>
  <section class="cc-card address-card">
    <div class="address-main">
      <h2>收货地址</h2>
      <template v-if="address">
        <p class="contact">{{ address.contactName }} {{ address.contactPhone }}</p>
        <p class="full-address">{{ address.fullAddress }}</p>
      </template>
      <template v-else>
        <p class="empty-text">还没有默认地址，先去地址管理里添加一个吧。</p>
      </template>
    </div>

    <el-button plain @click="emit('manage-address')">
      {{ address ? '管理地址' : '去新增地址' }}
    </el-button>
  </section>
</template>

<script setup lang="ts">
import type { UserAddressResponse } from '@/api/student'

defineProps<{
  address: UserAddressResponse | null
}>()

const emit = defineEmits<{
  'manage-address': []
}>()
</script>

<style scoped>
.address-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
}

.address-main h2 {
  margin: 0;
  font-size: 18px;
}

.contact,
.full-address,
.empty-text {
  margin: 8px 0 0;
}

.contact {
  font-weight: 600;
}

.full-address,
.empty-text {
  color: var(--cc-color-text-secondary);
}

@media (max-width: 768px) {
  .address-card {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
