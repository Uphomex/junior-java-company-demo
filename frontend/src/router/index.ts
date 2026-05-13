// 路由配置
//
// 教学要点：
//   1. createWebHistory() 使用 HTML5 history 模式，URL 没有 #
//   2. component: () => import() 是“懒加载”，对应页面只在访问时才下载
//   3. children 用于嵌套路由：父级是 Layout，子级是各个业务页面
//   4. meta.title 让我们在路由切换时统一改 document.title

import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/views/layout/MainLayout.vue'),
    redirect: '/employees',
    children: [
      {
        path: '/employees',
        name: 'employees',
        component: () => import('@/views/employee/EmployeeListView.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: '/departments',
        name: 'departments',
        component: () => import('@/views/department/DepartmentListView.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: '/attendance',
        name: 'attendance',
        component: () => import('@/views/attendance/AttendanceView.vue'),
        meta: { title: '考勤打卡' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach((to) => {
  const title = (to.meta?.title as string | undefined) ?? '员工与考勤管理'
  document.title = `${title} - 教学版`
})

export default router
