<template>
  <el-dialog :title="title" v-model="open" width="600px" append-to-body>
    <el-form ref="linkFormRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="原始URL" prop="originalUrl">
        <el-input v-model="form.originalUrl" placeholder="请输入原始URL（如 https://www.example.com）" type="textarea" :rows="2" />
      </el-form-item>
      <el-form-item label="自定义短码" prop="shortCode">
        <el-input v-model="form.shortCode" placeholder="留空自动生成6位短码" maxlength="20">
          <template #append>
            <el-button @click="generateRandomCode">随机生成</el-button>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio value="0">正常</el-radio>
          <el-radio value="1">过期</el-radio>
          <el-radio value="2">禁用</el-radio>
          <el-radio value="3">异常</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="健康状态" prop="healthStatus">
        <el-radio-group v-model="form.healthStatus">
          <el-radio value="0">健康</el-radio>
          <el-radio value="1">异常</el-radio>
          <el-radio value="2">未知</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="过期时间" prop="expireTime">
        <el-date-picker
          v-model="form.expireTime"
          type="datetime"
          placeholder="选择过期时间（留空表示永不过期）"
          value-format="YYYY-MM-DD HH:mm:ss"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="访问密码" prop="accessPassword">
        <el-input v-model="form.accessPassword" placeholder="留空表示无需密码访问" type="text" maxlength="50" />
      </el-form-item>
      <el-form-item label="最大点击次数" prop="maxClicks">
        <el-input-number v-model="form.maxClicks" :min="0" :step="100" placeholder="留空表示无限制" style="width: 100%" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ShortLinkForm">
import { addShortLink, updateShortLink } from "@/api/biz/shortLink"

const { proxy } = getCurrentInstance()

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ""
  },
  formData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const linkFormRef = ref(null)

const open = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = ref({})
const rules = reactive({
  originalUrl: [{ required: true, message: "原始URL不能为空", trigger: "blur" }],
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

watch(() => props.formData, (newVal) => {
  form.value = { ...newVal }
}, { immediate: true, deep: true })

/** 生成随机短码 */
function generateRandomCode() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let code = ''
  for (let i = 0; i < 6; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  form.value.shortCode = code
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    shortCode: undefined,
    originalUrl: undefined,
    status: "0",
    healthStatus: "2",
    expireTime: undefined,
    accessPassword: undefined,
    maxClicks: undefined
  }
  proxy.resetForm("linkFormRef")
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["linkFormRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateShortLink(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          emit('success')
        })
      } else {
        addShortLink(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          emit('success')
        })
      }
    }
  })
}

defineExpose({
  linkFormRef,
  reset
})
</script>
