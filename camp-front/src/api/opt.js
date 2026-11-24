import request from '@/utils/request'

/* ================= 支付沙箱 ================= */
export function createPay(data) {
  return request.post('/pay/create', data)
}

export function mockPay(payNo) {
  return request.get('/pay/mock/' + payNo)
}

export function getPayInfo(orderId) {
  return request.get('/pay/' + orderId)
}

export function refundDeposit(orderId) {
  return request.post('/pay/' + orderId + '/refund')
}

/* ================= 文件上传（定损照片） ================= */
export function uploadImage(file) {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/file/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}

/* ================= 站内消息 ================= */
export function getMessagePage(params) {
  return request.get('/message/page', { params })
}

export function getUnreadCount() {
  return request.get('/message/unread-count')
}

export function readMessage(id) {
  return request.put('/message/' + id + '/read')
}

export function readAllMessages() {
  return request.put('/message/read-all')
}

/* ================= 推荐规则 ================= */
export function getRuleList() {
  return request.get('/admin/rule/list')
}

export function addRule(data) {
  return request.post('/admin/rule', data)
}

export function updateRule(data) {
  return request.put('/admin/rule', data)
}

export function deleteRule(id) {
  return request.delete('/admin/rule/' + id)
}

export function toggleRule(id, enabled) {
  return request.put('/admin/rule/' + id + '/status', null, { params: { enabled } })
}

export function testRule(weather) {
  return request.post('/admin/rule/test', weather)
}

/* ================= 操作日志 ================= */
export function getLogPage(params) {
  return request.get('/admin/log/page', { params })
}

export function getLogDetail(id) {
  return request.get('/admin/log/' + id)
}

/* ================= 库存 / 保质期预警 ================= */
export function getStockWarn() {
  return request.get('/admin/warn/stock')
}

export function getExpireWarn() {
  return request.get('/admin/warn/expire')
}

/* ================= Excel 导出 ================= */
export function downloadExport(type) {
  return request.get('/admin/export/' + type, { responseType: 'blob' })
}

/* ================= 仓储工作台 ================= */
export function getStorageEquipmentPage(params) {
  return request.get('/storage/equipment/page', { params })
}

export function equipmentInbound(data) {
  return request.post('/storage/inbound', data)
}

export function getBatchPage(params) {
  return request.get('/storage/batch/page', { params })
}

export function addMaintainByStorage(data) {
  return request.post('/storage/maintain', data)
}

/* ================= 订单时段可用性 / 定损 ================= */
export function checkAvailable(params) {
  return request.post('/order/check-available', null, { params })
}

export function submitDamage(orderId, data) {
  return request.post('/order/' + orderId + '/damage', data)
}

export function getDamage(orderId) {
  return request.get('/order/' + orderId + '/damage')
}
