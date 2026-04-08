<template>
  <section class="header glass-panel">
    <div>
      <h1 class="cc-section-title">消息中心</h1>
    </div>
    <div class="actions">
      <el-switch v-model="onlyUnread" inline-prompt active-text="仅未读" inactive-text="全部" @change="loadMessages" />
      <el-button round @click="markAllRead">全部已读</el-button>
      <el-button type="primary" round @click="loadMessages">刷新</el-button>
    </div>
  </section>

  <section class="list">
    <el-skeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <article v-for="msg in messages" :key="msg.id" class="cc-card msg-card" :class="{ unread: msg.isRead === 0 }">
        <div class="top">
          <h3>{{ msg.title }}</h3>
          <span>{{ formatTime(msg.createdAt) }}</span>
        </div>
        <p>{{ msg.content }}</p>
        <div class="bottom">
          <el-tag :type="msg.isRead === 0 ? 'danger' : 'info'" effect="plain" round>
            {{ msg.isRead === 0 ? '未读' : '已读' }}
          </el-tag>
          <div class="card-actions">
            <el-button v-if="messageActions[msg.id]" size="small" text type="primary" @click="openRelated(msg)">
              查看相关内容
            </el-button>
            <el-button v-if="msg.isRead === 0" size="small" text type="primary" @click="readOne(msg.id)">标记已读</el-button>
          </div>
        </div>
      </article>
      <el-empty v-if="messages.length === 0" description="暂时没有消息" />
    </template>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  fetchStudentMessages,
  markAllStudentMessagesRead,
  markStudentMessageRead,
  type StudentMessageResponse,
} from '@/api/student'
import { emitStudentMessageChanged } from '@/utils/student-message-events'
import { resolveMessageAction } from '@/views/student/student-touchpoints'

const router = useRouter()
const loading = ref(false)
const onlyUnread = ref(false)
const messages = ref<StudentMessageResponse[]>([])

const messageActions = computed(() =>
  Object.fromEntries(messages.value.map((message) => [message.id, resolveMessageAction(message)])),
)

const loadMessages = async () => {
  loading.value = true
  try {
    messages.value = await fetchStudentMessages({ onlyUnread: onlyUnread.value, limit: 100 })
  } catch (error) {
    const message = error instanceof Error ? error.message : '消息加载失败'
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

const readOne = async (id: number) => {
  try {
    await markStudentMessageRead(id)
    await loadMessages()
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(message)
  }
}

const openRelated = async (message: StudentMessageResponse) => {
  const action = messageActions.value[message.id]
  if (!action) {
    return
  }
  try {
    if (message.isRead === 0) {
      await markStudentMessageRead(message.id)
      emitStudentMessageChanged()
    }
    await router.push(action.to)
  } catch (error) {
    const text = error instanceof Error ? error.message : '跳转失败'
    ElMessage.error(text)
  }
}

const markAllRead = async () => {
  try {
    await markAllStudentMessagesRead()
    ElMessage.success('已全部标记为已读')
    await loadMessages()
    emitStudentMessageChanged()
  } catch (error) {
    const message = error instanceof Error ? error.message : '操作失败'
    ElMessage.error(message)
  }
}

const formatTime = (raw: string) => String(raw || '').replace('T', ' ').slice(0, 16)

onMounted(loadMessages)
</script>

<style scoped>
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 20px;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.list {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.msg-card {
  padding: 14px 16px;
}

.msg-card.unread {
  border: 1px solid #ffd8d2;
  background: #fffaf9;
}

.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.top h3 {
  margin: 0;
  font-size: 16px;
}

.top span {
  color: var(--cc-color-text-tertiary);
  font-size: 12px;
}

.msg-card p {
  margin: 10px 0;
  color: var(--cc-color-text-secondary);
}

.bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.card-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: flex-start;
  }

  .actions,
  .bottom {
    flex-wrap: wrap;
  }
}
</style>
