<template>
  <div class="link-management">
    <el-card>
      <!-- Search & Actions Bar -->
      <div class="toolbar">
        <el-form :inline="true" :model="query" size="default">
          <el-form-item label="标题">
            <el-input v-model="query.title" placeholder="搜索标题" clearable
              @clear="handleSearch" @keyup.enter="handleSearch" style="width: 180px;" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable
              @change="handleSearch" style="width: 120px;">
              <el-option label="正常" :value="0" />
              <el-option label="已过期" :value="1" />
              <el-option label="已禁用" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
        <el-button type="primary" @click="openCreateDialog">
          <el-icon><Plus /></el-icon>
          创建短链
        </el-button>
      </div>

      <!-- Data Table -->
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%" empty-text="暂无数据">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="shortCode" label="短码" width="120" />
        <el-table-column label="短链接" min-width="280">
          <template #default="{ row }">
            <el-link :href="row.shortUrl" target="_blank" type="primary" :underline="false">
              {{ row.shortUrl }}
            </el-link>
            <el-button link type="primary" size="small"
              @click="handleCopyUrl(row.shortUrl)">
              <el-icon><CopyDocument /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.title || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="originalUrl" label="原始链接" min-width="200" show-overflow-tooltip />
        <el-table-column prop="clickCount" label="点击量" width="100" align="center" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="plain">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small"
              @click="$router.push(`/links/${row.id}`)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button link type="primary" size="small"
              @click="openEditDialog(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-popconfirm
              title="确定要删除此短链接吗？"
              confirm-button-text="确定"
              cancel-button-text="取消"
              @confirm="handleDelete(row.id)"
            >
              <template #reference>
                <el-button link type="danger" size="small">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSearch"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <!-- Create/Edit Dialog -->
    <FormDialog
      v-model:visible="dialogVisible"
      :edit-data="currentEdit"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus, CopyDocument, View, Edit, Delete } from '@element-plus/icons-vue'
import { getShortLinkList, deleteShortLink } from '@/api/shortLink'
import FormDialog from './FormDialog.vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const currentEdit = ref(null)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  status: null
})

const statusTagType = (status) => ({ 0: 'success', 1: 'warning', 2: 'danger' }[status] || 'info')

const fetchData = async () => {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.title) params.title = query.title
    if (query.status !== null && query.status !== '') params.status = query.status

    const res = await getShortLinkList(params)
    tableData.value = res.data?.rows || []
    total.value = res.data?.total || 0
  } catch {
    // Handled by request interceptor
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { query.pageNum = 1; fetchData() }

const handleReset = () => { query.title = ''; query.status = null; handleSearch() }

const openCreateDialog = () => { currentEdit.value = null; dialogVisible.value = true }

const openEditDialog = (row) => { currentEdit.value = { id: row.id }; dialogVisible.value = true }

const handleDelete = async (id) => {
  try {
    await deleteShortLink(id)
    ElMessage.success('删除成功')
    if (tableData.value.length === 1 && query.pageNum > 1) query.pageNum--
    fetchData()
  } catch { /* handled by interceptor */ }
}

const handleDialogSuccess = () => { dialogVisible.value = false; fetchData() }

const handleCopyUrl = async (url) => {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(url)
    } else {
      const ta = document.createElement('textarea')
      ta.value = url; ta.style.position = 'fixed'; ta.style.opacity = '0'
      document.body.appendChild(ta); ta.select()
      document.execCommand('copy'); document.body.removeChild(ta)
    }
    ElMessage.success('已复制到剪贴板')
  } catch { ElMessage.warning('复制失败，请手动复制') }
}

onMounted(() => fetchData())
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 20px; flex-wrap: wrap; gap: 10px; }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>
