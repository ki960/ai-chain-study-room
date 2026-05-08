<template>
  <div class="exam-container">
    <!-- AI智能判卷进度遮罩 -->
    <div v-if="isGrading" class="grading-overlay">
      <div class="grading-content">
        <div class="grading-icon">
          <el-icon class="is-loading"><Loading /></el-icon>
        </div>
        <h3>🤖 AI智能判卷中...</h3>
        <p>系统正在使用人工智能对您的试卷进行批改，请稍候</p>
        <div class="grading-progress">
          <el-progress :percentage="gradingProgress" :stroke-width="8" status="success" striped striped-flow />
          <p class="progress-text">{{ gradingStatusText }}</p>
        </div>
      </div>
    </div>
    
    <!-- 考试头部区域 -->
    <div class="exam-header">
      <div class="header-left">
        <h2 class="paper-title">{{ examRecord.paper?.name || '在线考试' }}</h2>
        <div class="student-info" v-if="examRecord.studentName">
          <el-icon><User /></el-icon>
          <span>考生：{{ examRecord.studentName }}</span>
        </div>
      </div>
      <div class="header-right">
        <div class="timer-display">
          <el-icon><Timer /></el-icon>
          <span>剩余时间: {{ formattedTime }}</span>
        </div>
        <el-progress 
          :percentage="progressPercentage" 
          :stroke-width="8" 
          class="timer-progress"
        />
      </div>
    </div>

    <!-- 试题区域 -->
    <div class="question-area" v-if="examRecord.paper">
      <div v-for="(group, type) in groupedQuestions" :key="type" class="question-group">
        <h3 class="group-title">{{ getQuestionTypeName(type) }}</h3>
        <div v-for="(question, index) in group" :key="question.id" class="question-card">
          <div class="question-title">
            <span class="question-number">第 {{ question.globalIndex }} 题 ({{ question.paperScore }}分)</span>
            <p class="question-content">{{ question.title }}</p>
          </div>
          <div class="question-options">
            <!-- 单选题 -->
            <el-radio-group v-if="question.type === 'CHOICE' && !question.multi" v-model="answers[question.id]" class="choice-options">
              <el-radio 
                v-for="(choice, optIndex) in question.choices" 
                :key="choice.id" 
                :label="getOptionLabel(optIndex)" 
                class="option-item"
              >
                <span class="option-label">{{ getOptionLabel(optIndex) }}.</span>
                <span class="option-content">{{ choice.content }}</span>
              </el-radio>
            </el-radio-group>
            <!-- 多选题 -->
            <el-checkbox-group v-if="question.type === 'CHOICE' && question.multi" v-model="answers[question.id]" class="choice-options">
              <el-checkbox
                v-for="(choice, optIndex) in question.choices"
                :key="choice.id"
                :label="getOptionLabel(optIndex)"
                class="option-item"
              >
                <span class="option-label">{{ getOptionLabel(optIndex) }}.</span>
                <span class="option-content">{{ choice.content }}</span>
              </el-checkbox>
            </el-checkbox-group>
            <!-- 判断题 -->
            <el-radio-group v-else-if="question.type === 'JUDGE'" v-model="answers[question.id]" class="judge-options">
              <el-radio label="T" class="judge-item">正确</el-radio>
              <el-radio label="F" class="judge-item">错误</el-radio>
            </el-radio-group>
            <!-- 简答题 -->
            <el-input 
              v-else-if="question.type === 'TEXT'" 
              type="textarea" 
              :rows="4" 
              placeholder="请输入你的答案（禁止粘贴，请手动输入）"
              v-model="answers[question.id]"
              class="text-input"
              @paste.prevent="handlePasteAttempt"
              @contextmenu.prevent="handleRightClick"
              @keydown="handleKeyDown"
              autocomplete="off"
              spellcheck="false"
            />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 提交按钮区域 -->
    <div class="submission-footer">
       <el-button type="primary" size="large" @click="submit" :loading="isSubmitting">交卷</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getExamRecordById, submitAnswers } from '../api/exam.js';
