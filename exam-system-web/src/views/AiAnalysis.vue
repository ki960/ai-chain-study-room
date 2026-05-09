<template>
  <div class="ai-analysis-container">
    <div class="page-header">
      <h1>学生学习分析报告</h1>
      <p class="subtitle">基于AI的个性化学习分析</p>
    </div>

    <div class="search-section">
      <el-form :model="form" ref="formRef" class="search-form">
        <el-form-item label="学生姓名" prop="studentName">
          <el-input
            v-model="form.studentName"
            placeholder="请输入学生姓名"
            class="name-input"
            @keyup.enter="handleSearch"
            :disabled="loading"
          >
            <template #prefix>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            @click="handleSearch"
            :loading="loading"
            :disabled="loading || !form.studentName.trim()"
            class="search-btn"
          >
            <el-icon><Search /></el-icon>
            生成分析报告
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="loading" class="loading-section">
      <div class="loading-content">
        <el-spinner size="50" />
        <p>正在生成分析报告，请稍候...</p>
      </div>
    </div>

    <div v-else-if="analysisResult" class="result-section">
      <div class="result-header">
        <h2>分析报告结果 - {{ analysisResult.studentName }}</h2>
        <el-button type="default" @click="resetSearch" class="reset-btn">
          <el-icon><Refresh /></el-icon>
          重新查询
        </el-button>
      </div>

      <div class="statistics-section">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6">
            <div class="stat-card">
              <div class="stat-icon exam-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-value">{{ analysisResult.totalExamCount }}</p>
                <p class="stat-label">考试总次数</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-card">
              <div class="stat-icon score-icon">
                <el-icon><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-value">{{ analysisResult.averageScore.toFixed(1) }}</p>
                <p class="stat-label">平均得分</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-card">
              <div class="stat-icon correct-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-value">{{ analysisResult.correctRate.toFixed(1) }}%</p>
                <p class="stat-label">正确率</p>
              </div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-card">
              <div class="stat-icon wrong-icon">
                <el-icon><Close /></el-icon>
              </div>
              <div class="stat-info">
                <p class="stat-value">{{ analysisResult.wrongAnswerCount }}</p>
                <p class="stat-label">错误题数</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="charts-section">
        <el-row :gutter="20">
          <el-col :xs="24" :md="12">
            <div class="chart-card">
              <h3>知识点错误率分布</h3>
              <div class="bar-chart">
                <div v-for="item in analysisResult.chartData.categoryErrorRate" :key="item.name" class="bar-item">
                  <span class="bar-label">{{ item.name }}</span>
                  <div class="bar-container">
                    <div 
                      class="bar-fill" 
                      :style="{ width: item.errorRate + '%', backgroundColor: getBarColor(item.errorRate) }"
                    ></div>
                  </div>
                  <span class="bar-value">{{ item.errorRate.toFixed(1) }}%</span>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :md="12">
            <div class="chart-card">
              <h3>题型分布</h3>
              <div class="pie-chart-container">
                <div class="pie-chart">
                  <svg viewBox="0 0 100 100">
                    <circle
                      v-for="(item, index) in pieChartData"
                      :key="item.name"
                      cx="50"
                      cy="50"
                      r="40"
                      :fill="pieColors[index % pieColors.length]"
                      :stroke="item.color"
                      stroke-width="2"
                      :d="getPieSlice(item.percentage, index)"
                    />
                  </svg>
                  <div class="pie-center">
                    <span>{{ analysisResult.totalAnswerCount }}</span>
                    <span class="pie-center-label">总题数</span>
                  </div>
                </div>
                <div class="pie-legend">
                  <div v-for="(item, index) in analysisResult.chartData.questionTypeDistribution" :key="item.name" class="legend-item">
                    <span class="legend-color" :style="{ backgroundColor: pieColors[index % pieColors.length] }"></span>
                    <span class="legend-text">{{ item.name }} ({{ item.value }})</span>
                  </div>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>

        <div class="chart-card full-width">
          <h3>历次考试成绩趋势</h3>
          <div class="line-chart">
            <div class="line-chart-grid">
              <div v-for="i in 5" :key="i" class="grid-line" :style="{ bottom: (i - 1) * 25 + '%' }">
                <span class="grid-label">{{ (i - 1) * 25 }}分</span>
              </div>
            </div>
            <div class="line-chart-bars">
              <div 
                v-for="(item, index) in analysisResult.chartData.examScoreTrend" 
                :key="item.label" 
                class="bar-wrapper"
              >
                <div 
                  class="score-bar" 
                  :style="{ height: (item.score || 0) + '%' }"
                  :title="item.date + ': ' + (item.score || '未批阅') + '分'"
                ></div>
                <span class="bar-date">{{ item.date }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-if="analysisResult.recommendations && analysisResult.recommendations.length > 0" class="recommendations-section">
        <h3>推荐学习课程</h3>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :lg="8" v-for="course in analysisResult.recommendations" :key="course.videoId">
            <div class="course-card">
              <div class="course-cover">
                <img :src="course.coverUrl || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=education%20video%20thumbnail%20learning%20course&image_size=square'" alt="课程封面" />
                <span class="course-duration">{{ course.durationText }}</span>
              </div>
              <div class="course-info">
                <h4>{{ course.title }}</h4>
                <p class="course-desc">{{ course.description }}</p>
                <div class="course-meta">
                  <span class="course-tag">{{ course.categoryName }}</span>
                  <span class="course-difficulty">{{ getDifficultyText(course.difficulty) }}</span>
                </div>
                <p class="course-suggestion">{{ course.learningSuggestion }}</p>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="report-section">
        <h3>详细分析报告</h3>
        <div v-html="renderedMarkdown" class="markdown-body"></div>
      </div>
    </div>

    <div v-else-if="error" class="error-section">
      <el-card class="error-card">
        <div class="error-content">
          <el-icon class="error-icon"><InfoFilled /></el-icon>
          <h3>查询失败</h3>
          <p>{{ error }}</p>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Refresh /></el-icon>
            重新查询
          </el-button>
        </div>
      </el-card>
    </div>

    <div v-else class="empty-section">
      <div class="empty-content">
        <el-icon class="empty-icon"><Document /></el-icon>
        <p>请输入学生姓名，点击按钮生成学习分析报告</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Search, Refresh, InfoFilled, Document, TrendCharts, Check, Close } from '@element-plus/icons-vue'
