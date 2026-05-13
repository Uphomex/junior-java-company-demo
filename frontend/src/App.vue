<script setup lang="ts">
// 根组件：只放一个 <router-view />，让真正的页面由路由决定显示哪一个
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style scoped>
</style>