import { Timer, User, Loading } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();

const examRecord = ref({});
const answers = ref({});
const timer = ref(null);
const remainingTime = ref(0);
const totalTime = ref(0);
const isSubmitting = ref(false);
const isGrading = ref(false);
const gradingProgress = ref(0);
const gradingStatusText = ref('');

const getExamData = async () => {
  try {
    const res = await getExamRecordById(route.params.id);
    examRecord.value = res.data;
    console.log(examRecord.value);
    // 检查考试状态，如果已完成则自动跳转到结果页面
    if (examRecord.value.status === 'COMPLETED' || examRecord.value.status === '已批阅') {
      ElMessage.warning({
        message: '该考试已完成，正在跳转到结果页面...',
        duration: 2000,
        showClose: false
      });
      
      // 延迟跳转，让用户看到提示信息
      setTimeout(() => {
        router.replace(`/exam-result/${route.params.id}`);
      }, 2000);
      return;
    }
    
    // 如果考试尚未开始或状态异常，也进行相应处理
    if (examRecord.value.status !== '进行中' && examRecord.value.status !== 'IN_PROGRESS') {
      ElMessage.error({
        message: '考试状态异常，正在跳转到考试列表...',
        duration: 2000,
        showClose: false
      });
      
      setTimeout(() => {
        router.replace('/exam/list');
      }, 2000);
      return;
    }
    
    // 正常的考试逻辑
    totalTime.value = (examRecord.value.paper?.duration || 0) * 60;
    remainingTime.value = totalTime.value;
    startTimer();
  } catch (error) {
    console.error('加载考试信息失败:', error);
    console.error('加载考试信息失败:', error);
    console.error('加载考试信息失败:', error);
    console.error('加载考试信息失败:', error);
    ElMessage.error('加载考试信息失败，正在跳转到考试列表...');
    setTimeout(() => {
      router.replace('/exam/list');
    }, 2000);
  }
};

const startTimer = () => {
  timer.value = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--;
    } else {
      clearInterval(timer.value);
      // 时间到时强制交卷，不给选择机会
      ElMessage.error({
        message: '⏰ 考试时间已到！系统将在3秒后自动交卷...',
        duration: 3000,
        showClose: false
      });
      
      // 禁用所有输入控件，防止继续答题
      disableAllInputs();
      
      // 3秒后强制提交
      setTimeout(() => {
        forceSubmit();
      }, 3000);
    }
  }, 1000);
};

// 禁用所有输入控件的函数
const disableAllInputs = () => {
  // 禁用所有单选框
  const radioInputs = document.querySelectorAll('.el-radio__input input');
  radioInputs.forEach(input => {
    input.disabled = true;
  });
  
  // 禁用所有多选框
  const checkboxInputs = document.querySelectorAll('.el-checkbox__input input');
  checkboxInputs.forEach(input => {
    input.disabled = true;
  });
  
  // 禁用所有文本框
  const textareas = document.querySelectorAll('.el-textarea__inner');
  textareas.forEach(textarea => {
    textarea.disabled = true;
    textarea.style.backgroundColor = '#f5f5f5';
    textarea.style.cursor = 'not-allowed';
  });
  
  // 在页面顶部显示时间到期提示
  showTimeUpOverlay();
};

// 显示时间到期遮罩
const showTimeUpOverlay = () => {
  const overlay = document.createElement('div');
  overlay.className = 'time-up-overlay';
  overlay.innerHTML = `
    <div class="time-up-content">
      <div class="time-up-icon">⏰</div>
      <h3>考试时间已到</h3>
      <p>系统正在自动交卷，请稍候...</p>
      <div class="countdown-progress"></div>
    </div>
  `;
  document.body.appendChild(overlay);
};

