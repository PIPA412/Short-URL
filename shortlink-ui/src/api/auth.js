import request from './request'

/** User login */
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/** User registration */
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/** Get current user info */
export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}
