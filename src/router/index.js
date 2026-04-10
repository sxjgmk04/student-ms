import { ElMessage } from 'element-plus'
import { createRouter, createWebHistory } from 'vue-router'
import { decodeUserInfo } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/welcome',
    children: [
      {
        path: 'welcome',
        name: 'Welcome',
        component: () => import('@/views/Welcome.vue'),
        meta: { title: '欢迎页' }
      },
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { titles: { admin: '数据统计', student: '数据概览' } }
      },
      {
        path: 'students',
        name: 'Students',
        component: () => import('@/views/Students.vue'),
        meta: { requiresAdmin: true, title: '学生信息管理' }
      },
      {
        path: 'changes',
        name: 'Changes',
        component: () => import('@/views/Changes.vue'),
        meta: { titles: { admin: '学籍异动管理', student: '我的学籍异动' } }
      },
      {
        path: 'scores',
        name: 'Scores',
        component: () => import('@/views/Scores.vue'),
        meta: { titles: { admin: '成绩管理', student: '我的成绩' } }
      },
      {
        path: 'transcript',
        name: 'Transcript',
        component: () => import('@/views/Transcript.vue'),
        meta: { requiresStudent: true, title: '成绩单打印' }
      },
      {
        path: 'rewards',
        name: 'Rewards',
        component: () => import('@/views/Rewards.vue'),
        meta: { titles: { admin: '奖惩管理', student: '我的奖惩' } }
      },
      {
        path: 'graduation',
        name: 'Graduation',
        component: () => import('@/views/Graduation.vue'),
        meta: { requiresAdmin: true, title: '毕业审核' }
      },
      {
        path: 'audit',
        name: 'Audit',
        component: () => import('@/views/AuditLog.vue'),
        meta: { requiresAdmin: true, title: '操作审计' }
      },
      {
        path: 'insights',
        name: 'Insights',
        component: () => import('@/views/Insights.vue'),
        meta: { requiresAdmin: true, title: '学情智能分析' }
      },
      {
        path: 'risk-interventions',
        name: 'RiskInterventions',
        component: () => import('@/views/RiskInterventions.vue'),
        meta: { requiresAdmin: true, title: '干预记录' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/Notifications.vue'),
        meta: { title: '通知公告' }
      },
      {
        path: 'academic',
        name: 'Academic',
        component: () => import('@/views/Academic.vue'),
        meta: { titles: { admin: '教学运行一览', student: '教务一览' } }
      },
      {
        path: 'policy-ai',
        name: 'PolicyAi',
        component: () => import('@/views/PolicyAi.vue'),
        meta: { title: '智能问答' }
      }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const whiteList = ['/login']

  const encryptedToken = localStorage.getItem('token')
  const encryptedUserInfo = localStorage.getItem('userInfo')

  let isLogin = false
  let isAdmin = false
  let userInfo = null

  if (encryptedToken && encryptedUserInfo) {
    try {
      userInfo = decodeUserInfo(encryptedUserInfo)
      isLogin = !!userInfo?.username
      isAdmin = userInfo?.role === 'admin'
    } catch {
      isLogin = false
      isAdmin = false
      userInfo = null
    }
  }

  if (whiteList.includes(to.path)) {
    if (isLogin && to.path === '/login') return '/welcome'
    return true
  }

  if (!isLogin) {
    ElMessage.warning('请先登录')
    return '/login'
  }

  const requiresAdmin = to.matched.some((record) => record.meta.requiresAdmin)
  if (requiresAdmin && !isAdmin) {
    ElMessage.error('你没有权限访问该页面')
    return '/welcome'
  }

  const requiresStudent = to.matched.some((record) => record.meta.requiresStudent)
  if (requiresStudent && userInfo?.role !== 'student') {
    ElMessage.warning('成绩单功能仅对学生账号开放')
    return '/welcome'
  }

  return true
})

export default router
