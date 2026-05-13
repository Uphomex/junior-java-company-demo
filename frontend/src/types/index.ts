// 类型定义：把后端返回的 JSON 结构在 TypeScript 里建模
// 教学要点：
//   1. interface 比 type 更适合描述对象结构，可以被扩展
//   2. 后端 status 字段是数字（1=在职，0=离职；考勤 1=正常 2=迟到 3=早退），
//      前端用同名 union number type 锁定可选值，避免拼错或写错。
//   3. 可选字段用 `?:`，TypeScript 编译器会强制你判空。

export interface Department {
  id: number
  code: string
  name: string
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface Employee {
  id: number
  name: string
  phone: string
  email?: string
  departmentId: number
  position: string
  hireDate: string
  salary: number
  status: 0 | 1
  createdAt?: string
  updatedAt?: string
}

export interface EmployeeCreateRequest {
  name: string
  phone: string
  email?: string
  departmentId: number | null
  position: string
  hireDate: string
  salary: number | null
}

export type EmployeeUpdateRequest = EmployeeCreateRequest

export interface DepartmentCreateRequest {
  code: string
  name: string
}

export interface AttendanceRecord {
  id: number
  employeeId: number
  workDate: string
  checkInTime?: string
  checkOutTime?: string
  status: 1 | 2 | 3
  note?: string
}

export interface AttendanceCheckInRequest {
  employeeId: number
  note?: string
}

export interface AttendanceCheckOutRequest {
  employeeId: number
  note?: string
}

export interface PageResult<T> {
  total: number
  pageNum: number
  pageSize: number
  records: T[]
}

export interface EmployeePageQuery {
  keyword?: string
  departmentId?: number
  pageNum: number
  pageSize: number
}

export interface AttendanceListQuery {
  employeeId: number
  startDate: string
  endDate: string
}
