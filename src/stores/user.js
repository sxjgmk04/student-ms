import { defineStore } from 'pinia'
import request from '@/utils/request.js'
import { decodeStoredJson, encodeStoredJson } from '@/utils/storageCodec.js'

export function decodeUserInfo(encrypted) {
  return decodeStoredJson(encrypted)
}

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: null
  }),
  getters: {
    isAdmin: (s) => s.userInfo?.role === 'admin',
    isStudent: (s) => s.userInfo?.role === 'student',
    currentStudentId: (s) => s.userInfo?.studentId || ''
  },
  actions: {
    setSession(payload) {
      const { token, user } = payload
      this.token = token
      this.userInfo = {
        id: user.id,
        username: user.username,
        name: user.name,
        role: user.role,
        studentId: user.studentId,
        email: user.email ?? null,
        phone: user.phone ?? null,
        avatarUrl: user.avatarUrl ?? null
      }
      localStorage.setItem('token', encodeStoredJson(this.token))
      localStorage.setItem('userInfo', encodeStoredJson(this.userInfo))
    },

    async loginWithPassword(username, password) {
      const data = await request.post('/auth/login', { username, password })
      this.setSession(data)
    },

    syncMe(user) {
      if (!user) return
      this.userInfo = {
        id: user.id,
        username: user.username,
        name: user.name,
        role: user.role,
        studentId: user.studentId,
        email: user.email ?? null,
        phone: user.phone ?? null,
        avatarUrl: user.avatarUrl ?? null
      }
      localStorage.setItem('userInfo', encodeStoredJson(this.userInfo))
    },

    restoreFromStorage() {
      try {
        const encTok = localStorage.getItem('token')
        const encUser = localStorage.getItem('userInfo')
        if (!encTok || !encUser) return
        this.token = decodeStoredJson(encTok)
        this.userInfo = decodeStoredJson(encUser)
      } catch {
        this.logout()
      }
    },

    logout() {
      this.userInfo = null
      this.token = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
