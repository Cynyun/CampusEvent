import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, me } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)

  const isLoggedIn = computed(() => Boolean(user.value))
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const homePath = computed(() => (isAdmin.value ? '/admin/activities' : '/activities'))

  async function fetchMe() {
    loading.value = true
    try {
      user.value = await me()
    } catch {
      user.value = null
    } finally {
      loading.value = false
    }
    return user.value
  }

  async function login(payload) {
    loading.value = true
    try {
      user.value = await loginApi(payload)
      return user.value
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      user.value = null
    }
  }

  return { user, loading, isLoggedIn, isAdmin, homePath, fetchMe, login, logout }
})
