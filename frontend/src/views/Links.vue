<script setup>
import { reactive, ref, onMounted } from 'vue'
import {
  getShortLinkList, createShortLink, updateShortLink,
  deleteShortLink, getAccessLogs,
} from '../api/shortlink'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const createVisible = ref(false)
const editVisible = ref(false)
const logVisible = ref(false)
const createFormRef = ref(null)
const editFormRef = ref(null)
const submitting = ref(false)

const createForm = reactive({ originalUrl: '', expireTime: null })
const editForm = reactive({ id: null, originalUrl: '', expireTime: null, status: 1 })
const logData = ref([])
const logTotal = ref(0)
const logPageNum = ref(1)
const currentShortLinkId = ref(null)

const rules = {
  originalUrl: [{ required: true, message: '请输入原始链接', trigger: 'blur' }],
}

async function fetchList() {
  loading.value = true
  try {
    const data = await getShortLinkList({ pageNum: pageNum.value, pageSize: pageSize.value })
    tableData.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function onPageChange(page) {
  pageNum.value = page
  fetchList()
}

function showCreate() {
  createForm.originalUrl = ''
  createForm.expireTime = null
  createVisible.value = true
}

async function onCreate() {
  const valid = await createFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createShortLink(createForm)
    createVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

function showEdit(row) {
  editForm.id = row.id
  editForm.originalUrl = row.originalUrl
  editForm.expireTime = row.expireTime
  editForm.status = row.status
  editVisible.value = true
}

async function onEdit() {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await updateShortLink(editForm.id, {
      originalUrl: editForm.originalUrl,
      expireTime: editForm.expireTime,
      status: editForm.status,
    })
    editVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function onDelete(row) {
  try {
    await deleteShortLink(row.id)
    fetchList()
  } finally {
    // confirm
  }
}

async function showLog(row) {
  currentShortLinkId.value = row.id
  logPageNum.value = 1
  await fetchLogs()
  logVisible.value = true
}

async function fetchLogs() {
  const data = await getAccessLogs({
    shortLinkId: currentShortLinkId.value,
    pageNum: logPageNum.value,
    pageSize: 10,
  })
  logData.value = data.records
  logTotal.value = data.total
}

function onLogPageChange(page) {
  logPageNum.value = page
  fetchLogs()
}

function copyUrl(row) {
  const url = `http://localhost:8080/${row.shortCode}`
  navigator.clipboard.writeText(url)
}

onMounted(fetchList)
</script>

<template>
  <div class="links-page">
    <el-card>
      <div class="toolbar">
        <h3>短链接管理</h3>
        <el-button type="primary" @click="showCreate">创建短链接</el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="shortCode" label="短码" width="120" />
        <el-table-column label="短链接" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" :href="row.shortUrl" target="_blank">
              {{ row.shortUrl }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="originalUrl" label="原始链接" min-width="200" show-overflow-tooltip />
        <el-table-column prop="clickCount" label="点击量" width="90" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="170">
          <template #default="{ row }">
            {{ row.expireTime || '永久有效' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" text @click="copyUrl(row)">复制</el-button>
            <el-button type="primary" size="small" text @click="showLog(row)">日志</el-button>
            <el-button type="primary" size="small" text @click="showEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该短链接？" @confirm="onDelete(row)">
              <template #reference>
                <el-button type="danger" size="small" text>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="total"
          :page-size="pageSize"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <!-- 创建弹窗 -->
    <el-dialog v-model="createVisible" title="创建短链接" width="520px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="rules" label-position="top">
        <el-form-item label="原始链接" prop="originalUrl">
          <el-input v-model="createForm.originalUrl" placeholder="请输入长链接" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="createForm.expireTime" type="datetime"
            placeholder="选择过期时间（可选）" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onCreate">生成</el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editVisible" title="编辑短链接" width="520px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-position="top">
        <el-form-item label="原始链接" prop="originalUrl">
          <el-input v-model="editForm.originalUrl" placeholder="请输入长链接" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="editForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker v-model="editForm.expireTime" type="datetime"
            placeholder="选择过期时间（可选）" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 访问日志弹窗 -->
    <el-dialog v-model="logVisible" title="访问日志" width="700px">
      <el-table :data="logData" border stripe size="small" max-height="400">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="referer" label="来源" min-width="160" show-overflow-tooltip />
        <el-table-column prop="userAgent" label="User Agent" min-width="200" show-overflow-tooltip />
        <el-table-column prop="accessTime" label="访问时间" width="170" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next, total"
          :total="logTotal"
          :page-size="10"
          @current-change="onLogPageChange"
        />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.links-page { max-width: 1200px; margin: 24px auto; }
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