// 强制交卷函数（时间到期时调用）
const forceSubmit = async () => {
  // 防止重复提交
  if (isSubmitting.value) {
    console.log('正在提交中，跳过重复提交');
    return;
  }
  
  isSubmitting.value = true;
  
  try {
    const examRecordId = route.params.id;
    
    if (!examRecordId || examRecordId === 'undefined') {
      throw new Error('考试记录ID无效，请重新开始考试');
    }
    
    // 检查是否已经提交过
    if (examRecord.value.status === 'COMPLETED') {
      console.log('考试已完成，直接跳转结果页面');
      ElMessage.success('考试已完成，正在跳转到结果页面...');
      setTimeout(() => {
        router.push(`/exam-result/${examRecordId}`);
      }, 1500);
      return;
    }
    
    const formattedAnswers = Object.entries(answers.value).map(([questionId, answer]) => ({
      questionId: Number(questionId),
      userAnswer: Array.isArray(answer) ? answer.sort().join(',') : answer
    }));
    
    await submitAnswers(examRecordId, formattedAnswers);
    
    // 移除时间到期遮罩
    const overlay = document.querySelector('.time-up-overlay');
    if (overlay) {
      overlay.remove();
    }
    
    ElMessage.success('时间到期，系统已自动交卷！');
    
    // 显示AI判卷进度
    isGrading.value = true;
    gradingProgress.value = 0;
    gradingStatusText.value = '正在分析试卷内容...';
    
    // 模拟AI判卷进度
    const progressInterval = setInterval(() => {
      if (gradingProgress.value < 30) {
        gradingProgress.value += 5;
        gradingStatusText.value = '正在分析试卷内容...';
      } else if (gradingProgress.value < 60) {
        gradingProgress.value += 3;
        gradingStatusText.value = '正在智能批改客观题...';
      } else if (gradingProgress.value < 90) {
        gradingProgress.value += 2;
        gradingStatusText.value = '正在AI评估主观题...';
      } else if (gradingProgress.value < 100) {
        gradingProgress.value += 1;
        gradingStatusText.value = '正在生成考试报告...';
      }
    }, 300);
    
    // 等待3-5秒后跳转（给用户足够的视觉反馈）
    setTimeout(() => {
      clearInterval(progressInterval);
      gradingProgress.value = 100;
      gradingStatusText.value = '批改完成！正在跳转到结果页面...';
      
      setTimeout(() => {
        isGrading.value = false;
        router.push(`/exam-result/${examRecordId}`);
      }, 1000);
    }, 4000);
    
  } catch (error) {
    console.error('自动交卷失败:', error);
    
    // 移除时间到期遮罩
    const overlay = document.querySelector('.time-up-overlay');
    if (overlay) {
      overlay.remove();
    }
    
    // 如果是重复提交错误，直接跳转
    if (error.message && error.message.includes('已完成')) {
      ElMessage.success('考试已完成，正在跳转到结果页面...');
      setTimeout(() => {
        router.push(`/exam-result/${route.params.id}`);
      }, 1500);
    } else {
      ElMessage.error('自动交卷失败，正在跳转到结果页面...');
      setTimeout(() => {
        router.push(`/exam-result/${route.params.id}`);
      }, 2000);
    }
  } finally {
    isSubmitting.value = false;
  }
};

// 格式化时间显示
const formattedTime = computed(() => {
  const minutes = Math.floor(remainingTime.value / 60);
  const seconds = remainingTime.value % 60;
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
});

// 进度条百分比
const progressPercentage = computed(() => {
  if (totalTime.value === 0) return 0;
  const passedTime = totalTime.value - remainingTime.value;
  return Math.floor((passedTime / totalTime.value) * 100);
});

const getOptionLabel = (index) => {
  return String.fromCharCode(65 + index); // A, B, C, D...
};

