<template>
  <div class="app-container">
    <!-- 头部信息 -->
    <el-card class="header-card">
      <div class="header-info">
        <el-button icon="ArrowLeft" @click="goBack">返回列表</el-button>
        <div class="link-info" v-if="linkInfo.title">
          <span class="label">短链接：</span>
          <el-tag type="success">{{ linkInfo.shortCode }}</el-tag>
          <span class="label" style="margin-left: 16px">标题：</span>
          <strong>{{ linkInfo.title }}</strong>
          <span class="label" style="margin-left: 16px">总点击量：</span>
          <el-tag type="warning">{{ linkInfo.clicks || 0 }}</el-tag>
        </div>
      </div>
    </el-card>

    <!-- 图表区域 -->
    <el-card class="chart-card">
      <template #header>
        <div class="chart-header">
          <span>📊 每日点击量统计</span>
          <span class="date-range" v-if="statsData.length > 0">
            {{ statsData[0].click_date }} ~ {{ statsData[statsData.length - 1].click_date }}
          </span>
        </div>
      </template>
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>加载统计数据中...</p>
      </div>
      <div v-else-if="statsData.length === 0" class="empty-container">
        <el-empty description="暂无点击数据" />
      </div>
      <div v-else ref="chartRef" class="chart-container"></div>
    </el-card>

    <!-- 数据明细表格 -->
    <el-card class="table-card" v-if="statsData.length > 0">
      <template #header>
        <span>📋 数据明细</span>
      </template>
      <el-table :data="statsData" stripe border style="width: 100%">
        <el-table-column prop="click_date" label="日期" align="center" width="180" sortable />
        <el-table-column prop="click_count" label="点击次数" align="center" sortable>
          <template #default="scope">
            <el-tag>{{ scope.row.click_count }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup name="ShortLinkStats">
import { getShortLink, getClickStats } from '@/api/biz/shortLink'
import * as echarts from 'echarts'

const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

const chartRef = ref(null)
const loading = ref(true)
const linkInfo = ref({})
const statsData = ref([])
let chartInstance = null

/** 返回列表页 */
function goBack() {
  router.push('/biz/shortlink')
}

/** 获取数据 */
function fetchData() {
  const linkId = route.params.linkId
  if (!linkId) {
    proxy.$modal.msgError('链接ID不存在')
    goBack()
    return
  }

  loading.value = true

  // 并行获取短链接信息和点击统计数据
  Promise.all([
    getShortLink(linkId),
    getClickStats(linkId)
  ]).then(([linkRes, statsRes]) => {
    linkInfo.value = linkRes.data || {}
    statsData.value = (statsRes.data || []).map(item => ({
      click_date: item.click_date,
      click_count: Number(item.click_count) || 0
    }))
    loading.value = false

    // 等 DOM 更新后渲染图表
    nextTick(() => {
      renderChart()
    })
  }).catch(() => {
    loading.value = false
    proxy.$modal.msgError('获取统计数据失败')
  })
}

/** 渲染柱状图 */
function renderChart() {
  if (!chartRef.value || statsData.value.length === 0) return

  // 销毁旧实例
  if (chartInstance) {
    chartInstance.dispose()
  }

  chartInstance = echarts.init(chartRef.value)

  const dates = statsData.value.map(item => item.click_date)
  const counts = statsData.value.map(item => item.click_count)

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: (params) => {
        const p = params[0]
        return `<strong>${p.name}</strong><br/>
                <span style="display:inline-block;margin-right:5px;border-radius:50%;width:10px;height:10px;background-color:#409eff;"></span>
                点击次数：<strong>${p.value}</strong>`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 0,
        interval: dates.length > 30 ? Math.ceil(dates.length / 20) : 0,
        formatter: (value) => {
          // 只显示月-日
          const parts = value.split('-')
          return parts.length === 3 ? parts[1] + '-' + parts[2] : value
        }
      },
      name: '日期',
      nameLocation: 'middle',
      nameGap: 30
    },
    yAxis: {
      type: 'value',
      name: '点击次数',
      minInterval: 1,
      axisLabel: {
        formatter: (value) => {
          return value >= 1000 ? (value / 1000).toFixed(1) + 'k' : value
        }
      }
    },
    series: [
      {
        name: '点击次数',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#409eff' },
            { offset: 1, color: '#79bbff' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#337ecc' },
              { offset: 1, color: '#5ba0e6' }
            ])
          }
        },
        barMaxWidth: 50,
        label: {
          show: true,
          position: 'top',
          color: '#606266',
          formatter: (params) => {
            return params.value > 0 ? params.value : ''
          }
        },
        animationDelay: (idx) => idx * 50
      }
    ]
  })

  // 窗口大小变化时重绘
  window.addEventListener('resize', handleResize)
}

function handleResize() {
  if (chartInstance) {
    chartInstance.resize()
  }
}

onMounted(() => {
  fetchData()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
})
</script>

<style lang="scss" scoped>
.header-card {
  margin-bottom: 16px;

  .header-info {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;

    .link-info {
      display: flex;
      align-items: center;
      gap: 6px;
      flex-wrap: wrap;

      .label {
        color: #909399;
        font-size: 14px;
      }
    }
  }
}

.chart-card {
  margin-bottom: 16px;

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .date-range {
      font-size: 13px;
      color: #909399;
    }
  }

  .loading-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 350px;
    color: #909399;
    gap: 12px;
  }

  .empty-container {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 350px;
  }

  .chart-container {
    width: 100%;
    height: 400px;
  }
}

.table-card {
  .el-table {
    font-size: 14px;
  }
}
</style>
