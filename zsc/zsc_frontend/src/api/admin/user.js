import request from '@/utils/request'

// 查询用户列表
export function listUser(query) {
  return request({ url: '/admin/user/list', method: 'get', params: query })
}

// 查询用户详细
export function getUser(userId) {
  return request({ url: '/admin/user/' + userId, method: 'get' })
}

// 新增用户
export function addUser(data) {
  return request({ url: '/admin/user', method: 'post', data: data })
}

// 修改用户
export function updateUser(data) {
  return request({ url: '/admin/user', method: 'put', data: data })
}

// 删除用户
export function delUser(userIds) {
  return request({ url: '/admin/user/' + userIds, method: 'delete' })
}

// 重置密码
export function resetUserPwd(data) {
  return request({ url: '/admin/user/resetPwd', method: 'put', data: data })
}

// 修改用户状态
export function changeUserStatus(data) {
  return request({ url: '/admin/user/changeStatus', method: 'put', data: data })
}
