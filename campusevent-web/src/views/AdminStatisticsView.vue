<script setup>
import { computed, onMounted, ref } from 'vue'
import { getStatistics } from '@/api/activities'
import { activityState, formatDateTime } from '@/utils/format'

const rows = ref([])
const loading = ref(false)

const approvedTotal = computed(() => rows.value.reduce((sum, item) => sum + (item.approvedCount || 0), 0))
const pendingTotal = computed(() => rows.value.reduce((sum, item) => sum + (item.pendingCount || 0), 0))
const remainingTotal = computed(() => rows.value.reduce((sum, item) => sum + (item.remaining || 0), 0))

async function load() {
  loading.value = true
  try {
    const data = await getStatistics()
    rows.value = Array.isArray(data) ? data : []
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function percent(row) {
  if (!row.capacity) return 0
  return Math.min(100, Math.round(((row.approvedCount || 0) / row.capacity) * 100))
}

function progressColor(value) {
  if (value >= 100) return '#dc2626'
  if (value >= 80) return '#d97706'
  return '#2563eb'
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="eyebrow">管理端 · 统计</div>
        <h1 class="page-title">数据统计</h1>
        <p class="page-subtitle">按活动查看已通过人数、待审核人数与剩余名额，快速发现满员或审核积压。</p>
      </div>
      <el-button :loading="loading" @click="load">刷新数据</el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-label">已通过报名</div>
        <div class="stat-value">{{ approvedTotal }}</div>
        <div class="stat-hint">占用名额的报名总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待审核</div>
        <div class="stat-value">{{ pendingTotal }}</div>
        <div class="stat-hint">需要管理员处理的申请</div>
      </div>
      <div class="stat-card accent">
        <div class="stat-label">剩余名额</div>
        <div class="stat-value">{{ remainingTotal }}</div>
        <div class="stat-hint">全部活动剩余名额合计</div>
      </div>
    </div>

    <div class="panel" v-loading="loading">
      <el-table :data="rows" style="width: 100%" empty-text="还没有活动数据">
        <el-table-column label="活动" min-width="220">
          <template #default="{ row }">
            <div style="font-weight: 600; color: var(--ce-ink)">{{ row.title }}</div>
            <div class="sub">活动 #{{ row.activityId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.startAt) }}</template>
        </el-table-column>
        <el-table-column label="已通过 / 容量" width="130">
          <template #default="{ row }">
            <span style="font-variant-numeric: tabular-nums">{{ row.approvedCount }} / {{ row.capacity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="待审核" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.pendingCount" type="warning" effect="light" round>{{ row.pendingCount }}</el-tag>
            <span v-else class="sub">0</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余" width="80" align="center">
          <template #default="{ row }">{{ row.remaining }}</template>
        </el-table-column>
        <el-table-column label="名额使用率" min-width="180">
          <template #default="{ row }">
            <el-progress :percentage="percent(row)" :stroke-width="8" :color="progressColor(percent(row))" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="activityState(row) === '报名中' ? 'success' : activityState(row) === '已满员' ? 'warning' : 'info'" effect="light" round>{{ activityState(row) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>