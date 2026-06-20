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

// 获取短链接统计
export function getShortLinkStats(linkId) {
  return request({
    url: '/api/shortlink/stats/' + linkId,
    method: 'get'
  })
}
