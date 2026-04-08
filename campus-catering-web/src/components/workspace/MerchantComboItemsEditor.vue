<template>
  <section class="combo-editor">
    <div v-if="showHeader" class="combo-head">
      <div>
        <h3>套餐组成</h3>
        <p>从现有单品里选择固定组成，并设置每项数量。</p>
      </div>
      <el-button plain @click="appendRow">添加组成项</el-button>
    </div>
    <div v-else class="combo-toolbar">
      <el-button plain @click="appendRow">添加组成项</el-button>
    </div>

    <el-empty v-if="modelValue.length === 0" description="当前还没有套餐组成项" :image-size="72" />

    <div v-else class="rows">
      <div v-for="(row, index) in modelValue" :key="index" class="row">
        <el-select
          :model-value="row.skuId ?? undefined"
          placeholder="选择单品 SKU"
          filterable
          @update:model-value="updateSku(index, $event)"
        >
          <el-option
            v-for="candidate in candidates"
            :key="candidate.skuId"
            :label="`${candidate.productName} / ${candidate.skuName}`"
            :value="candidate.skuId"
            :disabled="isSkuTaken(candidate.skuId, index)"
          />
        </el-select>
        <el-input-number
          :model-value="row.quantity"
          :min="1"
          :step="1"
          @update:model-value="updateQuantity(index, $event)"
        />
        <el-button type="danger" plain @click="removeRow(index)">删除</el-button>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { MerchantComboCandidate, MerchantComboDraftItem } from '@/views/workspace/merchant-product-editor'

const props = defineProps<{
  modelValue: MerchantComboDraftItem[]
  candidates: MerchantComboCandidate[]
  showHeader?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: MerchantComboDraftItem[]]
}>()

const sync = (value: MerchantComboDraftItem[]) => {
  emit('update:modelValue', value)
}

const appendRow = () => {
  sync([...props.modelValue, { skuId: null, quantity: 1 }])
}

const updateSku = (index: number, value: number | string) => {
  sync(
    props.modelValue.map((item, currentIndex) =>
      currentIndex === index ? { ...item, skuId: Number(value) } : item,
    ),
  )
}

const updateQuantity = (index: number, value: number | undefined) => {
  sync(
    props.modelValue.map((item, currentIndex) =>
      currentIndex === index ? { ...item, quantity: Math.max(1, Number(value ?? 1)) } : item,
    ),
  )
}

const removeRow = (index: number) => {
  sync(props.modelValue.filter((_, currentIndex) => currentIndex !== index))
}

const isSkuTaken = (skuId: number, currentIndex: number) =>
  props.modelValue.some((item, index) => index !== currentIndex && item.skuId === skuId)
</script>

<style scoped>
.combo-editor {
  border-top: 1px solid var(--cc-color-border);
  padding-top: 20px;
}

.combo-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.combo-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 14px;
}

.combo-head h3 {
  margin: 0;
  font-size: 16px;
}

.combo-head p {
  margin: 6px 0 0;
  color: var(--cc-color-text-tertiary);
  font-size: 13px;
}

.rows {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 120px auto;
  gap: 10px;
}

@media (max-width: 768px) {
  .combo-head {
    flex-direction: column;
  }

  .combo-toolbar {
    justify-content: stretch;
  }

  .combo-toolbar .el-button {
    width: 100%;
  }

  .row {
    grid-template-columns: 1fr;
  }
}
</style>
