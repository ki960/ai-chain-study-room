import request from '@/utils/request'

// ��ȡ��ҵ�����б�
export function getInterviewQuestions(params) {
  return request({
    url: '/api/interview-questions/list',
    method: 'get',
    params
  })
}

// ��ȡ��������
export function getInterviewQuestionDetail(id) {
  return request({
    url: `/api/interview-questions/${id}`,
    method: 'get'
  })
}

// �ϴ���ҵ����
export function uploadQuestion(data) {
  return request({
    url: '/api/user-contributions/upload',
    method: 'post',
    data
  })
}

// ��ȡ������Ŀ
export function getHotQuestions(limit = 10) {
  return request({
    url: '/api/interview-questions/hot',
    method: 'get',
    params: { limit }
  })
}

// ��ȡ������Ŀ
export function getLatestQuestions(limit = 10) {
  return request({
    url: '/api/interview-questions/latest',
    method: 'get',
    params: { limit }
  })
}

// �����������
export function incrementViewCount(id) {
  return request({
    url: `/api/interview-questions/${id}/view`,
    method: 'post'
  })
}

// ��ȡ��������ͳ��
export function getDirectionStats() {
  return request({
    url: '/api/interview-questions/stats/direction',
    method: 'get'
  })
}

// ��ȡ��˾ͳ��
export function getCompanyStats() {
  return request({
    url: '/api/interview-questions/stats/company',
    method: 'get'
  })
}

// ģ���������API
export function startMockInterview(data) {
  return request({
    url: '/api/mock-interview/start',
    method: 'post',
    data
  })
}

export function submitInterviewAnswer(data) {
  return request({
    url: '/api/mock-interview/submit-answer',
    method: 'post',
    data
  })
}

export function completeMockInterview(interviewId) {
  return request({
    url: `/api/mock-interview/${interviewId}/complete`,
    method: 'post'
  })
}

export function getMockInterviewDetail(interviewId) {
  return request({
    url: `/api/mock-interview/${interviewId}`,
    method: 'get'
  })
}

export function getUserInterviewRecords(userId, params) {
  return request({
    url: `/api/mock-interview/user/${userId}/records`,
    method: 'get',
    params
  })
}

// ���������API
export function getInterviewCodes(params) {
  return request({
    url: '/api/interview/codes',
    method: 'get',
    params
  })
}

export function generateInterviewCodes(data) {
  return request({
    url: '/api/interview/codes/generate',
    method: 'post',
    data
  })
}

export function activateInterviewCode(code) {
  return request({
    url: '/api/interview/codes/activate',
    method: 'post',
    data: { code }
  })
}

export function deleteInterviewCode(codeId) {
  return request({
    url: `/api/interview/codes/${codeId}`,
    method: 'delete'
  })
}

export function getInviteesList() {
  return request({
    url: '/api/interview/codes/invitees',
    method: 'get'
  })
}

export function requestInterviewCode(data) {
  return request({
    url: '/api/interview/codes/request',
    method: 'post',
    data
  })
}

// �û��������API
export function getUserCredits(userId) {
  return request({
    url: `/api/user-interview-credits/user/${userId}`,
    method: 'get'
  })
}

export function getActiveCredits(userId) {
  return request({
    url: `/api/user-interview-credits/active/${userId}`,
    method: 'get'
  })
}

// ��ȡ���Խ��
export function getInterviewResult(interviewId) {
  return request({
    url: `/api/interview/result/${interviewId}`,
    method: 'get'
  })
}

// ��ȡ�û�������ʷ
export function getUserInterviewHistory(params) {
  return request({
    url: '/api/interview/history',
    method: 'get',
    params
  })
}

// ��ȡ����ͳ������
export function getInterviewStatistics() {
  return request({
    url: '/api/interview/statistics',
    method: 'get'
  })
}

// �������Խ��
export function shareInterviewResult(interviewId, shareType) {
  return request({
    url: '/api/interview/share',
    method: 'post',
    data: {
      interviewId,
      shareType
    }
  })
}

// ��ȡ������ʷ
export function getCreditsHistory() {
  return request({
    url: '/api/interview/credits/history',
    method: 'get'
  })
}