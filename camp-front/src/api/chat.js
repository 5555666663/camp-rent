import request from '@/utils/request'

/** 取当前用户的客服会话（不存在则自动创建） */
export function getMySession() {
  return request.get('/chat/my')
}

/** 会话消息列表（用户侧，读取后自动已读） */
export function getChatMessages(sessionId) {
  return request.get('/chat/messages', { params: { sessionId } })
}

/** 用户发送消息 */
export function sendChatMessage(content) {
  return request.post('/chat/send', { content })
}

/** 用户未读数 */
export function getChatUnread() {
  return request.get('/chat/unread')
}

/** 客服：会话分页 */
export function getCsSessionPage(params) {
  return request.get('/cs/chat/page', { params })
}

/** 客服：会话消息列表 */
export function getCsMessages(sessionId) {
  return request.get(`/cs/chat/${sessionId}/messages`)
}

/** 客服：回复 */
export function replyCsMessage(sessionId, content) {
  return request.post(`/cs/chat/${sessionId}/reply`, { content })
}

/** 客服：标记已读 */
export function readCsSession(sessionId) {
  return request.put(`/cs/chat/${sessionId}/read`)
}