const groupedQuestions = computed(() => {
  if (!examRecord.value.paper?.questions) {
    return {};
  }
  let globalIndex = 0;
  const groups = examRecord.value.paper.questions.reduce((acc, q) => {
    q.globalIndex = ++globalIndex; // 添加全局索引

    // 初始化答案容器
    if (q.type === 'CHOICE' && q.multi) {
      answers.value[q.id] = []; // 多选题初始化为空数组
    } else {
      answers.value[q.id] = ''; // 其他题型初始化为空字符串
    }

    if (!acc[q.type]) {
      acc[q.type] = [];
    }
    acc[q.type].push(q);
    return acc;
  }, {});
  return groups;
});

const getQuestionTypeName = (type) => {
  const map = {
    'CHOICE': '一、选择题',
    'JUDGE': '二、判断题',
    'TEXT': '三、简答题'
  };
  return map[type] || '其他题目';
};

const submit = async () => {
  // 防止重复提交
  if (isSubmitting.value) {
    ElMessage.warning('正在交卷中，请稍候...');
    return;
  }
  
  // 检查是否已经提交过
  if (examRecord.value.status === 'COMPLETED') {
    ElMessage.success('考试已完成，正在跳转到结果页面...');
    setTimeout(() => {
      router.push(`/exam-result/${route.params.id}`);
    }, 1500);
    return;
  }
  
  try {
    await ElMessageBox.confirm(
      '确定要交卷吗？交卷后将无法修改答案。',
      '确认交卷',
      {
        confirmButtonText: '确定交卷',
        cancelButtonText: '继续答题',
        type: 'warning',
      }
    );
  } catch (error) {
    return;
  }

  isSubmitting.value = true;
  const formattedAnswers = Object.entries(answers.value).map(([questionId, answer]) => ({
    questionId: Number(questionId),
    // 对多选题的答案(数组)进行处理
    userAnswer: Array.isArray(answer) ? answer.sort().join(',') : answer
  }));
  
  try {
    // 获取考试记录ID，添加调试信息
    const examRecordId = route.params.id;
    console.log('当前考试记录ID:', examRecordId);
    console.log('提交的答案:', formattedAnswers);
    
    if (!examRecordId || examRecordId === 'undefined') {
      throw new Error('考试记录ID无效，请重新开始考试');
    }
    
    // 提交答案
    await submitAnswers(examRecordId, formattedAnswers);
    ElMessage.success('交卷成功！');
    
    // 显示AI判卷进度
    isGrading.value = true;
    gradingProgress.value = 0;
    gradingStatusText.value = '正在分析试卷内容...';
    
    // 模拟AI判卷进度
    const progressInterval = setInterval(() => {
      if (gradingProgress.value < 30) {
        gradingProgress.value += 5;
        gradingStatusText.value = '正在分析试卷内容...';
      } else if (gradingProgress.value < 60) {
        gradingProgress.value += 3;
        gradingStatusText.value = '正在智能批改客观题...';
      } else if (gradingProgress.value < 90) {
        gradingProgress.value += 2;
        gradingStatusText.value = '正在AI评估主观题...';
      } else if (gradingProgress.value < 100) {
        gradingProgress.value += 1;
        gradingStatusText.value = '正在生成考试报告...';
      }
    }, 300);
    
    // 等待3-5秒后跳转（给用户足够的视觉反馈）
    setTimeout(() => {
      clearInterval(progressInterval);
      gradingProgress.value = 100;
      gradingStatusText.value = '批改完成！正在跳转到结果页面...';
      
      setTimeout(() => {
        isGrading.value = false;
        // 跳转到结果页面，使用路径参数而不是query参数
        router.push(`/exam-result/${examRecordId}`);
      }, 1000);
    }, 4000);
    
  } catch (error) {
    console.error('提交试卷失败:', error);
    
    // 如果是重复提交错误，直接跳转
    if (error.message && error.message.includes('已完成')) {
      ElMessage.success('考试已完成，正在跳转到结果页面...');
      isGrading.value = false;
      setTimeout(() => {
        router.push(`/exam-result/${route.params.id}`);
      }, 1500);
    } else {
      ElMessage.error(error.message || '交卷失败，请稍后重试');
      isGrading.value = false;
    }
  } finally {
    isSubmitting.value = false;
  }
};

