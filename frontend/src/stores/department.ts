// Pinia 状态管理：部门列表 store
//
// 教学要点：
//   1. defineStore 第一个参数是 store 的唯一 id，会出现在 devtools 里
//   2. 用 setup-style 写法（组合式 API），return 出来的字段就是 store 公开 API
//   3. ref<T>(初值) 创建响应式数据；computed 派生属性
//   4. 同一个数据多页面共享时，用 store 比一次次发请求更优雅
//      （这里部门列表在“员工管理”和“部门管理”两处都要用到）

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { departmentApi } from '@/api/department'
import type { Department } from '@/types'

export const useDepartmentStore = defineStore('department', () => {
  const departments = ref<Department[]>([])
  const loading = ref(false)

  async function refresh(force = false) {
    if (!force && departments.value.length > 0) {
      return departments.value
    }
    loading.value = true
    try {
      departments.value = await departmentApi.list()
      return departments.value
    } finally {
      loading.value = false
    }
  }

  function findById(id: number | null | undefined): Department | undefined {
    if (id == null) {
      return undefined
    }
    return departments.value.find((item) => item.id === id)
  }

  return {
    departments,
    loading,
    refresh,
    findById
  }
})
