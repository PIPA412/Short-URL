import request from '@/utils/request'

// 查询短链接列表
export function listShortLink(query) {
  return request({
    url: '/api/shortlink/query',
    method: 'post',
    data: query
  })
}

// 查询短链接详细
export function getShortLink(linkId) {
  return request({
    url: '/api/shortlink/' + linkId,
    method: 'get'
  })
}

// 新增短链接
export function addShortLink(data) {
  return request({
    url: '/api/shortlink',
    method: 'post',
    data: data
  })
}

// 修改短链接
export function updateShortLink(data) {
  return request({
    url: '/api/shortlink',
    method: 'put',
    data: data
  })
}

// 删除短链接
export function delShortLink(linkId) {
  return request({
    url: '/api/shortlink/' + linkId,
    method: 'delete'
  })
}

// 查询短链接每日点击统计
export function getClickStats(linkId) {
  return request({
    url: '/api/shortlink/click-stats/' + linkId,
    method: 'get'
  })
}