// 禁止粘贴相关函数
const handlePasteAttempt = () => {
  ElMessage.warning('为保证考试公平性，简答题禁止粘贴内容，请手动输入答案！');
};

const handleRightClick = () => {
  ElMessage.warning('考试期间禁止右键操作！');
};

const handleKeyDown = (event) => {
  // 阻止Ctrl+V粘贴
  if ((event.ctrlKey || event.metaKey) && event.key === 'v') {
    event.preventDefault();
    ElMessage.warning('为保证考试公平性，简答题禁止粘贴内容，请手动输入答案！');
    return;
  }
  
  // 阻止Ctrl+A全选（可选，根据需要启用）
  // if ((event.ctrlKey || event.metaKey) && event.key === 'a') {
  //   event.preventDefault();
  //   ElMessage.warning('考试期间禁止全选操作！');
  //   return;
  // }
  
  // 阻止F12开发者工具（可选）
  if (event.key === 'F12') {
    event.preventDefault();
    ElMessage.warning('考试期间禁止打开开发者工具！');
    return;
  }
};

onMounted(() => {
  getExamData();
});

onUnmounted(() => {
  clearInterval(timer.value);
});
</script>

<style scoped>
.exam-container {
  max-width: 1000px;
  margin: 24px auto;
  background-color: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.exam-header {
  background-color: #F8FAFC;
  padding: 20px 24px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left .paper-title {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 6px 0;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #94A3B8;
  font-size: 13px;
}

.header-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  min-width: 200px;
}

.timer-display {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #DC2626;
  font-weight: 600;
  font-size: 15px;
}

.timer-progress {
  width: 100%;
}

.timer-progress :deep(.el-progress-bar__outer) {
  background-color: #E2E8F0;
  border-radius: 4px;
}

.timer-progress :deep(.el-progress-bar__inner) {
  background-color: #3B82F6;
  border-radius: 4px;
}

/* 题目区域样式 */
.question-area {
  padding: 24px;
}

.question-group {
  margin-bottom: 24px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  padding: 14px 16px;
  background-color: #F8FAFC;
  color: #1E293B;
  border-bottom: 1px solid #E2E8F0;
}

.question-card {
  padding: 20px;
  border-bottom: 1px solid #F1F5F9;
  background-color: #FFFFFF;
  transition: background-color 0.2s ease;
}

.question-card:hover {
  background-color: #FAFBFC;
}

.question-card:last-child {
  border-bottom: none;
}

.question-title {
  margin-bottom: 14px;
}

.question-number {
  display: block;
  font-weight: 600;
  color: #3B82F6;
  margin-bottom: 6px;
  font-size: 13px;
}

.question-content {
  margin: 0;
  color: #1E293B;
  line-height: 1.6;
  font-size: 14px;
}

/* 选择题选项样式 */
.choice-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-start;
}

.choice-options .option-item {
  display: flex;
  align-items: flex-start;
  padding: 10px 14px;
  border: 1px solid #E2E8F0;
  background-color: #F8FAFC;
  border-radius: 4px;
  cursor: pointer;
  margin: 0;
  min-width: 280px;
  max-width: 500px;
  transition: all 0.2s ease;
}

.choice-options .option-item:hover {
  background-color: #EFF6FF;
  border-color: #BFDBFE;
}

.choice-options :deep(.el-radio__input.is-checked + .el-radio__label),
.choice-options :deep(.el-checkbox__input.is-checked + .el-checkbox__label) {
  background-color: #EFF6FF !important;
  border-color: #3B82F6;
}

.option-label {
  font-weight: 600;
  color: #64748B;
  margin-right: 10px;
  min-width: 20px;
  font-size: 13px;
}

.option-content {
  flex: 1;
  color: #64748B;
  line-height: 1.5;
  font-size: 13px;
}

