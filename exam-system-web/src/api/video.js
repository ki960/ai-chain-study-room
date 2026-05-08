import request from '../utils/request'

/**
 * ��Ƶ��ص�API
 */

// ========== �û�����ƵAPI ==========

/**
 * ��ȡ��Ƶ�б���ҳ��
 * @param {Object} params - ��ѯ����
 * @param {number} params.page - ҳ�룬Ĭ��1
 * @param {number} params.size - ÿҳ��С��Ĭ��10
 * @param {number} params.categoryId - ����ID����ѡ��
 * @param {string} params.keyword - �����ؼ��֣���ѡ��
 */
export function getVideos(params) {
  return request({
    url: '/api/user/videos',
    method: 'get',
    params
  })
}

/**
 * ��ȡ��Ƶ����
 * @param {number} id - ��ƵID
 */
export function getVideoDetail(id) {
  return request({
    url: `/api/user/videos/${id}`,
    method: 'get'
  })
}

/**
 * ��ȡ������Ƶ�б�
 * @param {number} limit - ����������Ĭ��10
 */
export function getPopularVideos(limit = 10) {
  return request({
    url: '/api/user/videos/popular',
    method: 'get',
    params: { limit }
  })
}

/**
 * ��ȡ������Ƶ�б�
 * @param {number} limit - ����������Ĭ��10
 */
export function getLatestVideos(limit = 10) {
  return request({
    url: '/api/user/videos/latest',
    method: 'get',
    params: { limit }
  })
}

/**
 * ��¼��Ƶ�ۿ�
 * @param {number} videoId - ��ƵID
 * @param {number} viewDuration - �ۿ�ʱ�����룩
 */
export function recordVideoView(videoId, viewDuration) {
  return request({
    url: `/api/user/videos/${videoId}/view`,
    method: 'post',
    params: { viewDuration }
  })
}

/**
 * �л���Ƶ����״̬
 * @param {number} videoId - ��ƵID
 */
export function toggleVideoLike(videoId) {
  return request({
    url: `/api/user/videos/${videoId}/like`,
    method: 'post'
  })
}

/**
 * �û�Ͷ����Ƶ
 * @param {FormData} formData - ������Ƶ��Ϣ���ļ��ı�����
 */
export function submitVideo(formData) {
  return request({
    url: '/api/user/videos/submit',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// ========== �������ƵAPI ==========

/**
 * ����˻�ȡ��Ƶ�б�
 * @param {Object} params - ��ѯ����
 */
export function getVideosForAdmin(params) {
  return request({
    url: '/api/admin/videos',
    method: 'get',
    params
  })
}

/**
 * ����Ա�ϴ���Ƶ
 * @param {FormData} formData - ������Ƶ��Ϣ���ļ��ı�����
 */
export function uploadVideoByAdmin(formData) {
  return request({
    url: '/api/admin/videos/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * �����Ƶ
 * @param {number} videoId - ��ƵID
 * @param {number} status - ���״̬��1-ͨ����2-�ܾ���
 * @param {string} reason - ���ԭ�򣨾ܾ�ʱ���
 */
export function auditVideo(videoId, status, reason) {
  return request({
    url: `/api/admin/videos/${videoId}/audit`,
    method: 'post',
    params: { status, reason }
  })
}

/**
 * �¼���Ƶ
 * @param {number} videoId - ��ƵID
 */
export function offlineVideo(videoId) {
  return request({
    url: `/api/admin/videos/${videoId}/offline`,
    method: 'post'
  })
}

/**
 * ɾ����Ƶ
 * @param {number} videoId - ��ƵID
 */
export function deleteVideo(videoId) {
  return request({
    url: `/api/admin/videos/${videoId}`,
    method: 'delete'
  })
}

/**
 * ��ȡ��Ƶͳ������
 */
export function getVideoStatistics() {
  return request({
    url: '/api/admin/videos/statistics',
    method: 'get'
  })
}

/**
 * ��ȡ��Ƶ��ϸͳ������
 * @param {number} videoId - ��ƵID
 * @param {number} days - ͳ��������Ĭ��30��
 */
export function getVideoDetailStats(videoId, days = 30) {
  return request({
    url: `/api/admin/videos/${videoId}/stats`,
    method: 'get',
    params: { days }
  })
}