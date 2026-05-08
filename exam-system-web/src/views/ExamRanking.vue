<template>
  <div class="exam-ranking-page">
    <!-- 页面标题 - 重新设计 -->
    <div class="page-header">
      <div class="header-decoration">
        <div class="trophy-animation">🏆</div>
        <div class="stars">
          <span class="star">⭐</span>
          <span class="star">⭐</span>
          <span class="star">⭐</span>
        </div>
      </div>
      <h2 class="main-title">🏆 考试排行榜 🏆</h2>
      <p class="subtitle">🎯 挑战极限，追求卓越！看看谁是学霸之王？</p>
      <div class="title-underline"></div>
    </div>

    <!-- 筛选条件 - 美化 -->
    <div class="filter-bar">
      <div class="filter-label">🔍 筛选条件：</div>
      <el-select 
        v-model="selectedPaperId" 
        placeholder="📚 选择试卷"
        clearable 
        style="width: 300px"
        @change="loadRanking"
        class="custom-select"
      >
        <el-option 
          v-for="paper in paperList" 
          :key="paper.id" 
          :label="paper.name" 
          :value="paper.id" 
        />
      </el-select>
      <el-select 
        v-model="rankingLimit" 
        placeholder="📊 显示数量"
        style="width: 150px"
        @change="loadRanking"
        class="custom-select"
      >
        <el-option label="前10名" :value="10" />
        <el-option label="前20名" :value="20" />
        <el-option label="前50名" :value="50" />
        <el-option label="前100名" :value="100" />
      </el-select>
      <el-button 
        type="primary" 
        @click="loadRanking" 
        :loading="loading" 
        icon="Refresh"
        class="refresh-btn"
      >
        🔄 刷新排行榜
      </el-button>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧排行榜列表 -->
      <div class="ranking-container">
        <!-- 冠军展示区 -->
        <div v-if="rankingList.length > 0" class="champion-showcase">
          <div class="champion-crown">👑</div>
          <div class="champion-info">
            <div class="champion-name">{{ rankingList[0].studentName }}</div>
            <div class="champion-score">{{ rankingList[0].score }}分</div>
            <div class="champion-title">🎉 当前考试之王！🎉</div>
          </div>
        </div>

        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="10" animated />
        </div>
        
        <div v-else-if="rankingList.length > 0" class="ranking-list">
          <div 
            v-for="(record, index) in rankingList" 
            :key="record.id" 
            class="ranking-item"
            :class="{ 'top-three': index < 3 }"
          >
            <div class="rank-number" :class="getRankClass(index + 1)">
              <span v-if="index === 0">🥇</span>
              <span v-else-if="index === 1">🥈</span>
              <span v-else-if="index === 2">🥉</span>
              <span v-else>{{ index + 1 }}</span>
            </div>
            <div class="student-info">
              <div class="student-name">{{ record.studentName }}</div>
              <div class="paper-name">📝 {{ record.paper.name }}</div>
              <div class="exam-time">📅 {{ formatDateTime(record.endTime) }}</div>
            </div>
            <div class="score-info">
              <div class="score">{{ record.score }}</div>
              <div class="total-score">/ {{ record.paper?.totalScore }}</div>
              <div class="percentage">
                {{ calculatePercentage(record.score, record.paper?.totalScore) }}%
              </div>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <div class="empty-icon">📭</div>
          <div class="empty-text">暂无排行榜数据</div>
          <div class="empty-hint">快去参加考试，成为第一个上榜的人吧！</div>
        </div>
      </div>

      <!-- 右侧统计信息 -->
      <div v-if="allRecords.length > 0" class="statistics-sidebar">
        <div class="stats-title">{{ statsTitle }}</div>
        <div class="stats-vertical">
          <div class="stat-card-vertical">
            <div class="stat-icon">👥</div>
            <div class="stat-info">
              <div class="stat-value">{{ totalParticipants }}</div>
              <div class="stat-label">参与人数</div>
            </div>
          </div>
          <div class="stat-card-vertical">
            <div class="stat-icon">📊</div>
            <div class="stat-info">
              <div class="stat-value">{{ averageScore }}</div>
              <div class="stat-label">平均分</div>
            </div>
          </div>
          <div class="stat-card-vertical">
            <div class="stat-icon">🎯</div>
            <div class="stat-info">
              <div class="stat-value">{{ maxScore }}</div>
              <div class="stat-label">最高分</div>
            </div>
          </div>
          <div class="stat-card-vertical">
            <div class="stat-icon">📉</div>
            <div class="stat-info">
              <div class="stat-value">{{ minScore }}</div>
              <div class="stat-label">最低分</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部激励区域 -->
    <div class="motivation-section">
      <div class="motivation-text">? 每一次挑战都是成长的机会！加油冲刺更高的排名吧！</div>
      <div class="floating-emojis">
        <span class="emoji">?</span>
        <span class="emoji">?</span>
        <span class="emoji">?</span>
        <span class="emoji">?</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import request from '../utils/request'

