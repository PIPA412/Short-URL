import request from '@/utils/request'

// 查询参数列表
export function listConfig(query) {
  return request({ url: '/admin/config/list', method: 'get', params: query })
}

// 查询参数详细
export function getConfig(configId) {
  return request({ url: '/admin/config/' + configId, method: 'get' })
}

// 根据键名查询配置
export function getConfigKey(configKey) {
  return request({ url: '/admin/config/configKey/' + configKey, method: 'get' })
}

// 新增参数
export function addConfig(data) {
  return request({ url: '/admin/config', method: 'post', data: data })
}

// 修改参数
export function updateConfig(data) {
  return request({ url: '/admin/config', method: 'put', data: data })
}

// 删除参数
export function delConfig(configIds) {
  return request({ url: '/admin/config/' + configIds, method: 'delete' })
}
