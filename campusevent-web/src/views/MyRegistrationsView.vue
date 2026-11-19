<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelRegistration, listMyRegistrations } from '@/api/registrations'
import { formatDateTime } from '@/utils/format'
import PolicyTag from '@/components/PolicyTag.vue'
import StatusTag from '@/components/StatusTag.vue'

const router = useRouter()
const rows = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listMyRegistrations()
    rows.value = Array.isArray(data) ? data : []
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function canCancel(row) {
  return ['PENDING', 'APPROVED'].includes(row.status)
}

async function cancel(row) {
  try {
    await ElMessageBox.confirm('取消后名额将释放，确定取消这次报名吗？', '取消报名', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await cancelRegistration(row.id)
    ElMessage.success('报名已取消')
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
        <div class="eyebrow">我的报名</div>
        <h1 class="page-title">报名记录</h1>
        <p class="page-subtitle">查看直接报名与审核制报名的处理状态，待审核和已通过都可以取消。</p>
      </div>
      <el-button :loading="loading" @click="load">刷新</el-button>
    </div>

    <div class="panel" v-loading="loading">
      <el-table :data="rows" style="width: 100%" empty-text="还没有报名记录，去活动广场看看吧">
        <el-table-column label="活动" min-width="240">
          <template #default="{ row }">
            <el-button link type="primary" style="padding: 0; font-weight: 600" @click="router.push('/activities/' + row.activityId)">
              {{ row.activityTitle }}
            </el-button>
            <div class="sub">{{ row.activityLocation }} · {{ formatDateTime(row.activityStartAt) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="报名方式" width="130">
          <template #default="{ row }"><PolicyTag :type="row.policyType" /></template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="right">
          <template #default="{ row }">
            <el-button v-if="canCancel(row)" type="danger" link @click="cancel(row)">取消报名</el-button>
            <span v-else class="sub">已结束</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
