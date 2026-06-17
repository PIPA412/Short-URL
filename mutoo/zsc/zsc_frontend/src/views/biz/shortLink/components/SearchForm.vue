<template>
  <el-form :model="queryParams" ref="queryFormRef" :inline="true" v-show="showSearch" label-width="80px">
    <el-form-item label="标题" prop="title">
      <el-input
        v-model="queryParams.title"
        placeholder="请输入标题"
        clearable
        style="width: 200px"
        @keyup.enter="handleQuery"
      />
    </el-form-item>
    <el-form-item label="短码" prop="shortCode">
      <el-input
        v-model="queryParams.shortCode"
        placeholder="请输入短码"
        clearable
        style="width: 200px"
        @keyup.enter="handleQuery"
      />
    </el-form-item>
    <el-form-item label="原始URL" prop="originalUrl">
      <el-input
        v-model="queryParams.originalUrl"
        placeholder="请输入原始URL"
        clearable
        style="width: 240px"
        @keyup.enter="handleQuery"
      />
    </el-form-item>
    <el-form-item label="状态" prop="status">
      <el-select
        v-model="queryParams.status"
        placeholder="请选择状态"
        clearable
        style="width: 200px"
      >
        <el-option
          v-for="dict in sys_normal_disable"
          :key="dict.value"
          :label="dict.label"
          :value="dict.value"
        />
      </el-select>
    </el-form-item>
    <el-form-item label="创建时间" style="width: 308px">
      <el-date-picker
        v-model="dateRange"
        value-format="YYYY-MM-DD"
        type="daterange"
        range-separator="-"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
      <el-button icon="Refresh" @click="resetQuery">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup name="ShortLinkSearch">
const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

const props = defineProps({
  queryParams: {
    type: Object,
    required: true
  },
  showSearch: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['query', 'reset'])

const queryFormRef = ref(null)
const dateRange = ref([])

/** 搜索按钮操作 */
function handleQuery() {
  emit('query', dateRange.value)
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = []
  proxy.resetForm("queryFormRef")
  emit('reset')
}

defineExpose({
  queryFormRef
})
</script>
