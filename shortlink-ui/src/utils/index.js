/**
 * Format a date string to a consistent display format.
 * @param {string|Date} date - date to format
 * @param {string} [fmt='yyyy-MM-dd HH:mm:ss'] - format pattern
 * @returns {string} formatted date string
 */
export function formatDate(date, fmt = 'yyyy-MM-dd HH:mm:ss') {
  if (!date) return ''
  const d = new Date(date)
  if (isNaN(d.getTime())) return date

  const o = {
    'M+': d.getMonth() + 1,
    'd+': d.getDate(),
    'H+': d.getHours(),
    'h+': d.getHours() % 12 || 12,
    'm+': d.getMinutes(),
    's+': d.getSeconds(),
    'S': d.getMilliseconds()
  }

  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, String(d.getFullYear()).slice(-RegExp.$1.length))
  }
  for (const [k, v] of Object.entries(o)) {
    if (new RegExp(`(${k})`).test(fmt)) {
      fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? v : String(v).padStart(2, '0'))
    }
  }
  return fmt
}

/**
 * Copy text to clipboard.
 * @param {string} text - text to copy
 * @returns {Promise<boolean>} true if successful
 */
export async function copyToClipboard(text) {
  try {
    await navigator.clipboard.writeText(text)
    return true
  } catch {
    // Fallback for older browsers
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    return true
  }
}

/**
 * Debounce a function.
 * @param {Function} fn - function to debounce
 * @param {number} delay - delay in ms
 * @returns {Function} debounced function
 */
export function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

/**
 * Validate a URL format.
 * @param {string} url - URL to validate
 * @returns {boolean} true if valid
 */
export function isValidUrl(url) {
  try {
    new URL(url)
    return true
  } catch {
    return false
  }
}
