import request from './request'

/** Get comprehensive stats for a link */
export function getStats(linkId) {
  return request({ url: `/stats/${linkId}`, method: 'get' })
}

/** Get chart-only data (daily clicks for N days) */
export function getChartData(linkId, days = 7) {
  return request({
    url: `/stats/${linkId}/chart`,
    method: 'get',
    params: { days }
  })
}

/** Get paginated access logs with optional time-range filter */
export function getAccessLogs(linkId, params) {
  return request({
    url: `/stats/${linkId}/logs`,
    method: 'get',
    params
  })
}
