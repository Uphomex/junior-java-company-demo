<script setup lang="ts">
// 员工管理页面
//
// 教学要点：
//   1. ref + reactive 配合使用：列表/总数等独立字段用 ref，查询条件/表单用 reactive
//   2. computed 把"部门 id → 部门名称"映射做成派生数据，避免每行手动 find
//   3. 表单提交分"新增/编辑"两种模式，靠 mode 状态切换；UI 复用同一个对话框
//   4. el-form 的 rules 与 DTO 校验保持一致，前端先拦一道，体验更好

import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { employeeApi } from '@/api/employee'
import { useDepartmentStore } from '@/stores/department'
import type { Employee, EmployeeCreateRequest } from '@/types'

const departmentStore = useDepartmentStore()

const list = ref<Employee[]>([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  keyword: '',
  departmentId: undefined as number | undefined,
  pageNum: 1,
  pageSize: 10
})

async function fetchList() {
  loading.value = true
  try {
    const result = await employeeApi.page(query)
    list.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchList()
}

function handleReset() {
  query.keyword = ''
  query.departmentId = undefined
  query.pageNum = 1
  fetchList()
}

const deptMap = computed(() => {
  const map = new Map<number, string>()
  departmentStore.departments.forEach((d) => map.set(d.id, d.name))
  return map
})

// ---------------- 新增 / 编辑 ----------------

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editingId = ref<number | null>(null)

const form = reactive<EmployeeCreateRequest>({
  name: '',
  phone: '',
  email: '',
  departmentId: null,
  position: '',
  hireDate: '',
  salary: null
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  position: [{ required: true, message: '请输入岗位', trigger: 'blur' }],
  hireDate: [{ required: true, message: '请选择入职日期', trigger: 'change' }],
  salary: [{ required: true, message: '请输入薪资', trigger: 'blur' }]
}

function openCreate() {
  dialogMode.value = 'create'
  editingId.value = null
  form.name = ''
  form.phone = ''
  form.email = ''
  form.departmentId = null
  form.position = ''
  form.hireDate = ''
  form.salary = null
  dialogVisible.value = true
}

function openEdit(row: Employee) {
  dialogMode.value = 'edit'
  editingId.value = row.id
  form.name = row.name
  form.phone = row.phone
  form.email = row.email ?? ''
  form.departmentId = row.departmentId
  form.position = row.position
  form.hireDate = row.hireDate
  form.salary = row.salary
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await employeeApi.create(form)
      ElMessage.success('新增员工成功')
    } else if (editingId.value != null) {
      await employeeApi.update(editingId.value, form)
      ElMessage.success('修改员工成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleLeave(row: Employee) {
  await ElMessageBox.confirm(`确认让"${row.name}"办理离职吗？`, '提示', {
    type: 'warning'
  })
  await employeeApi.leave(row.id)
  ElMessage.success('已办理离职')
  fetchList()
}

onMounted(async () => {
  await departmentStore.refresh()
  await fetchList()
})
</script>

<template>
  <div class="page-card">
    <div class="page-toolbar">
      <div class="page-title">员工列表</div>
      <el-button type="primary" @click="openCreate">新增员工</el-button>
    </div>

    <el-form :inline="true" class="filter">
      <el-form-item label="关键字">
        <el-input v-model="query.keyword" placeholder="姓名/手机号" clearable />
      </el-form-item>
      <el-form-item label="部门">
        <el-select v-model="query.departmentId" placeholder="全部部门" clearable style="width: 180px">
          <el-option
            v-for="dept in departmentStore.departments"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="部门" width="140">
        <template #default="{ row }">{{ deptMap.get(row.departmentId) ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="position" label="岗位" width="160" />
      <el-table-column prop="hireDate" label="入职日期" width="120" />
      <el-table-column prop="salary" label="薪资" width="100" align="right">
        <template #default="{ row }">¥ {{ Number(row.salary).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '在职' : '离职' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button
            v-if="row.status === 1"
            size="small"
            link
            type="danger"
            @click="handleLeave(row)"
          >
            离职
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pagination"
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      :page-size="query.pageSize"
      :page-sizes="[10, 20, 50]"
      :current-page="query.pageNum"
      @current-change="(p: number) => { query.pageNum = p; fetchList() }"
      @size-change="(s: number) => { query.pageSize = s; query.pageNum = 1; fetchList() }"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增员工' : '编辑员工'"
      width="520px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="部门" prop="departmentId">
          <el-select v-model="form.departmentId" placeholder="选择部门" style="width: 100%">
            <el-option
              v-for="dept in departmentStore.departments"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <el-input v-model="form.position" />
        </el-form-item>
        <el-form-item label="入职日期" prop="hireDate">
          <el-date-picker
            v-model="form.hireDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="薪资" prop="salary">
          <el-input-number
            v-model="form.salary"
            :min="0"
            :precision="2"
            :step="500"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter {
  margin-bottom: 12px;
}

.pagination {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
