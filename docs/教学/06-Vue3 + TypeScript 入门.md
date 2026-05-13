# 06 Vue3 + TypeScript 入门

本章把"前端为什么写成现在这样"讲清楚，重点在 Vue3 组合式 API 与 TypeScript 基础语法。

## 1. 现代前端项目长什么样

```text
frontend/
├── package.json        ← 依赖、脚本命令
├── tsconfig.json       ← TypeScript 配置
├── vite.config.ts      ← Vite 配置（开发服务器、构建）
├── index.html          ← 唯一一个静态页面（壳子）
└── src/
    ├── main.ts         ← 程序入口
    ├── App.vue         ← 根组件
    ├── api/            ← 后端接口封装
    ├── assets/         ← 静态资源（CSS、图片）
    ├── components/     ← 通用组件
    ├── router/         ← Vue Router 路由
    ├── stores/         ← Pinia 状态
    ├── types/          ← TS 类型定义
    ├── utils/          ← 工具函数（含 axios 实例）
    └── views/          ← 页面级组件
```

要点：

- **整个应用只有一个 `index.html`**，加载 `main.ts` 后，所有页面切换都靠 JS 在前端完成 —— 这叫 **SPA（单页应用）**。
- 各个 `.vue` 文件是"组件"，里面同时写 HTML 模板、JS 逻辑、CSS 样式。
- 项目跑起来需要 Node.js + Vite 把 `.vue` / `.ts` 编译成浏览器能跑的 `.js`。

## 2. Vue 3 的两种写法：选项式 vs 组合式

### 选项式 API（Vue 2 风格）

```vue
<script lang="ts">
export default {
  data() {
    return { count: 0 }
  },
  methods: {
    inc() { this.count++ }
  }
}
</script>
```

### 组合式 API（Vue 3 推荐，本项目使用）

```vue
<script setup lang="ts">
import { ref } from 'vue'

const count = ref(0)
function inc() { count.value++ }
</script>

<template>
  <button @click="inc">{{ count }}</button>
</template>
```

为什么用组合式？

- **逻辑组合更灵活**：把相关的状态、方法、生命周期写在一起，不像选项式被强制按 `data/methods/computed` 切开。
- **TypeScript 支持更好**：变量类型一目了然。
- **`<script setup>`**：是组合式的语法糖，不用再写 `export default { setup() { ... } }`。

## 3. `ref` 与 `reactive`：响应式核心

```ts
import { ref, reactive } from 'vue'

const count = ref(0)              // 原始值：用 ref
const user = reactive({           // 对象：用 reactive
  name: '小明',
  age: 18
})

// 修改
count.value++                     // ref 需要 .value
user.age = 19                     // reactive 直接改

// 模板里不用 .value
// <div>{{ count }}</div>
```

| 场景 | 推荐 |
| --- | --- |
| number / string / boolean | `ref` |
| 对象（小型表单等） | `reactive` |
| 复杂数据 / 跨组件共享 | Pinia store（其实 store 也是 reactive） |

> 经验：能用 `ref` 就用 `ref`，统一 `.value` 反而不容易出错；只有需要 `v-model` 直接绑到对象属性时再用 `reactive`。

## 4. computed 和 watch

```ts
import { computed, watch } from 'vue'

const firstName = ref('张')
const lastName = ref('三')
const fullName = computed(() => firstName.value + lastName.value)

watch(firstName, (newVal, oldVal) => {
  console.log('姓变了', oldVal, '->', newVal)
})
```

- `computed`：依赖响应式数据自动重新计算，有缓存。
- `watch`：监听变化，做副作用（请求、跳路由）。

## 5. 生命周期钩子

```ts
import { onMounted, onUnmounted } from 'vue'

onMounted(() => {
  console.log('组件挂载完毕，可以拉数据了')
})

onUnmounted(() => {
  console.log('组件销毁，清理定时器、事件监听等')
})
```

常用：

| 钩子 | 时机 |
| --- | --- |
| `onMounted` | 第一次插入 DOM 后 |
| `onUpdated` | 数据变化导致 DOM 重新渲染后 |
| `onUnmounted` | 组件销毁前 |
| `onBeforeMount` / `onBeforeUpdate` | 对应"前置" |

## 6. 模板语法精选

### 文本插值

```vue
<div>{{ user.name }}</div>
<div>{{ count > 0 ? '正数' : '非正数' }}</div>
```

### 属性绑定 `v-bind` / `:`

```vue
<img :src="user.avatar" :alt="user.name" />
```

### 条件渲染 `v-if` / `v-else-if` / `v-else` / `v-show`

```vue
<el-tag v-if="row.status === 1" type="success">在职</el-tag>
<el-tag v-else type="info">离职</el-tag>
```

- `v-if`：根据条件**新增/移除 DOM 节点**。
- `v-show`：永远渲染节点，只切换 `display:none`，适合频繁切换。

### 列表渲染 `v-for`

