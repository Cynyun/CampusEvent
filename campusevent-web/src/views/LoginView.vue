<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

function fill(username) {
  form.username = username
  form.password = '123456'
}

async function submit() {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await auth.login({ username: form.username.trim(), password: form.password })
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.replace(redirect || auth.homePath)
  } catch {
    // http.js 已提示具体错误
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <section class="auth-side">
      <div class="auth-brand">CampusEvent · 校园活动平台</div>
      <h1>把校园活动，<br />组织得井井有条。</h1>
      <p>浏览活动、直接报名或提交申请，管理员统一审核，站内通知实时反馈。一个账号，走完从发现到参与的每一步。</p>
      <ul class="auth-points">
        <li>活动按名称与报名方式快速筛选</li>
        <li>直接报名、年级限定与审核制三种模式</li>
        <li>报名状态、审批结果、名额变化全程可查</li>
      </ul>
    </section>

    <section class="auth-panel">
      <div class="auth-card">
        <h2>登录</h2>
        <p>使用演示账号进入学生端或管理端。</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item label="账号" prop="username">
            <el-input v-model="form.username" size="large" placeholder="student 或 admin" clearable />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
          </el-form-item>
          <el-button class="auth-submit" type="primary" size="large" :loading="loading" @click="submit">
            登录
          </el-button>
        </el-form>
        <div class="demo-box">
          <span>演示账号（点击填入）</span>
          <el-button link type="primary" @click="fill('student')">学生：student / 123456</el-button>
          <el-button link type="primary" @click="fill('admin')">管理员：admin / 123456</el-button>
        </div>
      </div>
    </section>
  </div>
</template>
