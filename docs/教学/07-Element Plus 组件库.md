# 07 Element Plus 组件库

Element Plus 是 Element UI 的 Vue 3 版本，国内后台管理项目里出现频率排第一。本章不试图覆盖所有组件，重点讲项目里用到的几个，以及"看文档的姿势"。

文档地址（一定要常翻）：<https://element-plus.org/zh-CN/component/overview.html>

## 1. 全局安装与按需引入

本项目用最简单的"全量安装"：

```ts
// main.ts
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

app.use(ElementPlus, { locale: zhCn })
```

- `{ locale: zhCn }`：把组件内置的英文文案换成中文（分页器、确认框、日期选择等）。
- 项目大了优先用"自动按需引入"插件 `unplugin-vue-components` + `unplugin-auto-import` 减少打包体积。

## 2. 布局 Container

`MainLayout.vue` 用了 ElContainer 系列：

```vue
<el-container class="layout">
  <el-aside width="220px"> 左侧菜单 </el-aside>
  <el-container>
    <el-header> 顶部标题 </el-header>
    <el-main> 内容区 </el-main>
  </el-container>
</el-container>
```

特点：

- `<el-container>` 内部嵌套 `<el-aside>` / `<el-header>` / `<el-main>` 时会自动按横/纵方向布局。
- 不写 width 的 `<el-main>` 会自动占满剩余空间。

## 3. Menu 菜单

```vue
<el-menu :default-active="activeMenu" router>
  <el-menu-item index="/employees">员工管理</el-menu-item>
  <el-menu-item index="/departments">部门管理</el-menu-item>
</el-menu>
```

- `router` 属性让 `index` 自动当成路径用 Vue Router 跳转。
- `:default-active="activeMenu"`：响应式跟随当前路由高亮。

## 4. Form 表单

```vue
<el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
  <el-form-item label="姓名" prop="name">
    <el-input v-model="form.name" />
  </el-form-item>
  <el-form-item label="部门" prop="departmentId">
    <el-select v-model="form.departmentId" placeholder="请选择">
      <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
    </el-select>
  </el-form-item>
</el-form>
```

要点：

- **`:model="form"`**：表单数据对象，配合 `prop` 定位字段。
- **`prop="name"`**：和 `:rules` 里 key 对应，用于校验。
- **`:rules`**：校验规则数组，每个规则一个对象。
- **`ref="formRef"`**：拿到表单实例，可以调 `formRef.value?.validate()`、`resetFields()`。

校验规则：

```ts
import type { FormInstance, FormRules } from 'element-plus'

const formRef = ref<FormInstance>()
const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()      // 不通过会 throw
  // ... 通过则提交
}
```

| 属性 | 含义 |
| --- | --- |
| `required` | 必填 |
| `min` / `max` | 长度或数值范围 |
| `pattern` | 正则 |
| `type` | `'string' / 'number' / 'email' / 'url' / 'date'` |
| `validator` | 自定义函数 |
| `trigger` | `'blur' / 'change'` |

## 5. Input / Select / DatePicker / InputNumber

```vue
<el-input v-model="form.name" placeholder="请输入" clearable />

<el-select v-model="form.departmentId" placeholder="选择部门">
  <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
</el-select>

<el-date-picker
  v-model="form.hireDate"
  type="date"
  value-format="YYYY-MM-DD"
/>

<el-input-number v-model="form.salary" :min="0" :step="500" :precision="2" />
```

注意：

- DatePicker 加 `value-format="YYYY-MM-DD"` 后 `v-model` 拿到的就是字符串，方便直接传后端。
- InputNumber 的 `precision` 控制小数位。

## 6. Table 表格

```vue
<el-table :data="list" v-loading="loading" border stripe>
  <el-table-column prop="id" label="ID" width="80" />
  <el-table-column prop="name" label="姓名" width="120" />
  <el-table-column label="部门">
    <template #default="{ row }">{{ deptMap.get(row.departmentId) }}</template>
  </el-table-column>
  <el-table-column label="操作" width="160" fixed="right">
    <template #default="{ row }">
      <el-button size="small" link type="primary" @click="openEdit(row)">编辑</el-button>
    </template>
  </el-table-column>
</el-table>
```

