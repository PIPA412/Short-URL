import request from '@/utils/request'

// 获取系统概览统计
export function getDashboard(params) {
  return request({ url: '/admin/stats/dashboard', method: 'get', params })
}
