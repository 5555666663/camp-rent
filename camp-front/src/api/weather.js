import request from '@/utils/request'

/**
 * 露营装备智能推荐接口
 * @author camp
 */

/** 生成露营装备智能推荐（传日期、地点） */
export function getRecommend(data) {
  return request.post('/weather/recommend', data)
}

/** 查询在架套餐列表（可按场景筛选） */
export function getPackageList(params) {
  return request.get('/package/list', { params })
}

/** 查询套餐详情 */
export function getPackageDetail(id) {
  return request.get(`/package/${id}`)
}

/** 一键预订整个套餐 */
export function orderPackage(id, data) {
  return request.post(`/package/${id}/order`, data)
}

/** 自己组合套餐下单 */
export function orderCustomPackage(data) {
  return request.post('/package/order-custom', data)
}
