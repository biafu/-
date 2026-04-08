import { createRouter, createWebHistory } from 'vue-router'
import { loadSessionFromStorage } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/auth/login',
  },
  {
    path: '/auth/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
  },
  {
    path: '/auth/merchant-apply',
    name: 'merchant-apply',
    component: () => import('@/views/auth/MerchantApplyView.vue'),
  },
  {
    path: '/student',
    component: () => import('@/views/student/StudentLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/student/home',
      },
      {
        path: 'home',
        name: 'student-home',
        component: () => import('@/views/student/StudentHomeView.vue'),
      },
      {
        path: 'profile',
        name: 'student-profile',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentProfileView.vue'),
      },
      {
        path: 'coupons',
        name: 'student-coupons',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentCouponsView.vue'),
      },
      {
        path: 'store/:id/seckill',
        name: 'student-store-seckill',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentSeckillView.vue'),
      },
      {
        path: 'store/:id',
        name: 'student-store',
        component: () => import('@/views/student/StudentStoreDetailView.vue'),
      },
      
      {
        path: 'cart',
        name: 'student-cart',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentCartView.vue'),
      },
      {
        path: 'checkout',
        name: 'student-checkout',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentCheckoutView.vue'),
      },
      {
        path: 'payment-result',
        name: 'student-payment-result',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentPaymentResultView.vue'),
      },
      {
        path: 'orders',
        name: 'student-orders',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentOrdersView.vue'),
      },
      {
        path: 'orders/:id',
        name: 'student-order-detail',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentOrderDetailView.vue'),
      },
      {
        path: 'messages',
        name: 'student-messages',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentMessagesView.vue'),
      },
      {
        path: 'addresses',
        name: 'student-addresses',
        meta: { requiresRole: 'STUDENT' },
        component: () => import('@/views/student/StudentAddressView.vue'),
      },
    ],
  },
  {
    path: '/delivery',
    name: 'delivery-workbench',
    meta: { requiresRole: 'DELIVERY' },
    component: () => import('@/views/delivery/DeliveryWorkbenchView.vue'),
  },
  {
    path: '/workspace',
    meta: { requiresRole: 'MERCHANT' },
    component: () => import('@/views/workspace/WorkspaceLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/workspace/dashboard',
      },
      {
        path: 'dashboard',
        name: 'workspace-dashboard',
        component: () => import('@/views/workspace/DashboardView.vue'),
      },
      {
        path: 'products',
        name: 'workspace-products',
        component: () => import('@/views/workspace/ProductsView.vue'),
      },
      {
        path: 'orders',
        name: 'workspace-orders',
        component: () => import('@/views/workspace/OrdersView.vue'),
      },
      {
        path: 'reviews',
        name: 'workspace-reviews',
        component: () => import('@/views/workspace/ReviewsView.vue'),
      },
      {
        path: 'store',
        name: 'workspace-store',
        component: () => import('@/views/workspace/StoreView.vue'),
      },
    ],
  },
  {
    path: '/admin',
    meta: { requiresRole: 'ADMIN' },
    component: () => import('@/views/admin/AdminLayout.vue'),
    children: [
      {
        path: '',
        redirect: '/admin/dashboard',
      },
      {
        path: 'dashboard',
        name: 'admin-dashboard',
        component: () => import('@/views/admin/AdminDashboardView.vue'),
      },
      {
        path: 'merchants',
        name: 'admin-merchants',
        component: () => import('@/views/admin/AdminMerchantApplicationsView.vue'),
      },
      {
        path: 'operations',
        name: 'admin-operations',
        component: () => import('@/views/admin/AdminOperationsView.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0, behavior: 'smooth' }
  },
})

router.beforeEach((to) => {
  const session = loadSessionFromStorage()
  const requiredRole = to.matched.find((it) => it.meta?.requiresRole)?.meta?.requiresRole as string | undefined

  if (to.path === '/auth/login' && session?.token) {
    if (session.role === 'MERCHANT') {
      return '/workspace/dashboard'
    }
    if (session.role === 'STUDENT') {
      return '/student/home'
    }
    if (session.role === 'ADMIN') {
      return '/admin/dashboard'
    }
    if (session.role === 'DELIVERY') {
      return '/delivery'
    }
  }

  if (!requiredRole) {
    return true
  }

  if (!session?.token) {
    return '/auth/login'
  }

  if (session.role !== requiredRole) {
    if (session.role === 'MERCHANT') {
      return '/workspace/dashboard'
    }
    if (session.role === 'STUDENT') {
      return '/student/home'
    }
    if (session.role === 'ADMIN') {
      return '/admin/dashboard'
    }
    if (session.role === 'DELIVERY') {
      return '/delivery'
    }
    return '/auth/login'
  }

  return true
})

export default router
