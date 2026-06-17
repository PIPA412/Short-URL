<template>
  <el-dialog
    :model-value="visible"
    :title="isEdit ? '编辑短链接' : '创建短链接'"
    width="560px"
    :close-on-click-modal="false"
    @update:model-value="$emit('update:visible', $event)"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" v-loading="isLoadingDetail">
      <el-form-item label="原始链接" prop="originalUrl">
        <el-input v-model="form.originalUrl" placeholder="请输入长链接 URL" />
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" placeholder="给链接起个名字（选填）"
          maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" type="textarea" :rows="2"
          placeholder="备注说明（选填）" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="过期时间" prop="expireTime">
        <el-date-picker
          v-model="form.expireTime"
          type="datetime"
          placeholder="选择过期时间，留空表示永不过期"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item v-if="isEdit" label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="0">正常</el-radio>
          <el-radio :value="1">已过期</el-radio>
          <el-radio :value="2">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="$emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        {{ isEdit ? '保存' : '创建' }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { createShortLink, updateShortLink, getShortLinkById } from '@/api/shortLink'

const props = defineProps({
  visible: { type: Boolean, default: false },
  editData: { type: Object, default: null }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const submitting = ref(false)
const isLoadingDetail = ref(false)

const isEdit = computed(() => !!props.editData?.id)

const defaultForm = () => ({
  id: null, originalUrl: '', title: '',
  description: '', expireTime: null, status: 0
})

const form = reactive(defaultForm())

const rules = {
  originalUrl: [
    { required: true, message: '请输入原始链接', trigger: 'blur' },
    { max: 2048, message: '链接长度不能超过2048个字符', trigger: 'blur' }
  ]
}

watch(() => props.visible, async (val) => {
  if (!val) return

  if (props.editData?.id) {
    // === Edit mode: load full detail from backend ===
    isLoadingDetail.value = true
    try {
      const res = await getShortLinkById(props.editData.id)
      const data = res.data
      Object.assign(form, {
        id: data.id,
        originalUrl: data.originalUrl || '',
        title: data.title || '',
        description: data.description || '',
        expireTime: data.expireTime || null,
        status: data.status ?? 0
      })
    } catch {
      ElMessage.error('加载短链接详情失败')
      emit('update:visible', false)
    } finally {
      isLoadingDetail.value = false
    }
  } else {
    // === Create mode: reset to defaults ===
    Object.assign(form, defaultForm())
    // Reset validation state
    formRef.value?.resetFields()
  }
})

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (isEdit.value) {
      await updateShortLink({
        id: form.id,
        originalUrl: form.originalUrl,
        title: form.title || undefined,
        description: form.description || undefined,
        expireTime: form.expireTime || undefined,
        status: form.status
      })
      ElMessage.success('更新成功')
    } else {
      await createShortLink({
        originalUrl: form.originalUrl,
        title: form.title || undefined,
        description: form.description || undefined,
        expireTime: form.expireTime || undefined
      })
      ElMessage.success('创建成功')
    }
    emit('success')
  } catch {
    // Handled by request interceptor
  } finally {
    submitting.value = false
  }
}
</script>
