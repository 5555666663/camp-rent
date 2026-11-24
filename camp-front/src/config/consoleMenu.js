/**
 * 后台控制台菜单与角色权限配置
 * 三类角色共用同一套「左侧菜单 + 右侧内容」布局，菜单项根据登录用户角色动态渲染。
 * 路由守卫会再次校验 meta.roles，避免直接输入 URL 越权访问。
 */

/** 角色编码（与后端 role.role_code 一致） */
export const ROLE_ADMIN = 'ROLE_ADMIN'
export const ROLE_CS = 'ROLE_CS'
export const ROLE_STORAGE = 'ROLE_STORAGE'

/** 全部菜单项定义：path / title / icon / roles */
export const CONSOLE_MENUS = [
  // ---------------- 管理员 ----------------
  { path: '/admin/stats', title: '数据统计', icon: 'DataAnalysis', roles: [ROLE_ADMIN] },
  { path: '/admin/store', title: '网点管理', icon: 'MapLocation', roles: [ROLE_ADMIN] },
  { path: '/admin/category', title: '分类管理', icon: 'Menu', roles: [ROLE_ADMIN] },
  { path: '/admin/equipment', title: '商品管理', icon: 'Goods', roles: [ROLE_ADMIN] },
  { path: '/admin/package', title: '套餐管理', icon: 'Box', roles: [ROLE_ADMIN] },
  { path: '/admin/maintain', title: '维保管理', icon: 'Tools', roles: [ROLE_ADMIN] },
  { path: '/admin/order', title: '订单管理', icon: 'Tickets', roles: [ROLE_ADMIN] },
  { path: '/admin/comment', title: '评价管理', icon: 'ChatDotSquare', roles: [ROLE_ADMIN] },
  { path: '/admin/user', title: '账号权限管理', icon: 'UserFilled', roles: [ROLE_ADMIN] },
  { path: '/admin/warn', title: '库存预警', icon: 'Warning', roles: [ROLE_ADMIN] },
  { path: '/admin/rule', title: '推荐规则', icon: 'SetUp', roles: [ROLE_ADMIN] },
  { path: '/admin/log', title: '系统操作日志', icon: 'Document', roles: [ROLE_ADMIN] },

  // ---------------- 客服 ----------------
  { path: '/cs/order', title: '订单工作台', icon: 'Tickets', roles: [ROLE_CS] },
  { path: '/cs/comment', title: '评价管理', icon: 'ChatDotSquare', roles: [ROLE_CS] },
  { path: '/cs/message', title: '用户咨询消息', icon: 'ChatLineRound', roles: [ROLE_CS] },

  // ---------------- 仓储人员 ----------------
  { path: '/storage/stock', title: '装备库存盘点', icon: 'Files', roles: [ROLE_STORAGE] },
  { path: '/storage/maintain', title: '装备维保管理', icon: 'Tools', roles: [ROLE_STORAGE] },
  { path: '/storage/batch', title: '库存变更记录', icon: 'Tickets', roles: [ROLE_STORAGE] }
]

/** 菜单所属分组标题（用于侧栏分节显示） */
export const GROUP_TITLES = {
  [ROLE_ADMIN]: '管理员控制台',
  [ROLE_CS]: '客服工作台',
  [ROLE_STORAGE]: '仓储工作台'
}

/** 角色中文名 */
export const ROLE_LABELS = {
  ROLE_ADMIN: '超级管理员',
  ROLE_CS: '客服人员',
  ROLE_STORAGE: '仓储人员'
}

/** 取当前用户可访问的角色标识（管理员优先） */
export function primaryRole(roles = [], isAdmin = false) {
  if (isAdmin || roles.includes(ROLE_ADMIN)) return ROLE_ADMIN
  if (roles.includes(ROLE_CS)) return ROLE_CS
  if (roles.includes(ROLE_STORAGE)) return ROLE_STORAGE
  return ROLE_ADMIN
}

/** 按角色过滤菜单 */
export function menusForRole(role) {
  return CONSOLE_MENUS.filter((m) => m.roles.includes(role))
}

/** 角色默认落地页 */
export function homePathForRole(role) {
  if (role === ROLE_CS) return '/cs/order'
  if (role === ROLE_STORAGE) return '/storage/stock'
  return '/admin/stats'
}
