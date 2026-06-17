<script setup>
import { reactive, ref } from 'vue'
import { createShortLink } from '../api/shortlink'

const formRef = ref(null)
const generating = ref(false)
const result = ref(null)

const form = reactive({
  originalUrl: '',
  expireTime: null,
})

const rules = {
  originalUrl: [{ required: true, message: '请输入原始链接', trigger: 'blur' }],
}

async function onGenerate() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  generating.value = true
  try {
    result.value = await createShortLink({
      originalUrl: form.originalUrl,
      expireTime: form.expireTime || null,
    })
  } finally {
    generating.value = false
  }
}

function copyUrl() {
  if (!result.value) return
  navigator.clipboard.writeText(result.value.shortUrl)
}
</script>

<template>
  <div class="dashboard">
    <el-card class="create-card">
      <template #header>
        <span>创建短链接</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="原始链接" prop="originalUrl">
          <el-input v-model="form.originalUrl" placeholder="请输入需要缩短的长链接" />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="form.expireTime"
            type="datetime"
            placeholder="选择过期时间（可选）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="generating" @click="onGenerate">生成短链接</el-button>
        </el-form-item>
      </el-form>

      <div v-if="result" class="result-box">
        <el-alert title="短链接生成成功！" type="success" :closable="false" show-icon />
        <div class="result-url">
          <el-input v-model="result.shortUrl" readonly />
          <el-button type="primary" @click="copyUrl">复制</el-button>
        </div>
        <div class="result-meta">
          <span>原始链接：{{ result.originalUrl }}</span>
          <span v-if="result.expireTime">过期时间：{{ result.expireTime }}</span>
        </div>
      </div>
    </el-card>

    <div class="quick-links">
      <el-card>
        <template #header>快捷入口</template>
        <el-row :gutter="16">
          <el-col :span="8">
            <router-link to="/links" class="quick-item">
              <el-icon :size="32"><Link /></el-icon>
              <span>短链接管理</span>
            </router-link>
          </el-col>
        </el-row>
      </el-card>
    </div>
  </div>
</template>

<style scoped>
.dashboard { max-width: 800px; margin: 24px auto; }
.create-card { margin-bottom: 24px; }
.result-box { margin-top: 16px; }
.result-url {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}
.result-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 12px;
  font-size: 13px;
  color: #909399;
}
.quick-links { margin-top: 24px; }
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 24px;
  text-decoration: none;
  color: #303133;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  transition: all 0.3s;
}
.quick-item:hover {
  border-color: #409eff;
  color: #409eff;
}
</style>
