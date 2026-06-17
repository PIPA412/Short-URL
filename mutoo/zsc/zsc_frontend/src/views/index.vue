<template>
  <div class="app-container shortlink-home">
    <!-- 标题区域 -->
    <div class="page-header">
      <h2>
        <el-icon :size="28"><Link /></el-icon>
        短链接生成器
      </h2>
      <p>输入长链接，一键生成短链接，方便分享与追踪</p>
    </div>

    <el-row :gutter="20">
      <!-- 生成表单 -->
      <el-col :xs="24" :sm="24" :md="14">
        <el-card shadow="hover">
          <template #header>
            <span class="card-title">生成短链接</span>
          </template>

          <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
            <el-form-item label="标题">
              <el-input
                v-model="form.title"
                placeholder="可选的标题，方便识别（如：618活动页）"
                clearable
                @keyup.enter="handleGenerate"
              />
            </el-form-item>
            <el-form-item label="原始URL" prop="originalUrl">
              <el-input
                v-model="form.originalUrl"
                placeholder="请输入长链接，如 https://www.example.com/very/long/url/path"
                clearable
                @keyup.enter="handleGenerate"
              />
            </el-form-item>

            <el-form-item label="自定义短码">
              <el-input
                v-model="form.shortCode"
                placeholder="留空自动生成6位随机码"
                clearable
                maxlength="20"
                @keyup.enter="handleGenerate"
              >
                <template #append>
                  <el-button @click="generateCode">随机</el-button>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item label="过期时间">
              <el-date-picker
                v-model="form.expireTime"
                type="datetime"
                placeholder="留空表示永不过期"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="备注">
              <el-input v-model="form.remark" placeholder="可选的备注信息" clearable />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="loading" @click="handleGenerate" size="large">
                <el-icon v-if="!loading"><MagicStick /></el-icon>
                {{ loading ? '生成中...' : '立即生成短链接' }}
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 生成结果 -->
          <div v-if="result" class="result-area">
            <el-divider />
            <el-alert title="生成成功！" type="success" :closable="false" show-icon />
            <div class="result-card">
              <div class="result-row">
                <span class="result-label">短链接：</span>
                <el-link type="success" :href="result.shortUrl" target="_blank" class="result-link">
                  {{ fullShortUrl }}
                </el-link>
                <el-button type="primary" size="small" plain @click="copyUrl">
                  <el-icon><CopyDocument /></el-icon> 复制
                </el-button>
              </div>
              <div class="result-row">
                <span class="result-label">短码：</span>
                <el-tag type="success" size="large">{{ result.shortCode }}</el-tag>
              </div>
              <div class="result-row">
                <span class="result-label">原始URL：</span>
                <span class="original-url">{{ result.originalUrl }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 统计面板 -->
      <el-col :xs="24" :sm="24" :md="10">
        <el-row :gutter="0" class="stats-col">
          <el-col :xs="12" :sm="12" :md="24">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-inner">
                <div class="stat-icon links-icon">
                  <el-icon :size="32"><Connection /></el-icon>
                </div>
                <div class="stat-body">
                  <div class="stat-value">{{ stats.totalLinks }}</div>
                  <div class="stat-label">短链接总数</div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="12" :sm="12" :md="24">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-inner">
                <div class="stat-icon clicks-icon">
                  <el-icon :size="32"><TrendCharts /></el-icon>
                </div>
                <div class="stat-body">
                  <div class="stat-value">{{ stats.totalClicks }}</div>
                  <div class="stat-label">总点击次数</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 今日热门 -->
        <el-card shadow="hover" style="margin-top: 20px">
          <template #header>
            <span class="card-title">最近创建</span>
          </template>
          <div v-if="recentLinks.length === 0" class="empty-tip">暂无数据</div>
          <div v-else class="recent-list">
            <div v-for="link in recentLinks" :key="link.linkId" class="recent-item">
              <div class="recent-title" :title="link.title || link.originalUrl">
                {{ link.title || link.originalUrl }}
              </div>
              <div class="recent-meta">
                <el-tag size="small" type="success">/s/{{ link.shortCode }}</el-tag>
                <span class="clicks">{{ link.clicks }} 次点击</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import { Link, MagicStick, CopyDocument, Connection, TrendCharts } from '@element-plus/icons-vue'
import { listShortLink, addShortLink } from '@/api/biz/shortLink'

const { proxy } = getCurrentInstance()

const formRef = ref(null)
const loading = ref(false)
const result = ref(null)

const form = reactive({
  title: '',
  originalUrl: '',
  shortCode: '',
  expireTime: undefined,
  remark: ''
})

const rules = reactive({
  originalUrl: [
    { required: true, message: '请输入原始URL', trigger: 'blur' },
    { pattern: /^https?:\/\/.+/i, message: 'URL 必须以 http:// 或 https:// 开头', trigger: 'blur' }
  ],
  expireTime: [
    {
      validator: (rule, value, callback) => {
        if (value && new Date(value).getTime() <= Date.now()) {
          callback(new Error('过期时间不能早于当前时间'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
})

const stats = reactive({
  totalLinks: 0,
  totalClicks: 0
})

const recentLinks = ref([])

const fullShortUrl = computed(() => {
  if (!result.value) return ''
  return window.location.origin + result.value.shortUrl
})

function generateCode() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let code = ''
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  form.shortCode = code
}

function handleGenerate() {
  formRef.value?.validate(valid => {
    if (!valid) return
    loading.value = true
    result.value = null

    const data = { originalUrl: form.originalUrl.trim(), status: '0' }
    if (form.title.trim()) data.title = form.title.trim()
    if (form.shortCode.trim()) data.shortCode = form.shortCode.trim()
    if (form.expireTime) data.expireTime = form.expireTime
    if (form.remark.trim()) data.remark = form.remark.trim()

    addShortLink(data).then(res => {
      result.value = res.data
      proxy.$modal.msgSuccess('短链接生成成功')
      loadStats()
    }).catch(() => {}).finally(() => {
      loading.value = false
    })
  })
}

function copyUrl() {
  navigator.clipboard.writeText(fullShortUrl.value).then(() => {
    proxy.$modal.msgSuccess('已复制到剪贴板')
  }).catch(() => {
    const input = document.createElement('input')
    input.value = fullShortUrl.value
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    proxy.$modal.msgSuccess('已复制到剪贴板')
  })
}

function resetForm() {
  form.title = ''
  form.originalUrl = ''
  form.shortCode = ''
  form.expireTime = undefined
  form.remark = ''
  result.value = null
  proxy.resetForm('formRef')
}

function loadStats() {
  listShortLink({ currentPage: 1, pageSize: 1 }).then(res => {
    stats.totalLinks = res.data.total || 0
  })
  listShortLink({ currentPage: 1, pageSize: 5 }).then(res => {
    recentLinks.value = res.data.list || []
    const totalClicks = recentLinks.value.reduce((sum, l) => sum + (l.clicks || 0), 0)
    stats.totalClicks = totalClicks
  })
}

loadStats()
</script>

<style scoped lang="scss">
.shortlink-home {
  .page-header {
    margin-bottom: 24px;
    h2 {
      margin: 0;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #303133;
    }
    p {
      margin: 8px 0 0;
      color: #909399;
      font-size: 14px;
    }
  }

  .card-title {
    font-weight: 600;
    font-size: 15px;
  }

  .result-area {
    .result-card {
      margin-top: 16px;
      padding: 20px;
      background: #f0f9eb;
      border-radius: 8px;
      border: 1px solid #e1f3d8;
    }
    .result-row {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      flex-wrap: wrap;
      &:last-child { margin-bottom: 0; }
    }
    .result-label {
      font-weight: 600;
      color: #606266;
      min-width: 75px;
    }
    .result-link {
      font-size: 15px;
    }
    .original-url {
      color: #409eff;
      word-break: break-all;
      font-size: 13px;
    }
  }

  .stats-col {
    .stat-card {
      margin-bottom: 0;
      .stat-inner {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 8px 0;
      }
      .stat-icon {
        width: 60px; height: 60px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        &.links-icon { background: linear-gradient(135deg, #667eea, #764ba2); color: #fff; }
        &.clicks-icon { background: linear-gradient(135deg, #11998e, #38ef7d); color: #fff; }
      }
      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #303133;
      }
      .stat-label {
        font-size: 13px;
        color: #909399;
        margin-top: 2px;
      }
    }
  }

  .empty-tip { text-align: center; color: #c0c4cc; padding: 30px 0; }

  .recent-list {
    .recent-item {
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;
      &:last-child { border-bottom: none; }
      .recent-title {
        color: #303133;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        margin-bottom: 6px;
      }
      .recent-meta {
        display: flex;
        align-items: center;
        justify-content: space-between;
        .clicks { color: #909399; font-size: 12px; }
      }
    }
  }
}
</style>
