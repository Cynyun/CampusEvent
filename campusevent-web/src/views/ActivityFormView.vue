<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createActivity, getAdminActivity, updateActivity } from '@/api/activities'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const isEdit = computed(() => Boolean(route.params.id))

const form = reactive({
  title: '',
  category: '',
  startAt: '',
  location: '',
  capacity: 30,
  policyType: 'DIRECT',
})

const rules = {
  title: [
    { required: true, message: '请输入活动名称', trigger: 'blur' },
    { max: 100, message: '活动名称不能超过 100 个字符', trigger: 'blur' },
  ],
  category: [{ required: true, message: '请输入活动分类', trigger: 'blur' }],
  startAt: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入人数上限', trigger: 'change' }],
  policyType: [{ required: true, message: '请选择报名方式', trigger: 'change' }],
}

async function load() {
  if (!isEdit.value) return
  loading.value = true
  try {
    const data = await getAdminActivity(route.params.id)
    Object.assign(form, {
      title: data.title,
      category: data.category,
      startAt: data.startAt,
      location: data.location,
      capacity: data.capacity,
      policyType: data.policyType,
    })
  } catch {
    router.replace('/admin/activities')
  } finally {
    loading.value = false
  }
}

async function save() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  const payload = {
    title: form.title.trim(),
    category: form.category.trim(),
    startAt: form.startAt,
    location: form.location.trim(),
    capacity: form.capacity,
    policyType: form.policyType,
  }
  try {
    if (isEdit.value) {
      await updateActivity(route.params.id, payload)
      ElMessage.success('活动已更新')
    } else {
      await createActivity(payload)
      ElMessage.success('活动已创建')
    }
    router.push('/admin/activities')
  } catch {
    // http.js 已提示
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div>
        <div class="eyebrow">管理端 · 活动</div>
        <h1 class="page-title">{{ isEdit ? '编辑活动' : '新建活动' }}</h1>
        <p class="page-subtitle">活动开始时间必须晚于当前时间；容量不能低于已通过人数。</p>
      </div>
      <el-button @click="router.push('/admin/activities')">返回列表</el-button>
    </div>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="96px" class="form-card">
      <el-form-item label="活动名称" prop="title">
        <el-input v-model="form.title" maxlength="100" show-word-limit placeholder="例如：校园开放日志愿者招募" />
      </el-form-item>
      <el-form-item label="活动分类" prop="category">
        <el-input v-model="form.category" maxlength="40" placeholder="例如：志愿服务 / 学术讲座 / 文体活动" />
      </el-form-item>
      <el-form-item label="开始时间" prop="startAt">
        <el-date-picker
          v-model="form.startAt"
          type="datetime"
          value-format="YYYY-MM-DDTHH:mm:ss"
          format="YYYY-MM-DD HH:mm"
          placeholder="选择活动开始时间"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="活动地点" prop="location">
        <el-input v-model="form.location" maxlength="100" placeholder="例如：大学生活动中心 301" />
      </el-form-item>
      <el-form-item label="人数上限" prop="capacity">
        <el-input-number v-model="form.capacity" :min="1" :max="10000" :step="5" style="width: 100%" />
        <div class="form-hint">仅统计已通过报名；审核中的申请不占用名额。</div>
      </el-form-item>
      <el-form-item label="报名方式" prop="policyType">
        <el-radio-group v-model="form.policyType">
          <el-radio-button value="DIRECT">直接报名</el-radio-button>
          <el-radio-button value="GRADE_LIMITED_DIRECT">年级限定</el-radio-button>
          <el-radio-button value="APPROVAL">审核制</el-radio-button>
        </el-radio-group>
        <div class="form-hint">
          {{ form.policyType === 'APPROVAL'
            ? '学生提交后进入待审核，管理员通过后才占用名额。'
            : form.policyType === 'GRADE_LIMITED_DIRECT'
              ? '仅大二、大三学生可直接报名；提交后立即通过并占用名额，其他年级会被拒绝。'
              : '学生提交后立即通过，名额满时无法提交。' }}
        </div>
      </el-form-item>
      <div class="form-actions">
        <el-button @click="router.push('/admin/activities')">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存活动</el-button>
      </div>
    </el-form>
  </div>
</template>
