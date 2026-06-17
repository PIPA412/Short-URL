<template>
  <div class="link-detail">
    <!-- Header -->
    <el-page-header @back="$router.push('/links')" title="返回列表">
      <template #content>
        <span v-if="link.title">{{ link.title }}</span>
        <span v-else>短链详情 #{{ linkId }}</span>
      </template>
      <template #extra>
        <el-button type="primary" size="small" @click="handleCopyUrl(link.shortUrl)">
          <el-icon><CopyDocument /></el-icon> 复制短链接
        </el-button>
      </template>
    </el-page-header>

    <!-- ================ Basic Info ================ -->
    <el-card v-loading="loading" class="section-card">
      <template #header>基本信息</template>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="短码">
          <el-tag type="primary">{{ link.shortCode }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="短链接">
          <el-link :href="link.shortUrl" target="_blank" type="primary">{{ link.shortUrl }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="原始链接" :span="2">
          <el-link :href="link.originalUrl" target="_blank" type="info" :underline="false">{{ link.originalUrl }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="标题">{{ link.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(link.status)">{{ link.statusDesc }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ link.createTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过期时间">{{ link.expireTime || '永不过期' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- ================ Summary Cards ================ -->
    <el-row :gutter="16" class="section-row">
      <el-col :span="6">
        <el-card shadow="hover"><el-statistic title="总点击量" :value="stats.totalClicks" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><el-statistic title="独立访客" :value="stats.uniqueIps" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover"><el-statistic title="今日点击" :value="stats.todayClicks" /></el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <el-statistic title="最后访问" :value="stats.lastAccessTime || '暂无'" :value-style="{ fontSize:'14px' }" />
        </el-card>
      </el-col>
    </el-row>

    <!-- ================ Trend Chart ================ -->
    <el-card class="section-card">
      <template #header>点击趋势（近7天）</template>
      <v-chart :option="trendOption" style="height:320px" autoresize />
    </el-card>

    <!-- ================ Distribution Charts ================ -->
    <el-row :gutter="16" class="section-row">
      <el-col :span="12">
        <el-card>
          <template #header>设备分布</template>
          <v-chart :option="deviceOption" style="height:280px" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>浏览器分布</template>
          <v-chart :option="browserOption" style="height:280px" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <!-- ================ Referer TOP10 ================ -->
    <el-card class="section-card" v-if="stats.refererTop10?.length">
      <template #header>来源网站 TOP10</template>
      <v-chart :option="refererOption" style="height:300px" autoresize />
    </el-card>

    <!-- ================ Access Log Table ================ -->
    <el-card class="section-card">
      <template #header>
        <span>访问明细</span>
        <el-button size="small" style="float:right" @click="fetchLogs" :loading="logLoading">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </template>

      <!-- Time range filter -->
      <el-form :inline="true" :model="logQuery" size="small" class="log-filter">
        <el-form-item label="开始时间">
          <el-date-picker v-model="logQuery.startDate" type="datetime"
            placeholder="开始时间" value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleLogSearch" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="logQuery.endDate" type="datetime"
            placeholder="结束时间" value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleLogSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogSearch">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="handleLogReset">
            <el-icon><RefreshLeft /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="logData" v-loading="logLoading" stripe size="small" empty-text="暂无访问记录">
        <el-table-column prop="id" label="#" width="70" />
        <el-table-column prop="accessIp" label="IP地址" width="140" />
        <el-table-column prop="deviceType" label="设备" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.deviceType === 'PC' ? '' : 'warning'">
              {{ row.deviceType || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="browser" label="浏览器" width="100" />
        <el-table-column prop="os" label="操作系统" width="120" />
        <el-table-column prop="referer" label="来源" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.referer || '直接访问' }}</template>
        </el-table-column>
        <el-table-column prop="accessTime" label="访问时间" width="160" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="logQuery.pageNum"
          v-model:page-size="logQuery.pageSize"
          :total="logTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background size="small"
          @size-change="fetchLogs"
          @current-change="fetchLogs" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CopyDocument, Refresh, Search, RefreshLeft } from '@element-plus/icons-vue'
import { getShortLinkById } from '@/api/shortLink'
import { getStats, getAccessLogs } from '@/api/stats'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  GridComponent, TooltipComponent, LegendComponent, TitleComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([LineChart, PieChart, BarChart,
  GridComponent, TooltipComponent, LegendComponent, TitleComponent, CanvasRenderer])

const route = useRoute()
const linkId = Number(route.params.id)
const loading = ref(false)
const logLoading = ref(false)

// ---- Link Info ----
const link = reactive({
  id: null, shortCode: '', shortUrl: '', originalUrl: '', title: '',
  status: 0, statusDesc: '', createTime: '', expireTime: ''
})

// ---- Stats Data ----
const stats = reactive({
  totalClicks: 0, uniqueIps: 0, todayClicks: 0, lastAccessTime: null,
  chartData: [], deviceDistribution: [], browserDistribution: [], refererTop10: []
})

// ---- Log Query ----
const logQuery = reactive({
  pageNum: 1, pageSize: 10, startDate: '', endDate: ''
})
const logData = ref([])
const logTotal = ref(0)

// ---- Helper ----
const statusTagType = (s) => ({ 0: 'success', 1: 'warning', 2: 'danger' }[s] || 'info')

// ---- ECharts Options ----

const baseColors = ['#409EFF','#67C23A','#E6A23C','#F56C6C','#909399','#5470C6','#91CC75','#FAC858']

const trendOption = computed(() => ({
  grid: { top: 10, right: 20, bottom: 30, left: 50 },
  xAxis: { type: 'category', data: stats.chartData?.map(p => p.date) || [] },
  yAxis: { type: 'value', minInterval: 1 },
  tooltip: { trigger: 'axis' },
  series: [{
    type: 'line', data: stats.chartData?.map(p => p.count) || [],
    smooth: true, areaStyle: { opacity: 0.15 }, itemStyle: { color: '#409EFF' }
  }]
}))

const deviceOption = pieOption(stats.deviceDistribution, '设备类型')
const browserOption = pieOption(stats.browserDistribution, '浏览器')

const refererOption = computed(() => ({
  grid: { top: 10, right: 20, bottom: 30, left: 150 },
  xAxis: { type: 'value' },
  yAxis: {
    type: 'category',
    data: (stats.refererTop10 || []).map(r => {
      const s = r.name || ''
      return s.length > 40 ? s.slice(0, 37) + '...' : s
    }).reverse()
  },
  tooltip: { trigger: 'axis', formatter: p => `${p[0].name}: ${p[0].value} 次` },
  series: [{
    type: 'bar',
    data: (stats.refererTop10 || []).map(r => r.value).reverse(),
    itemStyle: { color: '#409EFF', borderRadius: [0, 4, 4, 0] }
  }]
}))

function pieOption(data, title) {
  return computed(() => ({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', name: title, radius: ['40%', '70%'],
      data: (data || []).map(d => ({ name: d.name, value: d.value })),
      color: baseColors,
      label: { formatter: '{b}\n{d}%' }
    }]
  }))
}

// ---- Data Fetching ----

const fetchStats = async () => {
  try {
    const res = await getStats(linkId)
    Object.assign(stats, res.data || {})
  } catch { /* handled */ }
}

const fetchLogs = async () => {
  logLoading.value = true
  try {
    const params = { pageNum: logQuery.pageNum, pageSize: logQuery.pageSize }
    if (logQuery.startDate) params.startDate = logQuery.startDate
    if (logQuery.endDate) params.endDate = logQuery.endDate
    const res = await getAccessLogs(linkId, params)
    logData.value = res.data?.rows || []
    logTotal.value = res.data?.total || 0
  } catch { /* handled */ }
  finally { logLoading.value = false }
}

const handleLogSearch = () => { logQuery.pageNum = 1; fetchLogs() }
const handleLogReset = () => {
  logQuery.startDate = ''; logQuery.endDate = ''; handleLogSearch()
}

// ---- Copy URL ----

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
  } catch { ElMessage.warning('复制失败') }
}

// ---- Init ----

onMounted(async () => {
  loading.value = true
  try {
    const [linkRes] = await Promise.all([
      getShortLinkById(linkId),
      fetchStats(),
      fetchLogs()
    ])
    Object.assign(link, linkRes.data || {})
  } catch { /* handled */ }
  finally { loading.value = false }
})
</script>

<style scoped>
.section-card { margin-top: 16px; }
.section-row { margin-top: 16px; }
.log-filter { margin-bottom: 16px; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
