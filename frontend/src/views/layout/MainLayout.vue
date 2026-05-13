<script setup lang="ts">
// 后台布局：左侧菜单 + 右侧内容区
//
// 教学要点：
//   1. <el-container> 是 Element Plus 提供的整体布局容器
//   2. useRoute() 用来读取当前激活的路由，给菜单高亮用
//   3. <router-link> 渲染成 <a>，但点击不会刷新页面，保留 SPA 体验
//   4. <RouterView /> 是占位符，会被子路由对应的组件填充

import { computed } from 'vue'
import { useRoute, RouterView, RouterLink } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<template>
  <el-container class="layout">
    <el-aside class="aside" width="220px">
      <div class="logo">员工与考勤管理</div>
      <el-menu :default-active="activeMenu" router class="menu">
        <el-menu-item index="/employees">
          <el-icon><User /></el-icon>
          <RouterLink to="/employees">员工管理</RouterLink>
        </el-menu-item>
        <el-menu-item index="/departments">
          <el-icon><OfficeBuilding /></el-icon>
          <RouterLink to="/departments">部门管理</RouterLink>
        </el-menu-item>
        <el-menu-item index="/attendance">
          <el-icon><Clock /></el-icon>
          <RouterLink to="/attendance">考勤打卡</RouterLink>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="title">{{ route.meta?.title ?? '' }}</div>
        <div class="subtitle">教学版 · Vue3 + TypeScript + Element Plus</div>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script lang="ts">
import { User, OfficeBuilding, Clock } from '@element-plus/icons-vue'
export default {
  components: { User, OfficeBuilding, Clock }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}

.aside {
  background: #001529;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #fff;
  border-bottom: 1px solid #022146;
}

.menu {
  background: transparent;
  border-right: 0;
}

.menu :deep(.el-menu-item) {
  color: #c9d1d9;
}

.menu :deep(.el-menu-item.is-active) {
  background-color: #1677ff !important;
  color: #fff !important;
}

.menu :deep(a) {
  color: inherit;
  display: block;
  width: 100%;
}

.header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.subtitle {
  font-size: 12px;
  color: #909399;
}

.main {
  padding: 16px;
}
</style>
