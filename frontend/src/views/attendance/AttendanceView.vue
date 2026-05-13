<script setup lang="ts">
// 考勤打卡页面
//
// 教学要点：
//   1. 用 el-date-picker 的 daterange 类型，绑定到 [start, end] 数组
//   2. 表格里展示 employeeName，需要把员工 id 映射成姓名（这里直接调员工 page 接口拿到当前页）
//   3. 简单实现：先选员工，再上下班打卡；下面表格按日期范围查询历史

import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { attendanceApi } from '@/api/attendance'
import { employeeApi } from '@/api/employee'
import type { AttendanceRecord, Employee } from '@/types'

const employees = ref<Employee[]>([])
const loading = ref(false)
const records = ref<AttendanceRecord[]>([])

const form = reactive({
  employeeId: undefined as number | undefined,
  note: ''
})

const range = ref<[string, string] | null>(null)

const empMap = computed(() => {
  const map = new Map<number, string>()
  employees.value.forEach((e) => map.set(e.id, e.name))
  return map
})

const STATUS_LABEL: Record<number, { text: string; tag: '' | 'success' | 'warning' | 'danger' }> = {
  1: { text: '正常', tag: 'success' },
  2: { text: '迟到', tag: 'warning' },
  3: { text: '早退', tag: 'danger' }
}

async function loadEmployees() {
  const result = await employeeApi.page({ pageNum: 1, pageSize: 50 })
  employees.value = result.list
  if (!form.employeeId && employees.value.length > 0) {
    form.employeeId = employees.value[0].id
  }
}

function defaultRange(): [string, string] {
  const today = new Date()
  const start = new Date(today.getTime() - 6 * 24 * 60 * 60 * 1000)
  const fmt = (d: Date) => d.toISOString().slice(0, 10)
  return [fmt(start), fmt(today)]
}

async function fetchRecords() {
  if (!form.employeeId || !range.value) return
  loading.value = true
  try {
    records.value = await attendanceApi.list({
      employeeId: form.employeeId,
      startDate: range.value[0],
      endDate: range.value[1]
    })
  } finally {
    loading.value = false
  }
}

async function handleCheckIn() {
  if (!form.employeeId) {
    ElMessage.warning('请先选择员工')
    return
  }
  await attendanceApi.checkIn({ employeeId: form.employeeId, note: form.note || undefined })
  ElMessage.success('上班打卡成功')
  fetchRecords()
}

async function handleCheckOut() {
  if (!form.employeeId) {
    ElMessage.warning('请先选择员工')
    return
  }
  await attendanceApi.checkOut({ employeeId: form.employeeId, note: form.note || undefined })
  ElMessage.success('下班打卡成功')
  fetchRecords()
}

watch(() => form.employeeId, fetchRecords)

onMounted(async () => {
  range.value = defaultRange()
  await loadEmployees()
  await fetchRecords()
})
</script>

<template>
  <div class="page-card">
    <div class="page-title">考勤打卡</div>

    <el-form :inline="true" class="filter">
      <el-form-item label="员工">
        <el-select v-model="form.employeeId" placeholder="选择员工" style="width: 180px">
          <el-option
            v-for="emp in employees"
            :key="emp.id"
            :label="emp.name + ' / ' + emp.phone"
            :value="emp.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.note" placeholder="可选，例如：到公司" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleCheckIn">上班打卡</el-button>
        <el-button type="success" @click="handleCheckOut">下班打卡</el-button>
      </el-form-item>
    </el-form>

    <el-divider />

    <el-form :inline="true" class="filter">
      <el-form-item label="日期范围">
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="fetchRecords">查询</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="records" v-loading="loading" border stripe>
      <el-table-column prop="workDate" label="日期" width="120" />
      <el-table-column label="员工" width="140">
        <template #default="{ row }">{{ empMap.get(row.employeeId) ?? row.employeeId }}</template>
      </el-table-column>
      <el-table-column prop="checkInTime" label="上班时间" width="200" />
      <el-table-column prop="checkOutTime" label="下班时间" width="200" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="STATUS_LABEL[row.status]?.tag ?? ''">
            {{ STATUS_LABEL[row.status]?.text ?? row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="note" label="备注" />
    </el-table>
  </div>
</template>

<style scoped>
.filter {
  margin-top: 12px;
  margin-bottom: 8px;
}
</style>
