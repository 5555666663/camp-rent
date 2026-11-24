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
    path: '/403',
    name: 'NoAuth',
    component: () => import('@/views/NoAuth.vue'),
    meta: { title: '无访问权限' }
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
      { path: 'messages', name: 'MessageCenter', component: () => import('@/views/MessageCenter.vue'), meta: { title: '消息中心', requiresAuth: true } },
      { path: 'consult', name: 'Consult', component: () => import('@/views/ConsultChat.vue'), meta: { title: '在线咨询', requiresAuth: true } }
    ]
  },

  // ============================================================
  // 管理端控制台：管理员 / 客服 / 仓储共用同一布局，菜单按角色渲染
  // ============================================================
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_ADMIN'] },
    children: [
      { path: '', redirect: '/admin/stats' },
      { path: 'stats', name: 'AdminStats', component: () => import('@/views/admin/Stats.vue'), meta: { title: '数据统计', roles: ['ROLE_ADMIN'] } },
      { path: 'store', name: 'AdminStore', component: () => import('@/views/admin/StoreManage.vue'), meta: { title: '网点管理', roles: ['ROLE_ADMIN'] } },
      { path: 'category', name: 'AdminCategory', component: () => import('@/views/admin/CategoryManage.vue'), meta: { title: '分类管理', roles: ['ROLE_ADMIN'] } },
      { path: 'equipment', name: 'AdminEquipment', component: () => import('@/views/admin/EquipmentManage.vue'), meta: { title: '商品管理', roles: ['ROLE_ADMIN'] } },
      { path: 'package', name: 'AdminPackage', component: () => import('@/views/admin/PackageManage.vue'), meta: { title: '套餐管理', roles: ['ROLE_ADMIN'] } },
      { path: 'maintain', name: 'AdminMaintain', component: () => import('@/views/admin/MaintainManage.vue'), meta: { title: '维保管理', roles: ['ROLE_ADMIN'] } },
      { path: 'order', name: 'AdminOrder', component: () => import('@/views/admin/OrderManage.vue'), meta: { title: '订单管理', roles: ['ROLE_ADMIN'] } },
      { path: 'comment', name: 'AdminComment', component: () => import('@/views/admin/CommentManage.vue'), meta: { title: '评价管理', roles: ['ROLE_ADMIN'] } },
      { path: 'user', name: 'AdminUser', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '账号权限管理', roles: ['ROLE_ADMIN'] } },
      { path: 'warn', name: 'AdminWarn', component: () => import('@/views/admin/WarnManage.vue'), meta: { title: '库存预警', roles: ['ROLE_ADMIN'] } },
      { path: 'rule', name: 'AdminRule', component: () => import('@/views/admin/RuleManage.vue'), meta: { title: '推荐规则', roles: ['ROLE_ADMIN'] } },
      { path: 'log', name: 'AdminLog', component: () => import('@/views/admin/LogManage.vue'), meta: { title: '系统操作日志', roles: ['ROLE_ADMIN'] } }
    ]
  },
  {
    path: '/cs',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_CS', 'ROLE_ADMIN'] },
    children: [
      { path: '', redirect: '/cs/order' },
      { path: 'order', name: 'CsOrder', component: () => import('@/views/cs/CsOrder.vue'), meta: { title: '订单工作台', roles: ['ROLE_CS', 'ROLE_ADMIN'] } },
      { path: 'comment', name: 'CsComment', component: () => import('@/views/cs/CsComment.vue'), meta: { title: '评价管理', roles: ['ROLE_CS', 'ROLE_ADMIN'] } },
      { path: 'message', name: 'CsChat', component: () => import('@/views/cs/CsChat.vue'), meta: { title: '用户咨询消息', roles: ['ROLE_CS', 'ROLE_ADMIN'] } }
    ]
  },
  {
    path: '/storage',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ROLE_STORAGE', 'ROLE_ADMIN'] },
    children: [
      { path: '', redirect: '/storage/stock' },
      { path: 'stock', name: 'StorageStock', component: () => import('@/views/storage/StockCheck.vue'), meta: { title: '装备库存盘点', roles: ['ROLE_STORAGE', 'ROLE_ADMIN'] } },
      { path: 'maintain', name: 'StorageMaintain', component: () => import('@/views/storage/MaintainRecord.vue'), meta: { title: '装备维保管理', roles: ['ROLE_STORAGE', 'ROLE_ADMIN'] } },
      { path: 'batch', name: 'StorageBatch', component: () => import('@/views/storage/StockBatch.vue'), meta: { title: '库存变更记录', roles: ['ROLE_STORAGE', 'ROLE_ADMIN'] } }
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 角色默认落地页 */
function homePathForRole(userStore) {
  if (userStore.isAdmin) return '/admin/stats'
  const roles = userStore.roles || []
  if (roles.includes('ROLE_CS')) return '/cs/order'
  if (roles.includes('ROLE_STORAGE')) return '/storage/stock'
  return '/equipment'
}

// 全局路由守卫：登录校验 + 角色权限校验（越权跳 403，而非静默回首页）
router.beforeEach((to) => {
  document.title = (to.meta.title ? to.meta.title + ' - ' : '') + '露营装备租赁系统'
  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.isLogin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!userStore.isLogin) return true

  // 逐级收集路由 meta.roles（父级与子级任一要求即生效）
  const required = []
  to.matched.forEach((m) => {
    if (m.meta && Array.isArray(m.meta.roles)) required.push(...m.meta.roles)
  })

  if (required.length) {
    const roles = userStore.roles || []
    const allow = userStore.isAdmin || required.some((r) => roles.includes(r))
    if (!allow) {
      return { path: '/403', query: { from: to.fullPath } }
    }
  }
  return true
})

export { homePathForRole }
export default router