```vue
<el-option
  v-for="dept in departmentStore.departments"
  :key="dept.id"
  :label="dept.name"
  :value="dept.id"
/>
```

`:key` 一定要写，必须唯一。

### 事件绑定 `v-on` / `@`

```vue
<el-button @click="handleSearch">查询</el-button>
<el-input @input="onInput" />
<el-input @keyup.enter="handleSearch" />   <!-- 按回车键触发 -->
```

### 双向绑定 `v-model`

```vue
<el-input v-model="form.name" />
```

等价于：

```vue
<el-input :model-value="form.name" @update:model-value="(v) => form.name = v" />
```

> 在 Element Plus 里很多组件支持 `v-model`，例如 `el-input`、`el-select`、`el-date-picker`、`el-dialog`（绑显隐）。

## 7. 组件与 props / emit

### 定义组件

`components/UserCard.vue`：

```vue
<script setup lang="ts">
defineProps<{
  name: string
  age?: number
}>()

const emit = defineEmits<{
  (e: 'click-name', name: string): void
}>()
</script>

<template>
  <div class="card" @click="emit('click-name', name!)">
    {{ name }} - {{ age ?? '未知' }}
  </div>
</template>
```

### 使用组件

```vue
<script setup lang="ts">
import UserCard from '@/components/UserCard.vue'
function onClickName(name: string) { console.log(name) }
</script>

<template>
  <UserCard name="小明" :age="18" @click-name="onClickName" />
</template>
```

要点：

- `defineProps` / `defineEmits` 是宏，编译期会被处理，不用 `import`。
- props 用 `:` 传响应式数据，否则就是字面量。
- 父传子用 props，子传父用 emit；跨层级共享用 Pinia。

## 8. TypeScript 基础（够用就好）

### 类型注解

```ts
let n: number = 1
let s: string = 'hello'
let arr: number[] = [1, 2, 3]
let user: { name: string; age: number } = { name: '小明', age: 18 }
```

### 类型推断

```ts
let n = 1            // 推断为 number
let arr = [1, 'x']   // 推断为 (string | number)[]
```

### interface / type

```ts
interface Employee {
  id: number
  name: string
  email?: string       // 可选字段
}

type EmployeeStatus = 0 | 1            // 字面量联合类型
type Pair<T> = [T, T]                  // 泛型 type
```

`interface` 适合描述对象、可以被多次声明合并、能 `extends`；`type` 适合联合 / 函数 / 元组类型。**初学者推荐：对象用 `interface`，其他用 `type`**。

### 泛型

```ts
function wrap<T>(value: T): { data: T } {
  return { data: value }
}
const r = wrap('hi')   // r 类型是 { data: string }
```

项目里 `request.get<T>(...)` 的 `T` 就是返回数据的类型。

### 联合类型与类型守卫

```ts
function format(x: number | string): string {
  if (typeof x === 'number') return x.toFixed(2)
  return x.trim()
}
```

### Optional Chaining 和 Nullish Coalescing

```ts
const name = user?.profile?.name ?? '未命名'
```

- `?.`：链路上有 null/undefined 就短路返回 undefined
- `??`：只有左边是 null/undefined 才用右边

### 严格模式带来的好处

`tsconfig.json` 启用了 `strict: true`，意味着：

- 不能给 `undefined` 赋值给 `string` 类型
- 函数参数没用要么删掉要么加 `_` 前缀
- 实测能挡掉 60% 的"小心心"bug

## 9. `<style scoped>` 与样式作用域

```vue
<style scoped>
.title { color: red; }
</style>
```

加了 `scoped`，编译后 Vue 会给每个元素加一个 `data-v-xxx` 属性，CSS 选择器自动加这个属性后缀，只影响当前组件，不污染全局。

要穿透到子组件（比如 Element Plus 内部 DOM）：

```css
.menu :deep(.el-menu-item) {
  color: #fff;
}
```

## 10. 项目里的 `App.vue` 与 `main.ts` 解读

```ts
// main.ts
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
```

- `createApp(App)`：根组件
- `app.use(plugin)`：每个生态插件（Pinia、Router、Element Plus）都通过 `use` 安装
- `app.mount('#app')`：把整个 Vue 实例挂到 `index.html` 里的 `<div id="app"></div>`

```vue
<!-- App.vue -->
<template>
  <RouterView />
</template>
```

`<RouterView />` 是占位符，会被当前路由对应的组件填充。

## 11. 课后练习

1. 写一个 `Counter.vue` 组件：有"加 1 / 减 1 / 重置"三个按钮，count 用 `ref<number>` 维护。
2. 把员工列表里"在职/离职"的渲染逻辑改成一个独立组件 `EmployeeStatusTag.vue`，通过 props 接收 `status`。
3. 写一个 `useTime()` 自定义组合函数（hook），返回当前时间字符串，每秒自动更新。

下一章：[07-Element Plus 组件库](./07-Element%20Plus%20组件库.md)
