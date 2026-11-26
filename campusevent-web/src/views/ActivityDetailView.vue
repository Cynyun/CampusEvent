<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivity } from '@/api/activities'
import { cancelRegistration, registerForActivity } from '@/api/registrations'
import { activityState, formatDateTime, policyText, statusText } from '@/utils/format'
import PolicyTag from '@/components/PolicyTag.vue'

const route = useRoute()
const router = useRouter()
const activity = ref(null)
const loading = ref(false)
const registering = ref(false)
const canceling = ref(false)

const hasActiveRegistration = computed(() =>
  ['PENDING', 'APPROVED'].includes(activity.value?.myRegistrationStatus)
)

const isDirectLike = computed(() =>
  ['DIRECT', 'GRADE_LIMITED_DIRECT'].includes(activity.value?.policyType)
)

const canRegister = computed(() => {
  const item = activity.value
  if (!item || !item.open || hasActiveRegistration.value) return false
  if (isDirectLike.value) return Boolean(item.hasCapacity)
  return true
})

const signupText = computed(() => {
  const item = activity.value
  if (!item) return '加载中'
  if (!item.open) return '活动已开始'
  if (hasActiveRegistration.value) return '已提交报名'
  if (isDirectLike.value && !item.hasCapacity) return '名额已满'
  return item.policyType === 'APPROVAL' ? '提交报名申请' : '立即报名'
})

const progress = computed(() => {
  const item = activity.value
  if (!item || !item.capacity) return 0
  return Math.min(100, Math.round(((item.approvedCount || 0) / item.capacity) * 100))
})

const remaining = computed(() => {
  const item = activity.value
  if (!item) return 0
  return item.remaining ?? Math.max(0, item.capacity - (item.approvedCount || 0))
})

async function load() {
  loading.value = true
  try {
    activity.value = await getActivity(route.params.id)
  } catch {
    router.replace('/activities')
  } finally {
    loading.value = false
  }
}

async function register() {
  if (!canRegister.value) return
  registering.value = true
  try {
    await registerForActivity(activity.value.id)
    ElMessage.success(activity.value.policyType === 'APPROVAL' ? '报名申请已提交，等待管理员审核' : '报名成功')
    await load()
  } catch {
    // http.js 已提示
  } finally {
    registering.value = false
  }
}

async function cancel() {
  if (!activity.value?.myRegistrationId) {
    ElMessage.warning('未找到报名记录，请到“我的报名”中操作')
    return
  }
  try {
    await ElMessageBox.confirm('取消后名额将释放，确定取消这次报名吗？', '取消报名', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning',
    })
  } catch {
    return
  }
  canceling.value = true
  try {
    await cancelRegistration(activity.value.myRegistrationId)
    ElMessage.success('报名已取消')
    await load()
  } catch {
    // http.js 已提示
  } finally {
    canceling.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page" v-loading="loading">
    <div>
      <el-button link type="primary" @click="router.push('/activities')">← 返回活动列表</el-button>
    </div>

    <div v-if="activity" class="detail-grid">
      <section class="panel detail-main">
        <div class="detail-head">
          <div>
            <div class="card-top" style="justify-content: flex-start; gap: 8px">
              <el-tag effect="plain" round>{{ activity.category }}</el-tag>
              <span class="status-dot" :class="{ open: activityState(activity) === '报名中', full: activityState(activity) === '已满员' }">{{ activityState(activity) }}</span>
            </div>
            <h1>{{ activity.title }}</h1>
            <p>活动编号 #{{ activity.id }} · 由管理员创建并维护</p>
          </div>
          <PolicyTag :type="activity.policyType" />
        </div>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="开始时间">{{ formatDateTime(activity.startAt) }}</el-descriptions-item>
          <el-descriptions-item label="活动地点">{{ activity.location }}</el-descriptions-item>
          <el-descriptions-item label="人数上限">{{ activity.capacity }} 人</el-descriptions-item>
          <el-descriptions-item label="报名方式">{{ policyText(activity.policyType) }}</el-descriptions-item>
        </el-descriptions>

        <div class="capacity-block">
          <div class="capacity-head">
            <span>名额使用情况</span>
            <span>已通过 {{ activity.approvedCount || 0 }} / {{ activity.capacity }}，待审核 {{ activity.pendingCount || 0 }}</span>
          </div>
          <el-progress :percentage="progress" :stroke-width="10" :color="progress >= 100 ? '#dc2626' : '#2563eb'" />
        </div>

        <div>
          <h3 style="margin: 0 0 10px; color: var(--ce-ink)">报名规则</h3>
          <p class="page-subtitle" style="margin: 0">
            {{ activity.policyType === 'DIRECT'
              ? '直接报名：提交后立即通过并占用名额；名额已满时无法提交，需要等待已通过的学生取消。'
              : activity.policyType === 'GRADE_LIMITED_DIRECT'
                ? '年级限定直接报名：仅大二、大三学生可提交；提交后立即通过并占用名额，其他年级会被拒绝。'
                : '审核制报名：提交后进入待审核，管理员通过后才占用名额；待审核不占名额，满员时仍可提交申请。' }}
          </p>
        </div>
      </section>

      <aside class="panel signup-panel">
        <div class="signup-label">报名入口</div>
        <div class="signup-count">
          <strong>{{ remaining }}</strong>
          <span>剩余名额</span>
        </div>

        <el-alert
          v-if="hasActiveRegistration"
          :type="activity.myRegistrationStatus === 'APPROVED' ? 'success' : 'warning'"
          :closable="false"
          show-icon
          :title="'当前报名状态：' + statusText(activity.myRegistrationStatus)"
          description="如需更换活动，请先取消当前报名。"
        />
        <el-alert
          v-else-if="!activity.open"
          type="info"
          :closable="false"
          show-icon
          title="活动已开始，报名通道关闭"
        />
        <el-alert
          v-else-if="isDirectLike && !activity.hasCapacity"
          type="error"
          :closable="false"
          show-icon
          title="直接报名名额已满"
          description="审核制报名在满员时仍可提交，直接报名则需等待名额释放。"
        />

        <el-button type="primary" size="large" :disabled="!canRegister" :loading="registering" @click="register">
          {{ signupText }}
        </el-button>
        <el-button
          v-if="hasActiveRegistration"
          type="danger"
          plain
          size="large"
          :loading="canceling"
          @click="cancel"
        >
          取消报名
        </el-button>

        <p class="hint">
          直接报名提交即通过；审核制报名先进入待审核，管理员通过后才占用名额。待审核期间可以取消。
        </p>
      </aside>
    </div>
  </div>
</template>