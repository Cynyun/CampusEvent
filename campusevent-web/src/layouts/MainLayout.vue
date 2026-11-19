<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getUnreadCount } from '@/api/messages'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const unread = ref(0)

const roleText = computed(() => (auth.isAdmin ? '管理员' : '学生'))
const menus = computed(() =>
  auth.isAdmin
    ? [
        { path: '/admin/activities', label: '活动管理' },
        { path: '/admin/registrations', label: '报名审核' },
        { path: '/admin/statistics', label: '数据统计' },
      ]
    : [
        { path: '/activities', label: '活动广场' },
        { path: '/my-registrations', label: '我的报名' },
        { path: '/messages', label: '站内通知' },
      ]
)
const activeMenu = computed(() => {
  if (route.path.startsWith('/admin/activities')) return '/admin/activities'
  if (route.path.startsWith('/activities')) return '/activities'
  return route.path
})

async function refreshUnread() {
  if (auth.isAdmin) return
  try {
    unread.value = await getUnreadCount()
  } catch {
    unread.value = 0
  }
}

onMounted(refreshUnread)
watch(() => route.fullPath, refreshUnread)

async function handleCommand(command) {
  if (command !== 'logout') return
  try {
    await ElMessageBox.confirm('退出后需要重新登录，确定退出吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  await auth.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="header-inner">
        <button class="brand" type="button" @click="router.push(auth.homePath)">
          <span class="brand-mark">CE</span>
          <span class="brand-text">
            <strong>CampusEvent</strong>
            <small>校园活动平台</small>
          </span>
        </button>

        <el-menu mode="horizontal" :default-active="activeMenu" :ellipsis="false" router class="app-nav">
          <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
            {{ item.label }}
            <el-badge
              v-if="item.path === '/messages' && unread > 0"
              :value="unread"
              :max="99"
              class="menu-badge"
            />
          </el-menu-item>
        </el-menu>

        <div class="header-user">
          <el-tag size="small" effect="plain" round class="role-tag">{{ roleText }}</el-tag>
          <el-dropdown trigger="click" @command="handleCommand">
            <button class="user-button" type="button">
              {{ auth.user?.name || '未登录' }}
              <span class="caret">▾</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>账号：{{ auth.user?.username || auth.user?.id }}</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="app-main">
      <router-view v-slot="{ Component }">
        <transition name="page-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <footer class="app-footer">CampusEvent · 校园活动报名与管理系统</footer>
  </div>
</template>
