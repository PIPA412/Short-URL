import router from './router'
import NProgress from 'nprogress'

/** Pages accessible without authentication */
const whiteList = ['/login', '/register', '/401', '/404']

/**
 * Route guard — runs before every navigation.
 *
 * Logic:
 * 1. No token → allow white-listed pages, redirect others to /login
 * 2. Has token but on /login → redirect to dashboard (already logged in)
 * 3. Has token and navigating to protected page → allow (user info loaded lazily)
 */
router.beforeEach(async (to, from, next) => {
  NProgress.start()

  // Read token directly from localStorage (Pinia persistence)
  const stored = localStorage.getItem('shortlink-user')
  let hasToken = false
  if (stored) {
    try {
      hasToken = !!JSON.parse(stored).token
    } catch {
      hasToken = false
    }
  }

  if (hasToken) {
    // Already logged in
    if (to.path === '/login') {
      // Trying to access login page — redirect to dashboard
      next({ path: '/', replace: true })
    } else {
      // Proceed to any page — user info is loaded on-demand by each page
      // If token is expired, the 401 interceptor in request.js will handle logout
      next()
    }
  } else {
    // Not logged in
    if (whiteList.includes(to.path)) {
      // Allow access to white-listed pages
      next()
    } else {
      // Redirect to login, preserving the intended destination
      next({
        path: '/login',
        query: { redirect: to.fullPath },
        replace: true
      })
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
