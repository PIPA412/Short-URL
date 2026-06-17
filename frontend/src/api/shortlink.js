import request from './request'

export function createShortLink(data) {
  return request.post('/short-link', data)
}

export function getShortLinkList(params) {
  return request.get('/short-link', { params })
}

export function getShortLinkDetail(id) {
  return request.get(`/short-link/${id}`)
}

export function updateShortLink(id, data) {
  return request.put(`/short-link/${id}`, data)
}

export function deleteShortLink(id) {
  return request.delete(`/short-link/${id}`)
}

export function getAccessLogs(params) {
  return request.get('/access-log', { params })
}
