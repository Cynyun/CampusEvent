<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
</script>

<template>
  <div class="page" style="padding-top: 60px">
    <el-result icon="warning" title="页面不存在" sub-title="链接可能已失效，或者你没有访问该页面的权限。">
      <template #extra>
        <el-button type="primary" @click="router.push(auth.homePath)">回到首页</el-button>
      </template>
    </el-result>
  </div>
</template>
