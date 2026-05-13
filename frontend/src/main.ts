// 应用入口文件：把 Vue 实例、Element Plus、Pinia、Router 三件套挂在一起
//   1. createApp 创建一个 Vue 应用实例
//   2. app.use(plugin) 注册全局插件
//   3. ElementPlus 提供 UI 组件库（按钮、表格、表单等）
//   4. createPinia 创建一个全局状态仓库
//   5. router 是路由实例，决定 URL ↔ 页面 的映射
//   6. 最后 mount('#app') 把应用挂到 index.html 里的 <div id="app" />

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'

import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
