import request from '@/utils/request'

// ============================================================
// 客服工作台接口（ROLE_CS / ROLE_ADMIN）
// ============================================================

// 全部订单分页
export function getCsOrderPage(params) {
  return request.get('/cs/order/page', { params })
}

// 核验归还
export function csVerifyReturn(id) {
  return request.post(`/cs/order/${id}/verify`)
}

// 标记逾期
export function csMarkOverdue(id) {
  return request.post(`/cs/order/${id}/overdue`)
}

// 修改订单状态
export function csUpdateOrderStatus(id, orderStatus) {
  return request.put(`/cs/order/${id}/status`, null, { params: { orderStatus } })
}

// 售后：评价分页
export function getCsCommentPage(params) {
  return request.get('/cs/comment/page', { params })
}

// 售后：删除评价
export function csDeleteComment(id) {
  return request.delete(`/cs/comment/${id}`)
}

// 咨询消息分页
export function getCsMessagePage(params) {
  return request.get('/cs/message/page', { params })
}

// 回复咨询消息
export function csReplyMessage(id, reply) {
  return request.put(`/cs/message/${id}/reply`, { reply })
}

// ============================================================
// 用户管理接口（超级管理员）
// ============================================================

// 用户分页
export function getAdminUserPage(params) {
  return request.get('/admin/user/page', { params })
}

// 新增客服账号
export function addCsAccount(data) {
  return request.post('/admin/user/cs', data)
}

// 启用 / 禁用账号
export function updateUserStatus(id, status) {
  return request.put(`/admin/user/${id}/status`, null, { params: { status } })
}