要点：

- `:data="list"`：传一个数组。
- `prop`：当 cell 内容就是某个字段时，直接用 prop 即可。
- `<template #default="{ row }">`：自定义渲染，`row` 是当前行数据。
- `v-loading="loading"`：内置 loading 遮罩。
- `border` / `stripe`：边框 / 斑马纹。

## 7. Pagination 分页器

```vue
<el-pagination
  background
  layout="total, sizes, prev, pager, next, jumper"
  :total="total"
  :page-size="query.pageSize"
  :page-sizes="[10, 20, 50]"
  :current-page="query.pageNum"
  @current-change="(p) => { query.pageNum = p; fetchList() }"
  @size-change="(s) => { query.pageSize = s; query.pageNum = 1; fetchList() }"
/>
```

`layout` 字段决定显示哪些控件，顺序就是字面上的顺序。

## 8. Dialog 对话框

```vue
<el-dialog v-model="dialogVisible" title="新增员工" width="520px">
  <el-form ...> ... </el-form>
  <template #footer>
    <el-button @click="dialogVisible = false">取消</el-button>
    <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
  </template>
</el-dialog>
```

- `v-model` 绑显隐布尔值。
- `#footer` 是具名插槽，覆盖默认底部按钮。
- 自定义关闭：直接把 `dialogVisible.value = false` 即可。

## 9. Message / MessageBox 消息

```ts
import { ElMessage, ElMessageBox } from 'element-plus'

ElMessage.success('保存成功')
ElMessage.warning('请检查输入')
ElMessage.error('网络异常')

await ElMessageBox.confirm('确认要删除吗？', '提示', { type: 'warning' })
// 用户点取消会抛 'cancel'，直接被 try/catch 捕获即可
```

> 重要：组合式 API 里调 `ElMessage` 是函数式的，不需要 ref。

## 10. Icon 图标

```vue
<script lang="ts">
import { User, OfficeBuilding, Clock } from '@element-plus/icons-vue'
export default { components: { User, OfficeBuilding, Clock } }
</script>

<template>
  <el-icon><User /></el-icon>
</template>
```

- 需要单独安装 `@element-plus/icons-vue`
- 图标是组件，要先 import 再 register

## 11. Tag 标签

```vue
<el-tag :type="row.status === 1 ? 'success' : 'info'">
  {{ row.status === 1 ? '在职' : '离职' }}
</el-tag>
```

`type` 可选 `'' | 'success' | 'info' | 'warning' | 'danger'`，对应不同颜色。

## 12. 加载状态：`v-loading` 指令

```vue
<el-table :data="list" v-loading="loading"> ... </el-table>
```

也可以：

```vue
<div v-loading="loading" element-loading-text="加载中…">...</div>
```

## 13. 国际化 / 主题色

国际化已经在 `main.ts` 里设过 `zhCn`。要切英文：

```ts
import en from 'element-plus/dist/locale/en.mjs'
app.use(ElementPlus, { locale: en })
```

主题色可以全局 CSS 变量覆盖：

```css
:root {
  --el-color-primary: #1677ff;
  --el-color-primary-light-3: #4096ff;
}
```

## 14. 看文档的姿势

每个组件文档结构都一样：

1. **示例**：上来就是几张截图 + 代码片段，先抄一个跑起来。
2. **API**：
   - **Attributes（属性）**：写在标签上的 `xxx="..."`
   - **Events（事件）**：`@xxx="..."` 监听
   - **Slots（插槽）**：`<template #xxx>` 自定义内容
   - **Exposes（实例方法）**：通过 ref 调用，例如 `formRef.value.validate()`

记住这四块，看任何组件都不会迷路。

## 15. 课后练习

1. 给员工表格增加一列"详情"，点击弹出 ElDialog 显示员工完整信息（含考勤）。
2. 把"删除员工"做成 `ElMessageBox.confirm` 二次确认 + ElMessage 反馈。
3. 把部门管理改成支持 ElForm + ElInput 内联编辑（鼠标 hover 显示"编辑/保存/取消"按钮）。

下一章：[08-Pinia 与 Axios](./08-Pinia%20与%20Axios.md)
