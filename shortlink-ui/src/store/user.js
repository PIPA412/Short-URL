import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userInfo: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    nickname: (state) => state.userInfo?.nickname || state.userInfo?.username || ''
  },

  actions: {
    /**
     * Login — call the backend, store token + user info.
     * The request interceptor shows ElMessage.error on failure,
     * so we only show success here and let the interceptor handle errors.
     */
    async login(credentials) {
      const res = await loginApi(credentials)
      // res = R<LoginResponse> → res.data = { token, user }
      const { token, user } = res.data
      this.token = token
      this.userInfo = user
      ElMessage.success(`欢迎回来，${user.nickname || user.username}！`)
      return true
    },

    /**
     * Register — create a new account.
     * On success, auto-redirect to login page.
     */
    async register(form) {
      await registerApi(form)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
      return true
    },

    /**
     * Fetch current user info from the backend.
     * Called by permission guard when token exists but userInfo is null.
     */
    async getUserInfo() {
      const res = await getUserInfoApi()
      // res = R<UserInfoResponse> → res.data = { id, username, nickname, email, ... }
      this.userInfo = res.data
    },

    /**
     * Logout — clear all auth state.
     * Does NOT call any backend logout endpoint (JWT is stateless).
     */
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('shortlink-user')
      localStorage.removeItem('shortlink-app')
    }
  },

  // Persist token to localStorage via pinia-plugin-persistedstate
  persist: {
    key: 'shortlink-user',
    storage: localStorage,
    pick: ['token']
  }
})
