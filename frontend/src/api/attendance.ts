// 考勤相关接口封装

import request from '@/utils/request'
import type {
  AttendanceCheckInRequest,
  AttendanceCheckOutRequest,
  AttendanceListQuery,
  AttendanceRecord
} from '@/types'

export const attendanceApi = {
  checkIn(payload: AttendanceCheckInRequest): Promise<AttendanceRecord> {
    return request.post('/attendance/check-in', payload)
  },
  checkOut(payload: AttendanceCheckOutRequest): Promise<AttendanceRecord> {
    return request.post('/attendance/check-out', payload)
  },
  list(query: AttendanceListQuery): Promise<AttendanceRecord[]> {
    return request.get('/attendance', { params: query })
  }
}
