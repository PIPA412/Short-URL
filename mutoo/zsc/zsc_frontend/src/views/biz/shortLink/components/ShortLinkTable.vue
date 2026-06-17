<template>
  <div>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['biz:shortlink:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Edit"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['biz:shortlink:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="Delete"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['biz:shortlink:remove']"
        >删除</el-button>
      </el-col>
      <right-toolbar :showSearch="showSearch" @update:showSearch="$emit('update:showSearch', $event)" @queryTable="handleQuery"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="linkList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="链接ID" align="center" prop="linkId" width="80" />
      <el-table-column label="短码" align="center" prop="shortCode" width="120">
        <template #default="scope">
          <el-tag type="success">{{ scope.row.shortCode }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" min-width="120" />
      <el-table-column label="原始URL" align="center" prop="originalUrl" min-width="200" :show-overflow-tooltip="true">
        <template #default="scope">
          <el-link type="primary" :href="scope.row.originalUrl" target="_blank" :underline="false">
            {{ scope.row.originalUrl }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="短链接" align="center" width="200">
        <template #default="scope">
          <el-link type="success" :underline="false" @click="copyShortUrl(scope.row.shortCode)">
            /s/{{ scope.row.shortCode }}
          </el-link>
          <el-button link type="primary" icon="CopyDocument" @click="copyShortUrl(scope.row.shortCode)" style="margin-left: 4px" />
        </template>
      </el-table-column>
      <el-table-column label="点击量" align="center" prop="clicks" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <dict-tag :options="sys_normal_disable" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column label="过期时间" align="center" prop="expireTime" width="160">
        <template #default="scope">
          <span v-if="scope.row.expireTime">{{ parseTime(scope.row.expireTime) }}</span>
          <span v-else style="color: #909399">永不过期</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['biz:shortlink:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['biz:shortlink:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page="queryParams.currentPage"
      :limit="queryParams.pageSize"
      @update:page="$emit('update:currentPage', $event)"
      @update:limit="$emit('update:pageSize', $event)"
      @pagination="handlePagination"
    />
  </div>
</template>

<script setup name="ShortLinkTable">
import { delShortLink } from "@/api/biz/shortLink"

const { proxy } = getCurrentInstance()
const { sys_normal_disable } = proxy.useDict("sys_normal_disable")

const props = defineProps({
  linkList: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  total: {
    type: Number,
    default: 0
  },
  queryParams: {
    type: Object,
    required: true
  },
  showSearch: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['add', 'update', 'delete', 'query', 'pagination', 'update:showSearch', 'update:currentPage', 'update:pageSize'])

const ids = ref([])
const single = ref(true)
const multiple = ref(true)

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.linkId)
  single.value = selection.length != 1
  multiple.value = !selection.length
}

/** 复制短链接URL */
function copyShortUrl(shortCode) {
  const url = window.location.origin + '/s/' + shortCode
  navigator.clipboard.writeText(url).then(() => {
    proxy.$modal.msgSuccess("短链接已复制: " + url)
  }).catch(() => {
    proxy.$modal.msgError("复制失败，请手动复制")
  })
}

/** 新增按钮操作 */
function handleAdd() {
  emit('add')
}

/** 修改按钮操作 */
function handleUpdate(row) {
  const linkId = row.linkId || ids.value[0]
  emit('update', linkId)
}

/** 删除按钮操作 */
function handleDelete(row) {
  const linkIds = row.linkId || ids.value
  proxy.$modal.confirm('是否确认删除链接编号为"' + linkIds + '"的数据项？').then(function() {
    return delShortLink(linkIds)
  }).then(() => {
    emit('delete')
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

/** 搜索按钮操作 */
function handleQuery() {
  emit('query')
}

/** 分页操作 */
function handlePagination() {
  emit('pagination')
}
</script>
