<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteActivity, listAdminActivities } from '@/api/activities'
import { activityState, formatDateTime } from '@/utils/format'
import PolicyTag from '@/components/PolicyTag.vue'

const router = useRouter()
const rows = ref([])
const loading = ref(false)
const includeDeleted = ref(false)
const keyword = ref('')

const filteredRows = computed(() => {
  const text = keyword.value.trim()
  if (!text) return rows.value
  return rows.value.filter((item) => item.title.includes(text) || item.category.includes(text))
})

async function load() {
  loading.value = true
  try {
    const data = await listAdminActivities({ includeDeleted: includeDeleted.value })
    rows.value = Array.isArray(data) ? data : []
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function goCreate() {
  router.push('/admin/activities/new')
}

function goEdit(row) {
  router.push('/admin/activities/' + row.id + '/edit')
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(
      '删除后学生端将不再展示该活动，历史报名与通知仍会保留。确定删除吗？',
      '删除活动',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await deleteActivity(row.id)
    ElMessage.success('活动已删除')
    await load()
  } catch {
    // http.js 已提示
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="eyebrow">管理端 · 活动</div>
        <h1 class="page-title">活动管理</h1>
        <p class="page-subtitle">创建活动、调整容量与报名方式；删除采用软删除，历史报名与通知保留。</p>
      </div>
      <el-button type="primary" @click="goCreate">+ 新建活动</el-button>
    </div>

    <div class="panel toolbar">
      <div class="toolbar-group">
        <el-input v-model="keyword" placeholder="按名称或分类过滤" clearable style="width: 240px" />
        <el-switch v-model="includeDeleted" active-text="显示已删除" @change="load" />
      </div>
      <div class="toolbar-spacer" />
      <span class="text-muted" style="font-size: 12px">共 {{ filteredRows.length }} 条</span>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <div class="panel" v-loading="loading">
      <el-table :data="filteredRows" style="width: 100%" empty-text="还没有活动，先创建第一个吧">
        <el-table-column label="活动" min-width="220">
          <template #default="{ row }">
            <div style="font-weight: 600; color: var(--ce-ink)">{{ row.title }}</div>
            <div class="sub">{{ row.category }} · {{ row.location }}</div>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.startAt) }}</template>
        </el-table-column>
        <el-table-column label="报名方式" width="120">
          <template #default="{ row }"><PolicyTag :type="row.policyType" /></template>
        </el-table-column>
        <el-table-column label="容量" width="110">
          <template #default="{ row }">
            <span style="font-variant-numeric: tabular-nums">{{ row.approvedCount || 0 }} / {{ row.capacity }}</span>
          </template>
        </el-table-column>
        <el-table-column label="待审核" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.pendingCount" type="warning" effect="light" round>{{ row.pendingCount }}</el-tag>
            <span v-else class="sub">0</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.deletedAt" type="info" effect="plain" round>已删除</el-tag>
            <el-tag v-else :type="activityState(row) === '报名中' ? 'success' : activityState(row) === '已满员' ? 'warning' : 'info'" effect="light" round>
              {{ activityState(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="right">
          <template #default="{ row }">
            <template v-if="!row.deletedAt">
              <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
            <span v-else class="sub">已归档</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