/* 判断题样式 */
.judge-options {
  display: flex;
  gap: 12px;
  justify-content: flex-start;
}

.judge-options .judge-item {
  padding: 10px 24px;
  border: 1px solid #E2E8F0;
  background-color: #F8FAFC;
  color: #64748B;
  font-weight: 500;
  cursor: pointer;
  margin: 0;
  min-width: 80px;
  text-align: center;
  border-radius: 4px;
  font-size: 13px;
  transition: all 0.2s ease;
}

.judge-options .judge-item:hover {
  background-color: #EFF6FF;
  border-color: #BFDBFE;
}

.judge-options :deep(.el-radio__input.is-checked + .el-radio__label) {
  background-color: #EFF6FF !important;
  border-color: #3B82F6;
  color: #3B82F6;
}

/* 简答题输入框样式 */
.text-input {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
}

.text-input .el-textarea {
  max-width: 600px;
  width: 100%;
}

.text-input :deep(.el-textarea__inner) {
  border: 1px solid #E2E8F0;
  padding: 14px;
  font-size: 13px;
  line-height: 1.6;
  resize: vertical;
  background-color: #FFFFFF;
  border-radius: 4px;
}

.text-input :deep(.el-textarea__inner:focus) {
  border-color: #3B82F6;
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}

/* 提交按钮区域 */
.submission-footer {
  text-align: center;
  padding: 24px;
  background-color: #F8FAFC;
  border-top: 1px solid #E2E8F0;
}

.submission-footer :deep(.el-button) {
  padding: 10px 40px;
  font-size: 14px;
  font-weight: 600;
  background-color: #3B82F6;
  border-color: #3B82F6;
  color: #FFFFFF;
  border-radius: 4px;
}

.submission-footer :deep(.el-button:hover) {
  background-color: #2563EB;
  border-color: #2563EB;
}

.submission-footer :deep(.el-button:disabled) {
  background-color: #94A3B8;
  border-color: #94A3B8;
}

/* AI判卷遮罩样式 */
.grading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.grading-content {
  background: #FFFFFF;
  padding: 40px;
  border: 1px solid #E2E8F0;
  text-align: center;
  max-width: 360px;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.grading-icon {
  font-size: 48px;
  color: #3B82F6;
  margin-bottom: 16px;
}

.grading-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 8px 0;
}

.grading-content p {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 20px 0;
}

.grading-progress {
  width: 100%;
}

.grading-progress :deep(.el-progress-bar__outer) {
  background-color: #E2E8F0;
  border-radius: 4px;
}

.grading-progress :deep(.el-progress-bar__inner) {
  background-color: #3B82F6;
  border-radius: 4px;
}

.progress-text {
  font-size: 12px;
  color: #94A3B8;
  margin: 8px 0 0 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .exam-container {
    margin: 10px; /* 移动端边距 */
  }
  
  .exam-header {
    padding: 16px; /* 移动端内边距 */
    flex-direction: column; /* 垂直排列 */
    text-align: center; /* 文字居中 */
    gap: 12px; /* 项目间距 */
  }
  
  .header-right {
    width: 100%; /* 占满宽度 */
    align-items: center; /* 居中对齐 */
  }
  
  .question-area {
    padding: 16px; /* 移动端内边距 */
  }
  
  .question-card {
    padding: 16px; /* 移动端内边距 */
  }
  
  .choice-options {
    align-items: stretch; /* 拉伸对齐，移动端占满宽度 */
  }
  
  .choice-options .option-item {
    min-width: auto; /* 移动端取消最小宽度限制 */
    max-width: none; /* 移动端取消最大宽度限制 */
  }
  
  .judge-options {
    justify-content: flex-start; /* 移动端也保持左对齐 */
    flex-wrap: wrap; /* 允许换行 */
  }
  
  .text-input {
    justify-content: stretch; /* 移动端拉伸对齐 */
  }
  
  .text-input .el-textarea {
    max-width: none; /* 移动端取消最大宽度限制 */
  }
}

