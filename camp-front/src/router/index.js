import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    children: [
      { path: '', redirect: '/equipment' },
      { path: 'map', name: 'Map', component: () => import('@/views/MapView.vue'), meta: { title: '网点地图' } },
      { path: 'nearest', name: 'NearestStore', component: () => import('@/views/NearestStore.vue'), meta: { title: '就近网点' } },
      { path: 'store/:id', name: 'StoreDetail', component: () => import('@/views/StoreDetail.vue'), meta: { title: '网点详情' } },
      { path: 'equipment', name: 'Equipment', component: () => import('@/views/EquipmentList.vue'), meta: { title: '装备浏览' } },
      { path: 'equipment/:id', name: 'EquipmentDetail', component: () => import('@/views/EquipmentDetail.vue'), meta: { title: '装备详情' } },
      { path: 'recommend', name: 'WeatherRecommend', component: () => import('@/views/WeatherRecommend.vue'), meta: { title: '露营装备智能推荐' } },
      { path: 'package/:id', name: 'PackageDetail', component: () => import('@/views/PackageDetail.vue'), meta: { title: '套餐详情' } },
      { path: 'order', name: 'OrderCreate', component: () => import('@/views/OrderCreate.vue'), meta: { title: '租赁下单', requiresAuth: true } },
      { path: 'my-orders', name: 'MyOrders', component: () => import('@/views/MyOrders.vue'), meta: { title: '我的订单', requiresAuth: true } },
      { path: 'messages', name: 'MessageCenter', component: () => import('@/views/MessageCenter.vue'), meta: { title: '消息中心', requiresAuth: true } }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', redirect: '/admin/stats' },
      { path: 'stats', name: 'AdminStats', component: () => import('@/views/admin/Stats.vue'), meta: { title: '数据统计' } },
      { path: 'store', name: 'AdminStore', component: () => import('@/views/admin/StoreManage.vue'), meta: { title: '网点管理' } },
      { path: 'category', name: 'AdminCategory', component: () => import('@/views/admin/CategoryManage.vue'), meta: { title: '分类管理' } },
      { path: 'equipment', name: 'AdminEquipment', component: () => import('@/views/admin/EquipmentManage.vue'), meta: { title: '装备管理' } },
      { path: 'maintain', name: 'AdminMaintain', component: () => import('@/views/admin/MaintainManage.vue'), meta: { title: '维保管理' } },
      { path: 'order', name: 'AdminOrder', component: () => import('@/views/admin/OrderManage.vue'), meta: { title: '订单管理' } },
      { path: 'comment', name: 'AdminComment', component: () => import('@/views/admin/CommentManage.vue'), meta: { title: '评价管理' } },
      { path: 'user', name: 'AdminUser', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'rule', name: 'AdminRule', component: () => import('@/views/admin/RuleManage.vue'), meta: { title: '推荐规则' } },
      { path: 'warn', name: 'AdminWarn', component: () => import('@/views/admin/WarnManage.vue'), meta: { title: '库存预警' } },
      { path: 'log', name: 'AdminLog', component: () => import('@/views/admin/LogManage.vue'), meta: { title: '操作日志' } }
    ]
  },
  {
    path: '/storage',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresStorage: true },
    children: [
      { path: '', name: 'StorageWorkbench', component: () => import('@/views/storage/StorageWorkbench.vue'), meta: { title: '仓储工作台' } }
    ]
  },
  {
    path: '/cs',
    component: () => import('@/views/cs/CSWorkbench.vue'),
    meta: { title: '客服工作台', requiresAuth: true, requiresCs: true }
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局路由守卫：未登录拦截、管理员权限拦截
router.beforeEach((to) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '露营装备租赁系统'
  const userStore = useUserStore()
  if (to.meta.requiresAuth && !userStore.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return { path: '/' }
  }
  if (to.meta.requiresStorage && !(userStore.roles || []).includes('ROLE_STORAGE') && !userStore.isAdmin) {
    return { path: '/' }
  }
  // RBAC：客服工作台仅客服 / 超级管理员可进入
  if (to.meta.requiresCs && !(userStore.isCs || userStore.isAdmin)) {
    return { path: '/' }
  }
  return true
})

export default router
