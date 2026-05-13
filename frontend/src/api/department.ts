// 部门相关接口封装
//
// 教学要点：
//   1. 把每一个后端 URL 包成一个 TS 函数，调用方不用关心 URL、method、参数序列化
//   2. 泛型 <T> 让 request 返回带类型的 Promise，IDE 能自动补全字段名
//   3. 文件按业务模块拆分（department / employee / attendance），方便管理

import request from '@/utils/request'
import type { Department, DepartmentCreateRequest } from '@/types'

export const departmentApi = {
  list(): Promise<Department[]> {
    return request.get('/departments')
  },
  create(payload: DepartmentCreateRequest): Promise<Department> {
    return request.post('/departments', payload)
  }
}
