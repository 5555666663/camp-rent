import request from '@/utils/request'

/**
 * 提交咨询（用户端）
 * @param {{title: string, content: string}} data
 */
export function submitConsult(data) {
  return request.post('/consult', data)
}

/**
 * 我的咨询记录分页（用户端，仅返回本人数据）
 * @param {{current?: number, size?: number}} params
 */
export function getMyConsults(params) {
  return request.get('/consult/my', { params })
}

/**
 * 客服：咨询列表分页
 * @param {{current?: number, size?: number, status?: number|null}} params
 *        status 传 null 表示全部，0 待处理，1 已完成
 */
export function getCsConsultPage(params) {
  return request.get('/cs/message/page', { params })
}

/**
 * 客服：回复咨询
 * @param {number} id 咨询 id
 * @param {string} reply 回复内容
 */
export function replyConsult(id, reply) {
  return request.put(`/cs/message/${id}/reply`, { reply })
}
