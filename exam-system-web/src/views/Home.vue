<template>
  <div class="home-page">
    <div class="navbar">
      <div class="logo">
        <img src="../assets/logo.svg" alt="logo" class="logo-img" />
        <span class="title">AI链习室</span>
      </div>
      <div class="nav-actions">
        <el-button @click="goToExam" :icon="Document">考试入口</el-button>
        <el-button @click="goToRanking" :icon="Trophy">考试排行榜</el-button>
        <el-button type="primary" @click="showAdminLogin" :icon="Edit">管理员后台</el-button>
      </div>
    </div>

    <div class="main-container">
      <div class="hero-section">
        <div class="carousel-section">
          <el-carousel 
            v-model="activeBannerIndex"
            :interval="5000" 
            height="280px"
            indicator-position="inside"
            arrow="hover"
          >
            <el-carousel-item v-for="(banner, index) in bannerList" :key="index">
              <div class="banner-item" @click="handleBannerClick(banner)">
                <img :src="banner.imageUrl" alt="" class="banner-img" />
              </div>
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="notice-section">
          <div class="notice-header">
            <el-icon class="notice-icon"><Bell /></el-icon>
            <span class="notice-title">系统公告</span>
          </div>
          <div class="notice-list">
            <div 
              class="notice-item" 
              v-for="(notice, index) in noticeList.slice(0, 3)" 
              :key="index"
              @click="handleNoticeClick(notice)"
            >
              <div class="notice-item-content">
                <h4 class="notice-item-title">{{ notice.title }}</h4>
                <p class="notice-item-desc">{{ notice.content }}</p>
              </div>
              <el-tag size="small" :type="getNoticeTypeTag(notice.type)">
                {{ getNoticeTypeText(notice.type) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="quick-actions">
        <h2 class="section-title">快捷功能</h2>
        <div class="action-cards">
          <div class="action-card" @click="goToExam">
            <el-icon class="card-icon"><Document /></el-icon>
            <h3>智能考试</h3>
            <p>AI智能出题，自动批阅</p>
          </div>
          <div class="action-card" @click="goToPractice">
            <el-icon class="card-icon"><Edit /></el-icon>
            <h3>智能刷题</h3>
            <p>个性化练习，智能分析</p>
          </div>
          <div class="action-card" @click="goToRanking">
            <el-icon class="card-icon"><Trophy /></el-icon>
            <h3>学习排行</h3>
            <p>查看排名，激发动力</p>
          </div>
          <div class="action-card" @click="goToAnalysis">
            <el-icon class="card-icon"><DataAnalysis /></el-icon>
            <h3>AI分析</h3>
            <p>智能报告，个性化建议</p>
          </div>
          <div class="action-card" @click="goToVideos">
            <el-icon class="card-icon"><VideoPlay /></el-icon>
            <h3>视频百科</h3>
            <p>技术讲解，分类学习</p>
          </div>
        </div>
      </div>

      <div class="popular-section">
        <div class="section-header">
          <h2 class="section-title">热门题目</h2>
          <el-button text @click="goToPractice">查看更多</el-button>
        </div>
        <div class="popular-grid">
          <div class="popular-card" v-for="question in popularQuestions" :key="question.id">
            <div class="question-type">
              <el-tag :type="getQuestionTypeTag(question.type)" size="small">
                {{ getQuestionTypeText(question.type) }}
              </el-tag>
              <el-tag :type="getDifficultyType(question.difficulty)" size="small">
                {{ getDifficultyText(question.difficulty) }}
              </el-tag>
            </div>
            <h4 class="question-title">{{ question.title }}</h4>
            <p class="question-category">{{ question.categoryName }}</p>
            <div class="question-stats">
              <span><el-icon><View /></el-icon> {{ question.viewCount || 0 }}</span>
              <span><el-icon><Check /></el-icon> {{ question.correctRate || 0 }}%</span>
            </div>
          </div>
        </div>
      </div>

      <div class="stats-section">
        <div class="stats-grid">
          <div class="stat-card">
            <el-icon class="stat-icon"><Document /></el-icon>
            <div class="stat-content">
              <h3 class="stat-number">{{ stats.questionCount || 0 }}</h3>
              <p class="stat-label">题目总数</p>
            </div>
          </div>
          <div class="stat-card">
            <el-icon class="stat-icon"><User /></el-icon>
            <div class="stat-content">
              <h3 class="stat-number">{{ stats.userCount || 0 }}</h3>
              <p class="stat-label">用户总数</p>
            </div>
          </div>
          <div class="stat-card">
            <el-icon class="stat-icon"><Files /></el-icon>
            <div class="stat-content">
              <h3 class="stat-number">{{ stats.examCount || 0 }}</h3>
              <p class="stat-label">考试场次</p>
            </div>
          </div>
          <div class="stat-card">
            <el-icon class="stat-icon"><TrendCharts /></el-icon>
            <div class="stat-content">
              <h3 class="stat-number">{{ stats.todayExamCount || 0 }}</h3>
              <p class="stat-label">今日考试</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="adminLoginVisible" title="管理员登录" width="400px" :close-on-click-modal="false">
      <el-form :model="adminLoginForm" :rules="adminLoginRules" ref="adminLoginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminLoginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="adminLoginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminLoginVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdminLogin" :loading="adminLoginLoading">登录</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="noticeDetailVisible" :title="selectedNotice?.title" width="600px">
      <div class="notice-detail" v-if="selectedNotice">
        <div class="notice-detail-meta">
          <el-tag :type="getNoticeTypeTag(selectedNotice.type)">
            {{ getNoticeTypeText(selectedNotice.type) }}
          </el-tag>
          <span class="notice-detail-time">{{ formatTime(selectedNotice.createTime) }}</span>
        </div>
        <div class="notice-detail-content" v-html="selectedNotice.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, Document, Edit, Trophy, Bell, DataAnalysis, View, Check, User, Files, TrendCharts, VideoPlay, ChatDotRound, Microphone
} from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()

const bannerList = ref([])
const noticeList = ref([])
const popularQuestions = ref([])
const stats = ref({
  questionCount: 0,
  userCount: 0,
  examCount: 0,
  todayExamCount: 0
})

const adminLoginVisible = ref(false)
const adminLoginLoading = ref(false)
const adminLoginFormRef = ref()
const adminLoginForm = reactive({ username: '', password: '' })
const adminLoginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const noticeDetailVisible = ref(false)
const selectedNotice = ref(null)
const activeBannerIndex = ref(0)

const getBannerList = async () => {
  try {
    const res = await request.get('/api/common/banners/active')
    bannerList.value = res.data || []
    console.log('轮播图数据加载完成')
  } catch (error) {
    console.error('获取轮播图数据失败：', error)
    bannerList.value = [
      {
        id: 1,
        title: '智能AI生成题目',
        description: '利用先进AI技术，快速生成高质量考试题目',
        imageUrl: '/api/banners/ai-generate.jpg',
        linkUrl: '/ai-generate',
        isActive: true
      },
      {
        id: 2,
        title: '海量题库资源',
        description: '覆盖多个学科领域，题目类型丰富多样',
        imageUrl: '/api/banners/question-bank.jpg',
        linkUrl: '/practice',
        isActive: true
      },
      {
        id: 3,
        title: '智能学习分析',
        description: '详细的答题报告，帮助您精准提升',
        imageUrl: '/api/banners/analysis.jpg',
        linkUrl: '/analysis',
        isActive: true
      }
    ]
  }
}

const getNoticeList = async () => {
  try {
    const res = await request.get('/api/user/notices/latest', { params: { limit: 5 } })
    noticeList.value = res.data || []
    console.log('公告数据加载完成')
  } catch (error) {
    console.error('获取公告数据失败：', error)
    noticeList.value = [
      {
        id: 1,
        title: '系统升级公告',
        content: '为了提供更好的服务体验，系统将于本周末进行升级维护。维护期间可能会出现短暂的服务中断，请大家合理安排考试时间。感谢您的理解与配合！',
        type: 'SYSTEM',
        createTime: '2024-06-24 10:00:00',
        isActive: true
      },
      {
        id: 2,
        title: '新增AI智能生成功能',
        content: '我们很高兴地宣布，系统新增了AI智能生成题目功能，可以快速生成高质量的考试题目。该功能支持多种题型和难度级别，让出题更加高效便捷。',
        type: 'FEATURE',
        createTime: '2024-06-23 15:30:00',
        isActive: true
      },
      {
        id: 3,
        title: '考试注意事项',
        content: '各位同学在参加在线考试时，请确保网络连接稳定，不要随意切换窗口。考试过程中如遇到技术问题，请及时联系技术支持。祝大家取得好成绩！',
        type: 'NOTICE',
        createTime: '2024-06-22 09:00:00',
        isActive: true
      }
    ]
  }
}

const getPopularQuestions = async () => {
  try {
    const res = await request.get('/api/user/questions/popular', { params: { size: 6 } })
    popularQuestions.value = res.data || []
  } catch (error) {
    console.error('获取热门题目失败：', error)
  }
}

const getStats = async () => {
  try {
    const res = await request.get('/api/common/stats/overview')
    if (res.code === 200) {
      stats.value = {
        questionCount: res.data.questionCount || 0,
        userCount: res.data.userCount || 0,
        examCount: res.data.examCount || 0,
        todayExamCount: res.data.todayExamCount || 0
      }
      console.log('统计数据获取成功：', stats.value)
    } else {
      console.error('获取统计数据失败：', res.message)
      stats.value = {
        questionCount: 0,
        userCount: 0,
        examCount: 0,
        todayExamCount: 0
      }
    }
  } catch (error) {
    console.error('获取统计数据失败：', error)
    stats.value = {
      questionCount: 0,
      userCount: 0,
      examCount: 0,
      todayExamCount: 0
    }
  }
}

const handleBannerClick = (banner) => {
  if (banner.linkUrl) {
    if (banner.linkUrl.startsWith('http://') || banner.linkUrl.startsWith('https://')) {
      window.open(banner.linkUrl, '_blank')
    } else {
      router.push(banner.linkUrl)
    }
  }
}

const handleNoticeClick = (notice) => {
  selectedNotice.value = notice
  noticeDetailVisible.value = true
}

const formatNoticeDate = (dateStr) => {
  const date = new Date(dateStr)
  return {
    day: date.getDate().toString().padStart(2, '0'),
    month: (date.getMonth() + 1).toString().padStart(2, '0') + '月'
  }
}

const formatTime = (dateStr) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getNoticeTypeTag = (type) => {
  const tagMap = {
    'SYSTEM': 'primary',
    'FEATURE': 'success',
    'NOTICE': 'info'
  }
  return tagMap[type] || 'info'
}

const getNoticeTypeText = (type) => {
  const textMap = {
    'SYSTEM': '系统',
    'FEATURE': '新功能',
    'NOTICE': '通知'
  }
  return textMap[type] || '其他'
}

const getQuestionTypeTag = (type) => {
  const tagMap = {
    'CHOICE': 'primary',
    'JUDGE': 'success',
    'TEXT': 'info'
  }
  return tagMap[type] || 'info'
}

const getQuestionTypeText = (type) => {
  const textMap = {
    'CHOICE': '选择题',
    'JUDGE': '判断题',
    'TEXT': '简答题'
  }
  return textMap[type] || type
}

const getDifficultyType = (difficulty) => {
  const typeMap = {
    'EASY': 'success',
    'MEDIUM': 'info',
    'HARD': 'warning'
  }
  return typeMap[difficulty] || 'info'
}

const getDifficultyText = (difficulty) => {
  const textMap = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return textMap[difficulty] || difficulty
}

const goToExam = () => {
  router.push('/exam/list')
}

const goToPractice = () => {
  router.push('/practice')
}

const goToRanking = () => {
  router.push('/exam-ranking')
}

const goToAnalysis = () => {
  router.push('/analysis')
}

const goToVideos = () => {
  router.push('/videos')
}

const goToInterviewQuestions = () => {
  router.push('/interview-questions')
}

const goToMockInterview = () => {
  router.push('/mock-interview')
}

const showAdminLogin = () => {
  adminLoginVisible.value = true
}

const handleAdminLogin = async () => {
  if (!adminLoginFormRef.value) return
  await adminLoginFormRef.value.validate(async (valid) => {
    if (valid) {
      adminLoginLoading.value = true
      try {
        const res = await request.post('/api/common/user/login', adminLoginForm)
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        ElMessage.success('登录成功，正在跳转到管理员后台...')
        adminLoginVisible.value = false
        adminLoginForm.username = ''
        adminLoginForm.password = ''
        router.push('/admin')
      } catch (e) {
      } finally {
        adminLoginLoading.value = false
      }
    }
  })
}

onMounted(() => {
  getBannerList()
  getNoticeList()
  getPopularQuestions()
  getStats()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: #F5F7FA;
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #FFFFFF;
  border-bottom: 1px solid #E2E8F0;
  padding: 0 24px;
  height: 64px;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
}

.logo {
  display: flex;
  align-items: center;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 12px;
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-actions :deep(.el-button) {
  font-size: 13px;
  padding: 6px 16px;
  border-radius: 4px;
}

.nav-actions :deep(.el-button--primary) {
  background-color: #3B82F6;
  border-color: #3B82F6;
  color: #FFFFFF;
  font-weight: 500;
}

.nav-actions :deep(.el-button:not(.el-button--primary)) {
  color: #64748B;
  border-color: #E2E8F0;
}

.nav-actions :deep(.el-button:not(.el-button--primary):hover) {
  background-color: #F1F5F9;
  border-color: #CBD5E1;
}

.main-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 24px;
}

.hero-section {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 48px;
}

.carousel-section {
  border-radius: 8px;
  overflow: hidden;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.banner-item {
  width: 100%;
  height: 100%;
  position: relative;
  cursor: pointer;
}

.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.notice-section {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.notice-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #E2E8F0;
}

.notice-icon {
  font-size: 16px;
  margin-right: 8px;
  color: #3B82F6;
}

.notice-title {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
}

.notice-list {
  padding: 0;
}

.notice-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  cursor: pointer;
  border-bottom: 1px solid #F1F5F9;
  transition: background-color 0.2s ease;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-item:hover {
  background-color: #F8FAFC;
}

.notice-item-content {
  flex: 1;
  min-width: 0;
}

.notice-item-title {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notice-item-desc {
  font-size: 12px;
  color: #94A3B8;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.notice-item :deep(.el-tag) {
  flex-shrink: 0;
  margin-left: 12px;
  border-radius: 4px;
}

.notice-item :deep(.el-tag--primary) {
  background-color: #FEF2F2;
  border-color: #FECACA;
  color: #DC2626;
}

.notice-item :deep(.el-tag--success) {
  background-color: #ECFDF5;
  border-color: #A7F3D0;
  color: #059669;
}

.notice-item :deep(.el-tag--info) {
  background-color: #EFF6FF;
  border-color: #BFDBFE;
  color: #2563EB;
}

.quick-actions {
  margin-bottom: 48px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 24px 0;
}

.action-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.action-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 24px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.action-card:hover {
  background-color: #EFF6FF;
  border-color: #BFDBFE;
  transform: translateY(-2px);
}

.card-icon {
  font-size: 32px;
  color: #3B82F6;
  margin-bottom: 14px;
}

.action-card h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 6px 0;
}

.action-card p {
  font-size: 12px;
  color: #94A3B8;
  margin: 0;
  line-height: 1.5;
}

.popular-section {
  margin-bottom: 48px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.popular-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.popular-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 20px;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.popular-card:hover {
  background-color: #F8FAFC;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.question-type {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.question-type :deep(.el-tag) {
  font-size: 11px;
  border-radius: 4px;
}

.question-type :deep(.el-tag--primary) {
  background-color: #EFF6FF;
  border-color: #BFDBFE;
  color: #2563EB;
}

.question-type :deep(.el-tag--success) {
  background-color: #ECFDF5;
  border-color: #A7F3D0;
  color: #059669;
}

.question-type :deep(.el-tag--warning) {
  background-color: #FFFBEB;
  border-color: #FEF3C7;
  color: #D97706;
}

.question-title {
  font-size: 14px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.question-category {
  font-size: 12px;
  color: #94A3B8;
  margin: 0 0 12px 0;
}

.question-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #94A3B8;
}

.question-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stats-section {
  margin-bottom: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 20px;
}

.stat-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 24px 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  font-size: 26px;
  color: #3B82F6;
  margin-bottom: 12px;
}

.stat-number {
  font-size: 26px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 4px 0;
}

.stat-label {
  font-size: 12px;
  color: #94A3B8;
  margin: 0;
}

.notice-detail-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #E2E8F0;
}

.notice-detail-time {
  color: #94A3B8;
  font-size: 12px;
}

.notice-detail-content {
  line-height: 1.7;
  color: #64748B;
  font-size: 14px;
}

@media (max-width: 768px) {
  .navbar {
    padding: 0 16px;
  }
  
  .nav-actions {
    gap: 8px;
  }
  
  .nav-actions :deep(.el-button) {
    font-size: 12px;
    padding: 4px 12px;
  }
  
  .nav-actions :deep(.el-button span) {
    display: none;
  }
  
  .main-container {
    padding: 20px 16px;
  }
  
  .hero-section {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .section-title {
    font-size: 18px;
  }
  
  .action-cards {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  }
  
  .popular-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .action-card {
    padding: 20px 16px;
  }
  
  .card-icon {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .action-card {
    padding: 16px 12px;
  }
  
  .action-card h3 {
    font-size: 13px;
  }
  
  .action-card p {
    font-size: 11px;
  }
}
</style>
