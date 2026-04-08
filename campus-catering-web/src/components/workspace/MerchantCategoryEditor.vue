<template>
  <el-dialog
    :model-value="visible"
    title="新增分类"
    width="420px"
    destroy-on-close
    @close="emit('update:visible', false)"
  >
    <el-form label-position="top">
      <el-form-item label="分类名称">
        <el-input v-model="form.categoryName" maxlength="24" show-word-limit />
      </el-form-item>
      <el-form-item label="排序值">
        <el-input-number v-model="form.sortNo" :min="0" :step="1" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps<{
  visible: boolean
  submitting?: boolean
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  submit: [payload: { categoryName: string; sortNo?: number }]
}>()

const form = reactive({
  categoryName: '',
  sortNo: 0,
})

watch(
  () => props.visible,
  (visible) => {
    if (visible) {
      form.categoryName = ''
      form.sortNo = 0
    }
  },
)

const submit = () => {
  if (!form.categoryName.trim()) {
    ElMessage.warning('请先填写分类名称')
    return
  }
  emit('submit', {
    categoryName: form.categoryName.trim(),
    sortNo: form.sortNo,
  })
}
</script>
