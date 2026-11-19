import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/activities' },
      {
        path: 'activities',
        name: 'activities',
        component: () => import('@/views/ActivityListView.vue'),
        meta: { role: 'STUDENT' },
      },
      {
        path: 'activities/:id',
        name: 'activity-detail',
        component: () => import('@/views/ActivityDetailView.vue'),
        meta: { role: 'STUDENT' },
      },
      {
        path: 'my-registrations',
        name: 'my-registrations',
        component: () => import('@/views/MyRegistrationsView.vue'),
        meta: { role: 'STUDENT' },
      },
      {
        path: 'messages',
        name: 'messages',
        component: () => import('@/views/MessagesView.vue'),
        meta: { role: 'STUDENT' },
      },
      {
        path: 'admin/activities',
        name: 'admin-activities',
        component: () => import('@/views/AdminActivityListView.vue'),
        meta: { role: 'ADMIN' },
      },
      {
        path: 'admin/activities/new',
        name: 'admin-activity-new',
        component: () => import('@/views/ActivityFormView.vue'),
        meta: { role: 'ADMIN' },
      },
      {
        path: 'admin/activities/:id/edit',
        name: 'admin-activity-edit',
        component: () => import('@/views/ActivityFormView.vue'),
        meta: { role: 'ADMIN' },
      },
      {
        path: 'admin/registrations',
        name: 'admin-registrations',
        component: () => import('@/views/AdminRegistrationView.vue'),
        meta: { role: 'ADMIN' },
      },
      {
        path: 'admin/statistics',
        name: 'admin-statistics',
        component: () => import('@/views/AdminStatisticsView.vue'),
        meta: { role: 'ADMIN' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { public: true },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (to.meta.public) {
    if (to.name === 'login' && auth.user) {
      return auth.homePath
    }
    return true
  }

  if (!auth.user) {
    await auth.fetchMe()
  }
  if (!auth.user) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.role && auth.user.role !== to.meta.role) {
    return auth.homePath
  }
  return true
})

export default router
