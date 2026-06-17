import request from './request'

/** Get paginated short link list */
export function getShortLinkList(params) {
  return request({
    url: '/short-link/list',
    method: 'get',
    params
  })
}

/** Get short link detail by ID */
export function getShortLinkById(id) {
  return request({
    url: `/short-link/${id}`,
    method: 'get'
  })
}

/** Create a new short link */
export function createShortLink(data) {
  return request({
    url: '/short-link',
    method: 'post',
    data
  })
}

/** Update a short link */
export function updateShortLink(data) {
  return request({
    url: '/short-link',
    method: 'put',
    data
  })
}

/** Delete a short link */
export function deleteShortLink(id) {
  return request({
    url: `/short-link/${id}`,
    method: 'delete'
  })
}
