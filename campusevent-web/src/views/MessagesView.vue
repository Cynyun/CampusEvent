<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listMessages, markAllMessagesRead, markMessageRead } from '@/api/messages'
import { formatDateTime } from '@/utils/format'

const messages = ref([])
const loading = ref(false)
const marking = ref(false)

const unreadCount = computed(() => messages.value.filter((item) => !item.isRead).length)

async function load() {
  loading.value = true
  try {
    const data = await listMessages()
    messages.value = Array.isArray(data) ? data : []
  } catch {
    messages.value = []
  } finally {
    loading.value = false
  }
}

async function read(message) {
  if (message.isRead) return
  try {
    await markMessageRead(message.id)
    message.isRead = true
  } catch {
    // http.js 已提示
  }
}

async function markAll() {
  if (!unreadCount.value) return
  marking.value = true
  try {
    await markAllMessagesRead()
    messages.value.forEach((item) => {
      item.isRead = true
    })
    ElMessage.success('已全部标记为已读')
  } catch {
    // http.js 已提示
  } finally {
    marking.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="eyebrow">站内通知</div>
        <h1 class="page-title">消息中心</h1>
        <p class="page-subtitle">报名提交、审批通过与拒绝、取消报名都会在这里留下记录。</p>
      </div>
      <div class="page-actions">
        <el-button :disabled="unreadCount === 0" :loading="marking" @click="markAll">全部标为已读</el-button>
        <el-button :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-label">消息总数</div>
        <div class="stat-value">{{ messages.length }}</div>
        <div class="stat-hint">全部站内通知</div>
      </div>
      <div class="stat-card accent">
        <div class="stat-label">未读消息</div>
        <div class="stat-value">{{ unreadCount }}</div>
        <div class="stat-hint">点击消息即可标记已读</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">已读消息</div>
        <div class="stat-value">{{ messages.length - unreadCount }}</div>
        <div class="stat-hint">已处理的通知</div>
      </div>
    </div>

    <div class="panel message-list" v-loading="loading">
      <div
        v-for="message in messages"
        :key="message.id"
        class="message-item"
        :class="{ unread: !message.isRead }"
        @click="read(message)"
      >
        <span class="message-dot" />
        <div class="message-body">
          <div class="message-content">{{ message.content }}</div>
          <div class="message-time">{{ formatDateTime(message.createdAt) }}</div>
        </div>
        <el-tag v-if="!message.isRead" type="warning" size="small" effect="light" round>未读</el-tag>
      </div>
      <el-empty v-if="!loading && messages.length === 0" description="还没有站内通知" />
    </div>
  </div>
</template>
