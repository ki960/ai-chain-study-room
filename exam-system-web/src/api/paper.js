import request from '../utils/request'

/**
 * 试卷相关的API
 */

// ========== 用户端试卷API ==========

// 获取试卷详情（用户端）
export function getPaperById(id) {
  return request({
    url: `/api/user/papers/${id}`,
    method: 'get'
  })
}

// 获取所有试卷列表（用户端）
export function getPapers(params) {
  return request({
    url: '/api/user/papers/list',
    method: 'get',
    params
  })
}

// ========== 管理端试卷API ==========

// 创建试卷（管理端）
export function createPaper(data) {
  return request({
    url: '/api/admin/paperspapers',
    method: 'post',
    data
  })
}

// AI智能组卷（管理端）
export function createPaperWithAI(data) {
  return request({
    url: '/api/admin/paperspapers/ai',
    method: 'post',
    data
  })
}

// 获取试卷详情（管理端）
export function getPaperByIdForAdmin(id) {
  return request({
    url: `/api/admin/paperspapers/${id}`,
    method: 'get'
  })
}

// 获取所有试卷列表（管理端）
export function getPapersForAdmin(params) {
  return request({
    url: '/api/admin/paperspapers/list',
    method: 'get',
    params
  })
}

// 更新试卷（管理端）
export function updatePaper(id, data) {
  return request({
    url: `/api/admin/paperspapers/${id}`,
    method: 'put',
    data
  })
}

// 更新试卷状态（管理端）
export function updatePaperStatus(id, status) {
  return request({
    url: `/api/admin/paperspapers/${id}/status`,
    method: 'post',
    params: { status }
  })
}

// 删除试卷（管理端）
export function deletePaper(id) {
  return request({
    url: `/api/admin/paperspapers/${id}`,
    method: 'delete'
  })
}