// 响应式数据
const loading = ref(false)
const rankingList = ref([])
const paperList = ref([])
const selectedPaperId = ref(null)
const rankingLimit = ref(10)
const allRecords = ref([]) // 用于统计的所有考试记录

// 计算属性 - 基于所有记录进行统计
const averageScore = computed(() => {
  if (allRecords.value.length === 0) return 0
  const total = allRecords.value.reduce((sum, record) => sum + Number(record.score), 0)
  return (total / allRecords.value.length).toFixed(1)
})

const maxScore = computed(() => {
  if (allRecords.value.length === 0) return 0
  return Math.max(...allRecords.value.map(record => Number(record.score)))
})

const minScore = computed(() => {
  if (allRecords.value.length === 0) return 0
  return Math.min(...allRecords.value.map(record => Number(record.score)))
})

const totalParticipants = computed(() => {
  return allRecords.value.length
})

// 动态统计标题
const statsTitle = computed(() => {
  if (selectedPaperId.value) {
    const selectedPaper = paperList.value.find(p => p.id === selectedPaperId.value)
    return `? ${selectedPaper?.name || '选中试卷'} 统计`
  }
  return '? 全部试卷统计'
})

// 获取试卷列表
const getPaperList = async () => {
  try {
    // 调用后端试卷列表API，只传递状态筛选参数
    const res = await request.get('/api/user/papers/list', {
      params: {
        status: 'PUBLISHED'  // 只获取已发布的试卷
      }
    })
    // 修正数据解析：后端返回的数据直接在res.data中，不是res.data.records
    paperList.value = res.data || []
    console.log('试卷列表加载成功，共', paperList.value.length, '个试卷')
  } catch (error) {
    console.error('获取试卷列表失败：', error)
    ElMessage.error('获取试卷列表失败')
  }
}

// 加载排行榜数据
const loadRanking = async () => {
  loading.value = true
  try {
    // 修正API调用参数，使用后端支持的paperId和limit参数
    const displayParams = {
      paperId: selectedPaperId.value,   // 试卷ID筛选参数
      limit: rankingLimit.value        // 显示数量限制参数
    }
    
    const statsParams = {
      paperId: selectedPaperId.value,   // 试卷ID筛选参数  
      limit: 1000                      // 统计时获取所有记录
    }
    
    // 并行调用两个API：一个用于显示，一个用于统计
    const [rankingRes, statsRes] = await Promise.all([
      request.get('/api/user/exam-records/ranking', { params: displayParams }),
      request.get('/api/user/exam-records/ranking', { params: statsParams })
    ])
    
    // 设置排行榜数据和统计数据
    rankingList.value = rankingRes.data || []
    allRecords.value = statsRes.data || []
  } catch (error) {
    console.error('获取排行榜数据失败：', error)
    ElMessage.error('获取排行榜数据失败')
  } finally {
    loading.value = false
  }
}

// 获取排名样式类
const getRankClass = (rank) => {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return 'rank-normal'
}

// 计算百分比
const calculatePercentage = (score, totalScore) => {
  if (!score || !totalScore) return 0
  return ((score / totalScore) * 100).toFixed(1)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(() => {
  getPaperList()
  loadRanking()
})
</script>

<style scoped>
.exam-ranking-page {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #F9FAFB;
  min-height: 100vh;
}

/* 页面标题 */
.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.main-title {
  font-size: 24px;
  color: #1F2937;
  margin: 0 0 8px;
  font-weight: 600;
}

.subtitle {
  color: #6B7280;
  margin: 0;
  font-size: 14px;
}

.title-underline {
  width: 60px;
  height: 3px;
  background-color: #3B82F6;
  margin: 16px auto 0;
  border-radius: 2px;
}

/* 筛选条件 */
.filter-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  justify-content: center;
  align-items: center;
  background-color: #FFFFFF;
  padding: 16px 24px;
  border-radius: 6px;
  border: 1px solid #E5E7EB;
}

.filter-label {
  color: #6B7280;
  font-weight: 500;
  font-size: 13px;
}

.custom-select {
  font-size: 13px;
}

.refresh-btn {
  font-size: 13px;
  padding: 6px 16px;
  background-color: #3B82F6;
  border-color: #3B82F6;
}

.refresh-btn:hover {
  background-color: #2563EB;
  border-color: #2563EB;
}

/* 冠军展示区 */
.champion-showcase {
  background-color: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  padding: 24px;
  text-align: center;
  margin-bottom: 20px;
}

.champion-crown {
  font-size: 32px;
  margin-bottom: 12px;
}

.champion-name {
  font-size: 20px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 6px;
}

.champion-score {
  font-size: 28px;
  font-weight: 700;
  color: #3B82F6;
  margin-bottom: 6px;
}

.champion-title {
  font-size: 13px;
  color: #6B7280;
  font-weight: 500;
}

