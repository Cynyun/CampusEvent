<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listActivities } from '@/api/activities'
import { activityState, formatDateTime } from '@/utils/format'
import PolicyTag from '@/components/PolicyTag.vue'

const router = useRouter()
const activities = ref([])
const loading = ref(false)
const filters = reactive({ keyword: '', policyType: '' })

const openCount = computed(() => activities.value.filter((item) => item.open).length)
const approvedTotal = computed(() =>
  activities.value.reduce((sum, item) => sum + (item.approvedCount || 0), 0)
)

async function load() {
  loading.value = true
  try {
    const data = await listActivities({
      keyword: filters.keyword.trim() || undefined,
      policyType: filters.policyType || undefined,
    })
    activities.value = Array.isArray(data) ? data : []
  } catch {
    activities.value = []
  } finally {
    loading.value = false
  }
}

function reset() {
  filters.keyword = ''
  filters.policyType = ''
  load()
}

function progressPercent(activity) {
  if (!activity.capacity) return 0
  return Math.min(100, Math.round(((activity.approvedCount || 0) / activity.capacity) * 100))
}

function progressColor(percent) {
  if (percent >= 100) return '#dc2626'
  if (percent >= 80) return '#d97706'
  return '#2563eb'
}

function goDetail(id) {
  router.push('/activities/' + id)
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <div class="eyebrow">活动广场</div>
        <h1 class="page-title">发现校园活动</h1>
        <p class="page-subtitle">按名称或报名方式筛选，查看名额与报名状态，再决定是否参与。</p>
      </div>
      <div class="page-actions">
        <el-button :loading="loading" @click="load">刷新列表</el-button>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-label">活动总数</div>
        <div class="stat-value">{{ activities.length }}</div>
        <div class="stat-hint">当前筛选结果</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">报名中</div>
        <div class="stat-value">{{ openCount }}</div>
        <div class="stat-hint">尚未开始的活动</div>
      </div>
      <div class="stat-card accent">
        <div class="stat-label">已通过报名</div>
        <div class="stat-value">{{ approvedTotal }}</div>
        <div class="stat-hint">所有活动的已通过人数合计</div>
      </div>
    </div>

    <div class="panel toolbar">
      <div class="toolbar-group">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索活动名称"
          clearable
          style="width: 240px"
          @keyup.enter="load"
        />
        <el-select v-model="filters.policyType" placeholder="全部报名方式" clearable style="width: 170px" @change="load">
          <el-option label="直接报名" value="DIRECT" />
          <el-option label="年级限定" value="GRADE_LIMITED_DIRECT" />
          <el-option label="审核制" value="APPROVAL" />
        </el-select>
        <el-button type="primary" @click="load">筛选</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
      <div class="toolbar-spacer" />
      <span class="text-muted" style="font-size: 12px">共 {{ activities.length }} 个活动</span>
    </div>

    <div v-loading="loading" style="min-height: 260px">
      <div v-if="activities.length" class="activity-grid">
        <article
          v-for="activity in activities"
          :key="activity.id"
          class="activity-card"
          tabindex="0"
          @click="goDetail(activity.id)"
          @keyup.enter="goDetail(activity.id)"
        >
          <div class="card-top">
            <el-tag effect="plain" round>{{ activity.category }}</el-tag>
            <PolicyTag :type="activity.policyType" />
          </div>
          <h3>{{ activity.title }}</h3>
          <div class="meta-row">
            <div class="meta-item"><span>时间</span><span>{{ formatDateTime(activity.startAt) }}</span></div>
            <div class="meta-item"><span>地点</span><span>{{ activity.location }}</span></div>
          </div>
          <div class="capacity-line">
            <span>已通过 {{ activity.approvedCount || 0 }} / {{ activity.capacity }}</span>
            <span>剩余 {{ activity.remaining ?? activity.capacity - (activity.approvedCount || 0) }}</span>
          </div>
          <el-progress
            :percentage="progressPercent(activity)"
            :stroke-width="8"
            :show-text="false"
            :color="progressColor(progressPercent(activity))"
          />
          <div class="card-footer">
            <span class="status-dot" :class="{ open: activityState(activity) === '报名中', full: activityState(activity) === '已满员' }">{{ activityState(activity) }}</span>
            <el-button type="primary" link @click.stop="goDetail(activity.id)">查看详情 →</el-button>
          </div>
        </article>
      </div>
      <el-empty v-else-if="!loading" description="暂时没有符合条件的活动">
        <el-button @click="reset">清空筛选条件</el-button>
      </el-empty>
    </div>
  </div>
</template>
