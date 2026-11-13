import request from '@/utils/request'

// ============================================================
// 管理端接口
// ============================================================

// ---------- 网点管理 ----------
export function getStorePage(params) {
  return request.get('/admin/store/page', { params })
}
export function getAllStores() {
  return request.get('/admin/store/list')
}
export function addStore(data) {
  return request.post('/admin/store', data)
}
export function updateStore(data) {
  return request.put('/admin/store', data)
}
export function deleteStore(id) {
  return request.delete(`/admin/store/${id}`)
}
export function updateStoreStatus(id, status) {
  return request.put(`/admin/store/${id}/status`, null, { params: { status } })
}

// ---------- 分类管理 ----------
export function getCategoryPage(params) {
  return request.get('/admin/category/page', { params })
}
export function addCategory(data) {
  return request.post('/admin/category', data)
}
export function updateCategory(data) {
  return request.put('/admin/category', data)
}
export function deleteCategory(id) {
  return request.delete(`/admin/category/${id}`)
}

// ---------- 装备管理 ----------
export function getEquipmentAdminPage(params) {
  return request.get('/admin/equipment/page', { params })
}
export function getAllEquipments() {
  return request.get('/admin/equipment/list')
}
export function addEquipment(data) {
  return request.post('/admin/equipment', data)
}
export function updateEquipment(data) {
  return request.put('/admin/equipment', data)
}
export function deleteEquipment(id) {
  return request.delete(`/admin/equipment/${id}`)
}
export function updateEquipmentStatus(id, status) {
  return request.put(`/admin/equipment/${id}/status`, null, { params: { status } })
}

// ---------- 维保管理 ----------
export function getMaintainPage(params) {
  return request.get('/admin/maintain/page', { params })
}
export function addMaintain(data) {
  return request.post('/admin/maintain', data)
}

// ---------- 订单管理 ----------
export function getAdminOrderPage(params) {
  return request.get('/admin/order/page', { params })
}
export function markOrderOverdue(id) {
  return request.post(`/admin/order/${id}/overdue`)
}
export function verifyOrderReturn(id) {
  return request.post(`/admin/order/${id}/verify`)
}
export function updateOrderRemark(id, remark) {
  return request.put(`/admin/order/${id}/remark`, { remark })
}

// ---------- 评价管理 ----------
export function getCommentPage(params) {
  return request.get('/admin/comment/page', { params })
}
export function deleteComment(id) {
  return request.delete(`/admin/comment/${id}`)
}

// ---------- 数据统计 ----------
export function getOrderByStore() {
  return request.get('/admin/stats/orderByStore')
}
export function getEquipmentRent() {
  return request.get('/admin/stats/equipmentRent')
}
export function getOrderTrend() {
  return request.get('/admin/stats/orderTrend')
}
