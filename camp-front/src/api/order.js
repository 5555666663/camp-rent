import request from '@/utils/request'

// ============================================================
// 订单与评价接口（用户端）
// ============================================================

// 下单
export function createOrder(data) {
  return request.post('/order', data)
}

// 模拟支付
export function payOrder(id) {
  return request.post(`/order/${id}/pay`)
}

// 发起归还
export function applyReturn(id) {
  return request.post(`/order/${id}/return`)
}

// 归还确认
export function confirmReturn(id) {
  return request.post(`/order/${id}/confirm`)
}

// 我的订单分页
export function getMyOrderPage(params) {
  return request.get('/order/page', { params })
}

// 订单详情
export function getOrderDetail(id) {
  return request.get(`/order/${id}`)
}

// 新增评价
export function addComment(data) {
  return request.post('/comment', data)
}

// 食品购买（一次性售卖，无押金、无归还）
export function createFoodOrder(data) {
  return request.post('/order/food', data)
}
