<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listAdminActivities } from '@/api/activities'
import { approveRegistration, listAdminRegistrations, rejectRegistration } from '@/api/registrations'
import { formatDateTime } from '@/utils/format'
import PolicyTag from '@/components/PolicyTag.vue'
import StatusTag from '@/components/StatusTag.vue'

const rows = ref([])
const activities = ref([])
const loading = ref(false)
const filters = reactive({ status: 'PENDING', activityId: null })

async function loadActivities() {
  try {
    const data = await listAdminActivities({ includeDeleted: false })
    activities.value = Array.isArray(data) ? data : []
  } catch {
    activities.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const data = await listAdminRegistrations({
      status: filters.status || undefined,
      activityId: filters.activityId || undefined,
    })
    rows.value = Array.isArray(data) ? data : []
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

async function approve(row) {
  try {
    await ElMessageBox.confirm(
      '通过后该报名将占用活动名额；如果名额已满，系统会拒绝本次操作。确定通过吗？',
      '通过报名',
      { confirmButtonText: '确认通过', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await approveRegistration(row.id)
    ElMessage.success('报名已通过')
    await load()
  } catch {
    // http.js 已提示
  }
}

async function reject(row) {
  try {
    await ElMessageBox.confirm('拒绝后学生将收到站内通知，且不能恢复为待审核。确定拒绝吗？', '拒绝报名', {
      confirmButtonText: '确认拒绝',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await rejectRegistration(row.id)
    ElMessage.success('报名已拒绝')
    await load()
  } catch {
    // http.js 已提示
  }
}

onMounted(async () => {
  await loadActivities()
  await load()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="eyebrow">管理端 · 报名</div>
        <h1 class="page-title">报名审核</h1>
        <p class="page-subtitle">审核制报名先进入待审核；通过前会重新检查名额，满员时保持待审核状态。</p>
      </div>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <div class="panel toolbar">
      <div class="toolbar-group">
        <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 150px" @change="load">
          <el-option label="待审核" value="PENDING" />
          <el-option label="已通过" value="APPROVED" />
          <el-option label="已拒绝" value="REJECTED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
        <el-select
          v-model="filters.activityId"
          placeholder="全部活动"
          clearable
          filterable
          style="width: 260px"
          @change="load"
        >
          <el-option v-for="activity in activities" :key="activity.id" :label="activity.title" :value="activity.id" />
        </el-select>
      </div>
      <div class="toolbar-spacer" />
      <span class="text-muted" style="font-size: 12px">共 {{ rows.length }} 条</span>
    </div>

    <div class="panel" v-loading="loading">
      <el-table :data="rows" style="width: 100%" empty-text="当前筛选条件下没有报名记录">
        <el-table-column label="学生" width="150">
          <template #default="{ row }">
            <div style="font-weight: 600; color: var(--ce-ink)">{{ row.studentName }}</div>
            <div class="sub">{{ row.studentId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="活动" min-width="220">
          <template #default="{ row }">
            <div>{{ row.activityTitle }}</div>
            <div class="sub">{{ row.activityLocation }} · {{ formatDateTime(row.activityStartAt) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="报名方式" width="120">
          <template #default="{ row }"><PolicyTag :type="row.policyType" /></template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button link type="success" @click="approve(row)">通过</el-button>
              <el-button link type="danger" @click="reject(row)">拒绝</el-button>
            </template>
            <span v-else class="sub">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
