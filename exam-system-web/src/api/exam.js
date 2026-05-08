import request from '../utils/request'

/**
 * 考试相关的API
 */

// 开始考试
export function startExam(paperId, studentName) {
  return request({
    url: '/api/user/exams/start',
    method: 'post',
    data: {
      paperId,
      studentName
    }
  })
}

// 提交答案
export function submitAnswers(examRecordId, data) {
  return request({
    url: `/api/user/exams/${examRecordId}/submit`,
    method: 'post',
    data
  })
}

// AI自动批阅
export function gradeExam(examRecordId) {
  return request({
    url: `/api/user/exams/${examRecordId}/grade`,
    method: 'post'
  })
}

// 获取我的考试记录
export function getMyExamRecords() {
  return request({
    url: '/api/user/exams/records',
    method: 'get'
  })
}

// 获取考试记录详情
export function getExamRecordById(id) {
  return request({
    url: `/api/user/exams/${id}`,
    method: 'get'
  })
}

// ========== 管理端考试记录API ==========

// 分页查询考试记录（管理端）
export function getExamRecordsForAdmin(params) {
  return request({
    url: '/api/admin/exam-records/list',
    method: 'get',
    params
  })
}

// 获取考试记录详情（管理端）
export function getExamRecordByIdForAdmin(id) {
  return request({
    url: `/api/admin/exam-records/${id}`,
    method: 'get'
  })
}

// 删除考试记录（管理端）
export function deleteExamRecord(id) {
  return request({
    url: `/api/admin/exam-records/${id}`,
    method: 'delete'
  })
}