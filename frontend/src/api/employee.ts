// 员工相关接口封装

import request from '@/utils/request'
import type {
  Employee,
  EmployeeCreateRequest,
  EmployeePageQuery,
  EmployeeUpdateRequest,
  PageResult
} from '@/types'

export const employeeApi = {
  page(query: EmployeePageQuery): Promise<PageResult<Employee>> {
    return request.get('/employees', { params: query })
  },
  detail(id: number): Promise<Employee> {
    return request.get(`/employees/${id}`)
  },
  create(payload: EmployeeCreateRequest): Promise<Employee> {
    return request.post('/employees', payload)
  },
  update(id: number, payload: EmployeeUpdateRequest): Promise<Employee> {
    return request.put(`/employees/${id}`, payload)
  },
  leave(id: number): Promise<void> {
    return request.delete(`/employees/${id}`)
  }
}
