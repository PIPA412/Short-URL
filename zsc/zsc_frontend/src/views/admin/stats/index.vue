<template>
  <div class="app-container stats-dashboard">
    <!-- 概览卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon users-icon"><el-icon :size="28"><User /></el-icon></div>
            <div class="stat-body">
              <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon links-icon"><el-icon :size="28"><Link /></el-icon></div>
            <div class="stat-body">
              <div class="stat-value">{{ stats.totalLinks || 0 }}</div>
              <div class="stat-label">总短链接数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-inner">
            <div class="stat-icon clicks-icon"><el-icon :size="28"><TrendCharts /></el-icon></div>
            <div class="stat-body">
              <div class="stat-value">{{ formatNumber(stats.totalClicks || 0) }}</div>
              <div class="stat-label">总点击量</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 每日新增趋势图表 -->
    <el-card shadow="hover" class="chart-card">
      <template #header><span class="card-title">每日新增链接趋势（近7天）</span></template>
      <div ref="chartRef" style="height: 320px;"></div>
    </el-card>

    <!-- 热门链接 + 活跃用户 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :xs="24" :sm="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">🔥 热门短链接 TOP10</span></template>
          <el-table :data="stats.topLinks || []" v-loading="loading" stripe size="small" max-height="400">
            <el-table-column label="短码" prop="short_code" width="100" />
            <el-table-column label="标题/URL" prop="title" :show-overflow-tooltip="true">
              <template #default="{ row }">
                {{ row.title || row.original_url }}
              </template>
            </el-table-column>
            <el-table-column label="点击量" prop="clicks" width="90" align="center" sortable />
            <el-table-column label="创建者" prop="create_by" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12">
        <el-card shadow="hover">
          <template #header><span class="card-title">👤 最活跃用户 TOP10</span></template>
          <el-table :data="stats.activeUsers || []" v-loading="loading" stripe size="small" max-height="400">
            <el-table-column type="index" label="#" width="50" />
            <el-table-column label="用户名" prop="create_by" />
            <el-table-column label="创建链接数" prop="link_count" width="110" align="center" sortable />
            <el-table-column label="总点击量" prop="total_clicks" width="100" align="center" sortable />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="AdminStats">
import { getDashboard } from '@/api/admin/stats'
import { User, Link, TrendCharts } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ref, reactive, watch, onMounted, nextTick } from 'vue'

const loading = ref(false)
const chartRef = ref(null)
let chartInstance = null

const stats = reactive({
  totalUsers: 0,
  totalLinks: 0,
  totalClicks: 0,
  dailyTrend: [],
  topLinks: [],
  activeUsers: []
})

function formatNumber(num) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return Number(num).toLocaleString()
}

function loadStats() {
  loading.value = true
  getDashboard().then(res => {
    Object.assign(stats, res.data)
    loading.value = false
    nextTick(() => renderChart())
  }).catch(() => {
    loading.value = false
  })
}

function renderChart() {
  const trend = stats.dailyTrend || []
  if (trend.length === 0) return

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }

  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: trend.map(t => t.date),
      axisLabel: { rotate: 0 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    series: [{
      name: '新增链接',
      type: 'line',
      data: trend.map(t => t.count),
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.35)' },
          { offset: 1, color: 'rgba(64,158,255,0.02)' }
        ])
      },
      lineStyle: { color: '#409eff', width: 3 },
      itemStyle: { color: '#409eff' },
      symbol: 'circle',
      symbolSize: 6
    }]
  })
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})

watch(() => stats.dailyTrend, () => {
  nextTick(() => renderChart())
})
</script>

<style scoped lang="scss">
.stats-dashboard {
  .stats-cards {
    margin-bottom: 20px;
  }
  .stat-card {
    .stat-inner {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 8px 0;
    }
    .stat-icon {
      width: 56px; height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      &.users-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
      &.links-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }
      &.clicks-icon { background: linear-gradient(135deg, #11998e, #38ef7d); }
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
  .chart-card {
    margin-bottom: 0;
  }
  .card-title {
    font-weight: 600;
    font-size: 15px;
  }
}
</style>
