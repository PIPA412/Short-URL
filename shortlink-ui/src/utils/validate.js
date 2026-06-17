/**
 * Form validation rules.
 */

/** URL validation rule */
export const validateUrl = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  try {
    new URL(value)
    callback()
  } catch {
    callback(new Error('请输入有效的URL地址'))
  }
}

/** Password strength: at least 6 characters */
export const validatePassword = (rule, value, callback) => {
  if (value && value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    callback()
  }
}

/** Username: 3-50 alphanumeric + underscores */
export const validateUsername = (rule, value, callback) => {
  if (!value) {
    callback()
    return
  }
  if (value.length < 3 || value.length > 50) {
    callback(new Error('用户名长度为3-50个字符'))
    return
  }
  if (!/^[a-zA-Z0-9_]+$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字和下划线'))
    return
  }
  callback()
}
