import request from '../utils/request'

export function generateAnalysisReport(studentName) {
  return request({
    url: '/api/user/analysis',
    method: 'POST',
    params: {
      studentName
    }
  })
}
