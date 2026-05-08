<template>
  <div class="app-container">
    <div class="sidebar">
      <h3 class="sidebar-title">管理菜单</h3>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        @select="handleMenuSelect"
        router
      >
        <el-submenu index="question">
          <template #title>试题管理</template>
          <el-menu-item index="/admin/question-manage">
            <el-icon><Document /></el-icon>
            <span>题目管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/category-manage">
            <el-icon><Folder /></el-icon>
            <span>类别管理</span>
          </el-menu-item>
        </el-submenu>
        <el-submenu index="exam">
          <template #title>考试管理</template>
          <el-menu-item index="/admin/paper-manage">
            <el-icon><Files /></el-icon>
            <span>试卷管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/score-manage">
            <el-icon><DataAnalysis /></el-icon>
            <span>成绩管理</span>
          </el-menu-item>
        </el-submenu>
        <el-submenu index="system">
          <template #title>系统管理</template>
          <el-menu-item index="/admin/banner-manage">
            <el-icon><Picture /></el-icon>
            <span>轮播图管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/notice-manage">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>
        </el-submenu>
        <el-submenu index="video">
          <template #title>视频管理</template>
          <el-menu-item index="/admin/video-manage">
            <el-icon><VideoPlay /></el-icon>
            <span>视频管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/video-category-manage">
            <el-icon><Collection /></el-icon>
            <span>视频分类</span>
          </el-menu-item>
        </el-submenu>
      </el-menu>
    </div>

    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Document, Folder, Files, DataAnalysis, 
  Picture, Bell, VideoPlay, Collection 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => {
  return route.path
})

const handleMenuSelect = (index) => {
  router.push(index)
}
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  background-color: #F9FAFB;
}

.sidebar {
  width: 220px;
  background: #FFFFFF;
  border-right: 1px solid #E5E7EB;
}

.sidebar-title {
  padding: 20px 16px;
  margin: 0;
  border-bottom: 1px solid #E5E7EB;
  color: #1F2937;
  font-size: 14px;
  font-weight: 600;
}

.sidebar-menu {
  border: none;
  background: transparent;
}

.sidebar-menu :deep(.el-menu-item),
.sidebar-menu :deep(.el-submenu__title) {
  color: #6B7280;
  font-size: 13px;
  padding: 12px 16px;
}

.sidebar-menu :deep(.el-menu-item:hover),
.sidebar-menu :deep(.el-submenu__title:hover) {
  background-color: #F3F4F6;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #EFF6FF;
  color: #3B82F6;
}

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .sidebar {
    width: 64px;
  }
  
  .sidebar-title,
  .sidebar-menu :deep(.el-submenu__title span),
  .sidebar-menu :deep(.el-menu-item span) {
    display: none;
  }
  
  .sidebar-menu :deep(.el-menu-item),
  .sidebar-menu :deep(.el-submenu__title) {
    padding: 12px;
    text-align: center;
  }
  
  .main-content {
    padding: 16px;
  }
}
</style>