/* 主内容区域 */
.main-content {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.ranking-container {
  flex: 2;
  margin-bottom: 0;
}

.loading-container {
  padding: 32px;
  text-align: center;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  background-color: #FFFFFF;
  border-radius: 6px;
  padding: 16px;
  border: 1px solid #E5E7EB;
  transition: all 0.2s ease;
}

.ranking-item:hover {
  background-color: #F9FAFB;
}

.ranking-item.top-three {
  border-left-width: 3px;
}

.ranking-item.top-three:nth-child(1) {
  border-left-color: #F59E0B;
}

.ranking-item.top-three:nth-child(2) {
  border-left-color: #6B7280;
}

.ranking-item.top-three:nth-child(3) {
  border-left-color: #D97706;
}

.rank-number {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  margin-right: 16px;
  border: 2px solid transparent;
}

.rank-gold {
  background-color: #FEF3C7;
  color: #D97706;
  border-color: #FCD34D;
}

.rank-silver {
  background-color: #F3F4F6;
  color: #6B7280;
  border-color: #D1D5DB;
}

.rank-bronze {
  background-color: #FEF3C7;
  color: #B45309;
  border-color: #FBBF24;
}

.rank-normal {
  background-color: #EFF6FF;
  color: #3B82F6;
  border-color: #DBEAFE;
}

.student-info {
  flex: 1;
}

.student-name {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
  color: #1F2937;
}

.paper-name {
  font-size: 12px;
  color: #6B7280;
  margin-bottom: 2px;
}

.exam-time {
  font-size: 11px;
  color: #9CA3AF;
}

.score-info {
  text-align: right;
  margin-left: 16px;
}

.score {
  font-size: 20px;
  font-weight: 600;
  color: #3B82F6;
}

.total-score {
  font-size: 12px;
  color: #6B7280;
}

.percentage {
  font-size: 11px;
  color: #10B981;
  margin-top: 4px;
  font-weight: 500;
}

/* 统计侧边栏 */
.statistics-sidebar {
  flex: 1;
  max-width: 280px;
  min-width: 260px;
  padding: 16px;
  background-color: #FFFFFF;
  border-radius: 6px;
  border: 1px solid #E5E7EB;
  height: fit-content;
  position: sticky;
  top: 24px;
}

.stats-title {
  font-size: 14px;
  font-weight: 600;
  color: #1F2937;
  margin-bottom: 14px;
  padding-bottom: 8px;
  border-bottom: 1px solid #E5E7EB;
  text-align: center;
}

.stats-vertical {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-card-vertical {
  text-align: center;
  padding: 16px;
  background-color: #F9FAFB;
  border-radius: 4px;
  border: 1px solid #E5E7EB;
}

.stat-icon {
  font-size: 22px;
  margin-bottom: 8px;
}

.stat-info {
  text-align: center;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #3B82F6;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #6B7280;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 48px 20px;
  background-color: #FFFFFF;
  border-radius: 6px;
  border: 1px solid #E5E7EB;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 14px;
}

.empty-text {
  font-size: 16px;
  color: #4B5563;
  margin-bottom: 6px;
  font-weight: 500;
}

.empty-hint {
  font-size: 13px;
  color: #9CA3AF;
}

/* 底部激励区域 */
.motivation-section {
  margin-top: 32px;
  text-align: center;
  background-color: #FFFFFF;
  padding: 20px;
  border-radius: 6px;
  border: 1px solid #E5E7EB;
}

.motivation-text {
  font-size: 14px;
  color: #4B5563;
  margin-bottom: 12px;
  font-weight: 500;
}

.floating-emojis {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.emoji {
  font-size: 24px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-ranking-page {
    padding: 16px;
  }
  
  .main-title {
    font-size: 20px;
  }
  
  .subtitle {
    font-size: 13px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .main-content {
    flex-direction: column;
    gap: 20px;
  }
  
  .ranking-container {
    flex: none;
  }
  
  .statistics-sidebar {
    position: static;
    max-width: none;
    min-width: auto;
    margin-top: 20px;
  }
  
  .champion-showcase {
    padding: 20px;
  }
  
  .champion-name {
    font-size: 24px;
  }
  
  .champion-score {
    font-size: 28px;
  }
  
  .stats-vertical {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .stat-card-vertical {
    padding: 15px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .stat-icon {
    font-size: 24px;
    margin-bottom: 8px;
  }
  
  .ranking-item {
    flex-direction: column;
    text-align: center;
    gap: 15px;
    padding: 15px;
  }
  
  .rank-number {
    margin-right: 0;
    width: 50px;
    height: 50px;
    font-size: 18px;
  }
  
  .score-info {
    margin-left: 0;
  }
  
  .score {
    font-size: 20px;
  }
  
  .floating-emojis {
    gap: 20px;
  }
  
  .emoji {
    font-size: 24px;
  }
}
</style> 