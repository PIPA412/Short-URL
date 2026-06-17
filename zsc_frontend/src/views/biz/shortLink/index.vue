<template>
  <div class="app-container">
    <!-- 搜索组件 -->
    <SearchForm
      ref="searchFormRef"
      :queryParams="queryParams"
      :showSearch="showSearch"
      @query="handleQuery"
      @reset="handleReset"
    />

    <!-- 列表组件 -->
    <ShortLinkTable
      :linkList="linkList"
      :loading="loading"
      :total="total"
      :queryParams="queryParams"
      :showSearch="showSearch"
      @update:showSearch="showSearch = $event"
      @update:currentPage="queryParams.currentPage = $event"
      @update:pageSize="queryParams.pageSize = $event"
      @add="handleAdd"
      @update="handleUpdate"
      @delete="handleDelete"
      @query="handleQuery"
      @pagination="getList"
    />

    <!-- 表单组件 -->
    <ShortLinkForm
      v-model="open"
      :title="title"
      :formData="form"
      ref="linkFormRef"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup name="ShortLink">
import { listShortLink, getShortLink } from "@/api/biz/shortLink"
import SearchForm from "./components/SearchForm.vue"
import ShortLinkTable from "./components/ShortLinkTable.vue"
import ShortLinkForm from "./components/ShortLinkForm.vue"

const { proxy } = getCurrentInstance()

const linkList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")

const queryParams = ref({
  currentPage: 1,
  pageSize: 10,
  shortCode: undefined,
  originalUrl: undefined,
  status: undefined
})

const form = ref({})

const dateRange = ref([])

const searchFormRef = ref(null)
const linkFormRef = ref(null)

/** 查询短链接列表 */
function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  listShortLink(params).then(response => {
    linkList.value = response.data.list
    total.value = response.data.total
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery(range) {
  dateRange.value = range || []
  queryParams.value.currentPage = 1
  getList()
}

/** 重置按钮操作 */
function handleReset() {
  dateRange.value = []
  handleQuery()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加短链接"
}

/** 修改按钮操作 */
function handleUpdate(linkId) {
  reset()
  getShortLink(linkId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改短链接"
  })
}

/** 删除按钮操作 */
function handleDelete() {
  getList()
}

/** 表单提交成功回调 */
function handleFormSuccess() {
  getList()
}

/** 表单重置 */
function reset() {
  form.value = {
    linkId: undefined,
    shortCode: undefined,
    originalUrl: undefined,
    title: undefined,
    status: "0",
    expireTime: undefined,
    remark: undefined
  }
  if (linkFormRef.value) {
    linkFormRef.value.reset()
  }
}

// 初始化加载数据
getList()
</script>