/* 时间到期遮罩样式 */
.time-up-overlay {
  position: fixed; /* 固定定位 */
  top: 0; /* 顶部对齐 */
  left: 0; /* 左侧对齐 */
  width: 100%; /* 占满宽度 */
  height: 100%; /* 占满高度 */
  background: rgba(220, 53, 69, 0.9); /* 红色半透明背景 */
  backdrop-filter: blur(8px); /* 背景模糊效果 */
  display: flex; /* 弹性布局 */
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  z-index: 9999; /* 最高层级 */
  animation: slideDown 0.5s ease-out; /* 滑入动画 */
}

.time-up-content {
  background: #ffffff; /* 白色背景 */
  padding: 40px; /* 内边距 */
  border-radius: 12px; /* 圆角 */
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3); /* 阴影 */
  text-align: center; /* 文字居中 */
  max-width: 400px; /* 最大宽度 */
  width: 90%; /* 宽度占比 */
  border: 3px solid #dc3545; /* 红色边框 */
}

.time-up-icon {
  font-size: 60px; /* 图标大小 */
  margin-bottom: 20px; /* 底部间距 */
  animation: pulse 1s infinite; /* 脉冲动画 */
}

.time-up-content h3 {
  margin: 0 0 16px 0; /* 外边距 */
  font-size: 24px; /* 字体大小 */
  font-weight: 600; /* 字体加粗 */
  color: #dc3545; /* 红色文字 */
}

.time-up-content p {
  margin: 0 0 24px 0; /* 外边距 */
  font-size: 16px; /* 字体大小 */
  color: #6c757d; /* 灰色文字 */
  line-height: 1.5; /* 行高 */
}

.countdown-progress {
  width: 100%; /* 占满宽度 */
  height: 4px; /* 进度条高度 */
  background-color: #f8f9fa; /* 背景色 */
  border-radius: 2px; /* 圆角 */
  position: relative; /* 相对定位 */
  overflow: hidden; /* 隐藏溢出 */
}

.countdown-progress::before {
  content: ''; /* 空内容 */
  position: absolute; /* 绝对定位 */
  top: 0; /* 顶部对齐 */
  left: 0; /* 左侧对齐 */
  height: 100%; /* 占满高度 */
  background: linear-gradient(90deg, #dc3545, #ff6b6b); /* 红色渐变 */
  border-radius: 2px; /* 圆角 */
  animation: countdown 3s linear; /* 倒计时动画 */
}

/* 滑入动画 */
@keyframes slideDown {
  from {
    opacity: 0; /* 开始透明 */
    transform: translateY(-50px); /* 开始向上偏移 */
  }
  to {
    opacity: 1; /* 结束不透明 */
    transform: translateY(0); /* 结束无偏移 */
  }
}

/* 脉冲动画 */
@keyframes pulse {
  0%, 100% { 
    transform: scale(1); /* 正常大小 */
    opacity: 1; /* 不透明 */
  }
  50% { 
    transform: scale(1.1); /* 放大 */
    opacity: 0.8; /* 半透明 */
  }
}

/* 倒计时进度条动画 */
@keyframes countdown {
  from { width: 0%; } /* 开始空 */
  to { width: 100%; } /* 结束满 */
}

/* 禁用状态的输入控件样式 */
:deep(.el-radio.is-disabled .el-radio__input) {
  cursor: not-allowed !important; /* 禁用光标 */
}

:deep(.el-checkbox.is-disabled .el-checkbox__input) {
  cursor: not-allowed !important; /* 禁用光标 */
}

:deep(.el-textarea.is-disabled .el-textarea__inner) {
  background-color: #f5f5f5 !important; /* 禁用背景色 */
  cursor: not-allowed !important; /* 禁用光标 */
  color: #999 !important; /* 禁用文字颜色 */
}
</style> 