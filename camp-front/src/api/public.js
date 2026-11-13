import request from '@/utils/request'

// ============================================================
// 前台公开浏览接口（网点、分类、装备、食品）
// ============================================================

// 获取启用中的网点列表
export function getStoreList() {
  return request.get('/store/list')
}

// 获取启用网点的城市列表
export function getStoreCities() {
  return request.get('/store/cities')
}

// 网点详情
export function getStoreDetail(id) {
  return request.get(`/store/${id}`)
}

// 就近网点列表（按用户经纬度计算距离，后端计算，保护高德 Key）
export function getNearestStores(params) {
  return request.get('/store/nearest', { params })
}

// 网点详情（含距离、该网点装备与食品库存）
export function getStoreDetailWithStock(id, params) {
  return request.get(`/store/${id}/detail`, { params })
}

// 获取所有分类（含分类类型：1-装备 2-食品）
export function getCategoryList() {
  return request.get('/category/list')
}

// 分页查询商品（productType：1-装备 2-食品，不传为全部）
export function getEquipmentPage(params) {
  return request.get('/equipment/page', { params })
}

// 装备/食品详情
export function getEquipmentDetail(id) {
  return request.get(`/equipment/${id}`)
}

// IP 定位兜底（浏览器不允许定位时使用，城市级精度）
export function getIpLocation() {
  return request.get('/location/ip')
}
