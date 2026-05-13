<script setup lang="ts">
// 部门管理页面
//
// 教学要点：
//   1. onMounted 是生命周期钩子，在组件挂载后执行一次
//   2. ref 创建响应式数据，模板里直接用变量名（不用 .value）
//   3. v-model 双向绑定表单字段
//   4. ElMessage 用 await 调 API 失败时的 catch 由 request.ts 拦截器统一处理
//   5. 用 reactive 包对象的 form，跟 ref 的简单数据互补

import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { departmentApi } from '@/api/department'
import type { DepartmentCreateRequest } from '@/types'
import { useDepartmentStore } from '@/stores/department'

const store = useDepartmentStore()
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive<DepartmentCreateRequest>({
  code: '',
  name: ''
})

const rules: FormRules = {
  code: [
    { required: true, message: '请输入部门编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]{2,20}$/, message: '只能 2-20 位大写字母/数字/下划线', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入部门名称', trigger: 'blur' },
    { min: 1, max: 64, message: '部门名称最多 64 个字符', trigger: 'blur' }
  ]
}

function openCreateDialog() {
  form.code = ''
  form.name = ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  submitting.value = true
  try {
    await departmentApi.create({ code: form.code.trim(), name: form.name.trim() })
    ElMessage.success('新增部门成功')
    dialogVisible.value = false
    await store.refresh(true)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  store.refresh()
})
</script>

<template>
  <div class="page-card">
    <div class="page-toolbar">
      <div class="page-title">部门列表</div>
      <el-button type="primary" @click="openCreateDialog">新增部门</el-button>
    </div>

    <el-table :data="store.departments" v-loading="store.loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="code" label="编码" width="160" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增部门" width="420px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="例如 RD / OPS / HR" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="例如 研发部" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>
