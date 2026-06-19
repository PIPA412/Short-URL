/**
 * ZSC 通用工具函数
 */

/**
 * 解析时间，格式化后台返回的日期时间
 * @param {Object} time 时间对象或者字符串
 * @param {String} pattern 格式，默认：YYYY-MM-DD HH:mm:ss
 * @returns {String|null}
 */
export function parseTime(time, pattern) {
  if (arguments.length === 0 || !time) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
      time = parseInt(time)
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const timeStr = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return timeStr
}

/**
 * 表单重置方法
 * @param {Object} refName 引用名称
 */
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields()
  }
}

/**
 * 添加日期范围
 * @param {Array} data 数据
 * @param {String} prop 属性名
 * @param {Array} dateRange 日期范围
 */
export function addDateRange(data, prop, dateRange) {
  const params = { ...data }
  if (dateRange && dateRange.length === 2) {
    params[prop + 'Begin'] = dateRange[0]
    params[prop + 'End'] = dateRange[1]
  }
  return params
}

/**
 * 回显数据字典
 * @param {Array} datas 数据列表
 * @param {String} value 值
 * @returns {String}
 */
export function selectDictLabel(datas, value) {
  if (value === undefined || value === null || value === '') {
    return ''
  }
  const actions = []
  Object.keys(datas).some((key) => {
    if (datas[key].value === ('' + value)) {
      actions.push(datas[key].label)
      return true
    }
  })
  return actions.join('')
}

/**
 * 回显数据字典（多个）
 * @param {Array} datas 数据列表
 * @param {String} values 值（逗号分隔）
 * @returns {String}
 */
export function selectDictLabels(datas, values) {
  if (values === undefined || values === null || values === '') {
    return ''
  }
  const arrs = []
  const valuesArr = values.split(',')
  valuesArr.forEach(val => {
    Object.keys(datas).some(key => {
      if (datas[key].value === ('' + val)) {
        arrs.push(datas[key].label)
        return true
      }
    })
  })
  return arrs.join('')
}

/**
 * 构造树型结构数据
 * @param {Array} data 数据源
 * @param {String} id id field
 * @param {String} pid parentId field
 * @param {String} children children field
 * @returns {Array}
 */
export function handleTree(data, id, pid, children) {
  const config = {
    id: id || 'id',
    parentId: pid || 'parentId',
    childrenList: children || 'children'
  }
  const childrenListMap = {}
  const nodeIds = {}
  const tree = []

  for (const d of data) {
    const parentId = d[config.parentId]
    if (childrenListMap[parentId] == null) {
      childrenListMap[parentId] = []
    }
    nodeIds[d[config.id]] = d
    childrenListMap[parentId].push(d)
  }

  for (const d of data) {
    const parentId = d[config.parentId]
    if (nodeIds[parentId] == null) {
      tree.push(d)
    }
  }

  for (const t of tree) {
    adaptToChildrenList(t)
  }

  function adaptToChildrenList(t) {
    if (childrenListMap[t[config.id]] !== null) {
      t[config.childrenList] = childrenListMap[t[config.id]]
    }
    if (t[config.childrenList]) {
      for (const c of t[config.childrenList]) {
        adaptToChildrenList(c)
      }
    }
  }
  return tree
}

/**
 * 字符串空值处理
 * @param {String} str 字符串
 * @returns {String}
 */
export function parseStrEmpty(str) {
  if (!str || str === 'null' || str === 'undefined') {
    return ''
  }
  return str
}

/**
 * 获取正常路径 (移除多余斜杠，确保路径格式正确)
 * @param {String} path 路径字符串
 * @returns {String} 处理后的路径
 */
export function getNormalPath(path) {
  if (!path || typeof path !== 'string') {
    return ''
  }
  // 替换多个连续斜杠为单个斜杠
  let result = path.replace(/\/+/g, '/')
  // 如果路径以 / 开头但不是 http(s) 开头，保留开头的 /
  if (result.startsWith('/') && !isExternal(result)) {
    return result
  }
  return result
}

/**
 * 判断是否为外部链接
 * @param {String} path 路径
 * @returns {Boolean}
 */
function isExternal(path) {
  return /^(https?:|mailto:|tel:)/.test(path)
}

/**
 * 参数处理
 * @param {Object} params 参数
 * @returns {String}
 */
export function tansParams(params) {
  let result = ''
  Object.keys(params).forEach(key => {
    if (params[key] !== void 0 && params[key] !== null && params[key] !== '') {
      result += '&' + key + '=' + encodeURIComponent(params[key])
    }
  })
  return result.substring(1)
}

/**
 * blob 验证
 * @param {Blob} data blob 数据
 * @returns {Boolean}
 */
export function blobValidate(data) {
  return data.type !== 'application/json'
}