import request from '../utils/request'

const form = ref({
  studentName: ''
})

const formRef = ref(null)
const loading = ref(false)
const analysisResult = ref(null)
const error = ref('')

let debounceTimer = null

const pieColors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6']

const pieChartData = computed(() => {
  if (!analysisResult.value?.chartData?.questionTypeDistribution) return []
  const total = analysisResult.value.chartData.questionTypeDistribution.reduce((sum, item) => sum + item.value, 0)
  return analysisResult.value.chartData.questionTypeDistribution.map(item => ({
    ...item,
    percentage: total > 0 ? (item.value / total) * 100 : 0
  }))
})

const renderedMarkdown = computed(() => {
  if (!analysisResult.value?.analysisReport) return ''
  return markdownToHtml(analysisResult.value.analysisReport)
})

watch(() => form.value.studentName, (newVal) => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  debounceTimer = setTimeout(() => {
    error.value = ''
  }, 300)
})

const handleSearch = async () => {
  if (!form.value.studentName.trim()) {
    ElMessage.warning('请输入学生姓名')
    return
  }

  loading.value = true
  analysisResult.value = null
  error.value = ''

  try {
    const response = await request({
      url: '/api/user/analysis/full',
      method: 'POST',
      params: {
        studentName: form.value.studentName.trim()
      }
    })

    if (response && response.data) {
      analysisResult.value = response.data
      ElMessage.success('分析报告生成成功')
    } else {
      throw new Error('未获取到分析报告')
    }
  } catch (err) {
    error.value = err.message || '生成分析报告失败，请稍后重试'
    ElMessage.error(error.value)
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  form.value.studentName = ''
  analysisResult.value = null
  error.value = ''
}

const getBarColor = (errorRate) => {
  if (errorRate >= 50) return '#ef4444'
  if (errorRate >= 30) return '#f59e0b'
  return '#10b981'
}

const getDifficultyText = (difficulty) => {
  const map = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return map[difficulty] || '未知'
}

const getPieSlice = (percentage, index) => {
  const startAngle = pieChartData.value.slice(0, index).reduce((sum, item) => sum + item.percentage, 0)
  const endAngle = startAngle + percentage
  
  const startRad = (startAngle / 100) * 2 * Math.PI - Math.PI / 2
  const endRad = (endAngle / 100) * 2 * Math.PI - Math.PI / 2
  
  const x1 = 50 + 40 * Math.cos(startRad)
  const y1 = 50 + 40 * Math.sin(startRad)
  const x2 = 50 + 40 * Math.cos(endRad)
  const y2 = 50 + 40 * Math.sin(endRad)
  
  const largeArcFlag = percentage > 50 ? 1 : 0
  
  return `M 50 50 L ${x1} ${y1} A 40 40 0 ${largeArcFlag} 1 ${x2} ${y2} Z`
}

const markdownToHtml = (markdown) => {
  let html = markdown || ''
  
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>')
  
  html = html.replace(/\*\*(.*?)\*\*/gim, '<strong>$1</strong>')
  html = html.replace(/\*(.*?)\*/gim, '<em>$1</em>')
  
  html = html.replace(/^\- (.*$)/gim, '<li>$1</li>')
  html = html.replace(/(<li>[\s\S]*?<\/li>)/gim, '<ul>$1</ul>')
  
  html = html.replace(/^\d+\. (.*$)/gim, '<li>$1</li>')
  html = html.replace(/(<li>[\s\S]*?<\/li>)/gim, '<ol>$1</ol>')
  
  html = html.replace(/\n/gim, '<br>')
  
  return html
}
</script>

<style scoped>
.ai-analysis-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-header h1 {
  font-size: 28px;
  color: #1f2937;
  margin-bottom: 8px;
}

.page-header .subtitle {
  color: #6b7280;
  font-size: 14px;
}

.search-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 30px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.search-form .name-input {
  width: 350px;
  max-width: 100%;
}

.search-btn {
  height: 40px;
  padding: 0 24px;
}

.loading-section {
  text-align: center;
  padding: 60px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.loading-content p {
  margin-top: 20px;
  color: #6b7280;
  font-size: 16px;
}

.result-section {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  border-bottom: 1px solid #e5e7eb;
}

.result-header h2 {
  font-size: 18px;
  color: #1f2937;
  margin: 0;
}

.reset-btn {
  padding: 6px 16px;
}

.statistics-section {
  padding: 30px;
  border-bottom: 1px solid #e5e7eb;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 20px;
  border-radius: 12px;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.exam-icon { background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%); }
.score-icon { background: linear-gradient(135deg, #10b981 0%, #059669 100%); }
.correct-icon { background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%); }
.wrong-icon { background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); }

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.stat-label {
  font-size: 13px;
  color: #6b7280;
  margin: 4px 0 0;
}

.charts-section {
  padding: 30px;
  border-bottom: 1px solid #e5e7eb;
}

.chart-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.chart-card h3 {
  font-size: 16px;
  color: #1f2937;
  margin: 0 0 20px;
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bar-label {
  width: 80px;
  font-size: 13px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bar-container {
  flex: 1;
  height: 24px;
  background: #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 12px;
  transition: width 0.5s ease;
}

.bar-value {
  width: 50px;
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  text-align: right;
}

.pie-chart-container {
  display: flex;
  align-items: center;
  gap: 20px;
}

.pie-chart {
  position: relative;
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.pie-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.pie-center span:first-child {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.pie-center-label {
  font-size: 10px !important;
  color: #6b7280;
}

.pie-legend {
  flex: 1;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 3px;
}

.legend-text {
  font-size: 13px;
  color: #6b7280;
}

.line-chart {
  position: relative;
  height: 200px;
  padding-left: 40px;
}

.line-chart-grid {
  position: absolute;
  left: 40px;
  right: 0;
  top: 0;
  bottom: 30px;
}

.grid-line {
  position: absolute;
  left: 0;
  right: 0;
  border-bottom: 1px dashed #e5e7eb;
}

.grid-label {
  position: absolute;
  left: -40px;
  bottom: -8px;
  font-size: 11px;
  color: #9ca3af;
}

.line-chart-bars {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  height: calc(100% - 30px);
  padding-bottom: 10px;
}

.bar-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  margin: 0 4px;
}

.score-bar {
  width: 30px;
  background: linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 6px 6px 0 0;
  transition: height 0.5s ease;
  min-height: 4px;
}

.bar-date {
  font-size: 11px;
  color: #9ca3af;
  margin-top: 8px;
}

.recommendations-section {
  padding: 30px;
  border-bottom: 1px solid #e5e7eb;
}

.recommendations-section h3 {
  font-size: 18px;
  color: #1f2937;
  margin: 0 0 20px;
}

.course-card {
  background: #f8fafc;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 20px;
}

.course-cover {
  position: relative;
  height: 150px;
  overflow: hidden;
}

.course-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.course-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 4px;
}

.course-info {
  padding: 16px;
}

.course-info h4 {
  font-size: 15px;
  color: #1f2937;
  margin: 0 0 8px;
}

.course-desc {
  font-size: 13px;
  color: #6b7280;
  margin: 0 0 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.course-tag {
  background: #dbeafe;
  color: #1d4ed8;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 4px;
}

.course-difficulty {
  background: #fef3c7;
  color: #d97706;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 4px;
}

.course-suggestion {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.report-section {
  padding: 30px;
}

.report-section h3 {
  font-size: 18px;
  color: #1f2937;
  margin: 0 0 20px;
}

.markdown-body {
  line-height: 1.8;
  color: #374151;
}

.markdown-body h1 {
  font-size: 24px;
  color: #1f2937;
  margin: 24px 0 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #3b82f6;
}

.markdown-body h2 {
  font-size: 20px;
  color: #1f2937;
  margin: 20px 0 12px;
}

.markdown-body h3 {
  font-size: 16px;
  color: #374151;
  margin: 16px 0 10px;
}

.markdown-body p {
  margin: 10px 0;
  text-indent: 2em;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 24px;
  margin: 10px 0;
}

.markdown-body li {
  margin: 8px 0;
}

.markdown-body strong {
  color: #1f2937;
  font-weight: 600;
}

.markdown-body em {
  font-style: italic;
}

.error-section {
  margin-top: 30px;
}

.error-card {
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.error-content {
  text-align: center;
  padding: 40px;
}

.error-icon {
  font-size: 48px;
  color: #ef4444;
  margin-bottom: 16px;
}

.error-content h3 {
  font-size: 18px;
  color: #1f2937;
  margin-bottom: 8px;
}

.error-content p {
  color: #6b7280;
  margin-bottom: 20px;
}

.empty-section {
  text-align: center;
  padding: 80px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.empty-icon {
  font-size: 64px;
  color: #d1d5db;
  margin-bottom: 20px;
}

.empty-content p {
  color: #6b7280;
  font-size: 16px;
}

@media (max-width: 768px) {
  .ai-analysis-container {
    padding: 20px 15px;
  }

  .page-header h1 {
    font-size: 22px;
  }

  .search-section {
    padding: 20px;
  }

  .search-form {
    flex-direction: column;
    align-items: stretch;
  }

  .search-form .name-input {
    width: 100%;
  }

  .search-btn {
    width: 100%;
  }

  .result-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .statistics-section {
    padding: 20px;
  }

  .stat-card {
    margin-bottom: 15px;
  }

  .charts-section {
    padding: 20px;
  }

  .pie-chart-container {
    flex-direction: column;
    align-items: flex-start;
  }

  .bar-label {
    width: 60px;
  }

  .recommendations-section {
    padding: 20px;
  }

  .report-section {
    padding: 20px;
  }
}
</style>
