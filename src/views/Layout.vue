<template>
  <div class="app-wrapper">
    <div
      v-if="isMobile && isMobileSidebarOpen"
      class="sidebar-mask"
      aria-hidden="true"
      @click="closeMobileSidebar"
    ></div>

    <aside
      class="sidebar-container"
      :class="{ collapse: isCollapsed, 'mobile-open': isMobileSidebarOpen }"
      aria-label="主导航"
    >
      <div class="sidebar-head">
        <div class="brand-glow glow-1"></div>
        <div class="brand-glow glow-2"></div>
        <div class="sidebar-logo">
          <div class="logo-icon-wrap">
            <el-icon :size="isCollapsed ? 24 : 18" color="#fff">
              <School />
            </el-icon>
          </div>
          <div v-show="!isCollapsed" class="logo-text-group">
            <span class="logo-text">学籍管理系统</span>
            <span class="logo-subtext">Academic Console</span>
          </div>
        </div>
      </div>

      <div class="sidebar-nav-wrap">
        <el-menu
          :default-active="activeMenu"
          router
          class="sidebar-menu"
          :collapse="isCollapsed"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#64748b"
          active-text-color="#0f172a"
          @select="handleMenuSelect"
        >
          <el-menu-item index="/welcome">
            <el-icon>
              <HomeFilled />
            </el-icon>
            <template #title>欢迎页</template>
          </el-menu-item>

          <el-menu-item index="/dashboard">
            <el-icon>
              <DataLine />
            </el-icon>
            <template #title>{{ userStore.isAdmin ? '数据统计' : '数据概览' }}</template>
          </el-menu-item>

          <el-menu-item index="/insights" v-if="userStore.isAdmin && userStore.userInfo">
            <el-icon>
              <TrendCharts />
            </el-icon>
            <template #title>学情智能分析</template>
          </el-menu-item>
          <el-menu-item index="/risk-interventions" v-if="userStore.isAdmin && userStore.userInfo">
            <el-icon>
              <List />
            </el-icon>
            <template #title>干预记录</template>
          </el-menu-item>

          <el-menu-item index="/students" v-if="userStore.isAdmin && userStore.userInfo">
            <el-icon>
              <User />
            </el-icon>
            <template #title>学生信息</template>
          </el-menu-item>
          <el-menu-item index="/changes" v-if="userStore.userInfo">
            <el-icon>
              <Refresh />
            </el-icon>
            <template #title>{{ userStore.isAdmin ? '学籍异动' : '我的学籍异动' }}</template>
          </el-menu-item>
          <el-menu-item index="/graduation" v-if="userStore.isAdmin && userStore.userInfo">
            <el-icon>
              <Medal />
            </el-icon>
            <template #title>毕业审核</template>
          </el-menu-item>
          <el-menu-item index="/audit" v-if="userStore.isAdmin && userStore.userInfo">
            <el-icon>
              <Notebook />
            </el-icon>
            <template #title>操作审计</template>
          </el-menu-item>
          <el-menu-item index="/scores">
            <el-icon>
              <Document />
            </el-icon>
            <template #title>{{ userStore.isAdmin ? '成绩管理' : '我的成绩' }}</template>
          </el-menu-item>
          <el-menu-item index="/transcript" v-if="userStore.isStudent && userStore.userInfo">
            <el-icon>
              <Printer />
            </el-icon>
            <template #title>成绩单打印</template>
          </el-menu-item>
          <el-menu-item index="/rewards">
            <el-icon>
              <Trophy />
            </el-icon>
            <template #title>{{ userStore.isAdmin ? '奖惩管理' : '我的奖惩' }}</template>
          </el-menu-item>

          <el-menu-item index="/notifications" v-if="userStore.userInfo">
            <el-icon>
              <Bell />
            </el-icon>
            <template #title>通知公告</template>
          </el-menu-item>
          <el-menu-item index="/policy-ai" v-if="userStore.userInfo">
            <el-icon>
              <ChatLineRound />
            </el-icon>
            <template #title>智能问答</template>
          </el-menu-item>
          <el-menu-item index="/academic" v-if="userStore.userInfo">
            <el-icon>
              <Reading />
            </el-icon>
            <template #title>{{ userStore.isAdmin ? '教学运行' : '教务一览' }}</template>
          </el-menu-item>
          <el-menu-item index="/profile" v-if="userStore.userInfo">
            <el-icon>
              <UserFilled />
            </el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </el-menu>
      </div>

      <div class="sidebar-foot" v-show="!isCollapsed">
        <div class="sidebar-meta">
          <el-tag class="mode-chip" size="small" :type="userStore.isAdmin ? 'primary' : 'success'">
            {{ userStore.isAdmin ? '管理员模式' : '学生模式' }}
          </el-tag>
          <span class="meta-date">{{ todayText }}</span>
        </div>
      </div>
    </aside>

    <div class="main-box">
      <header class="header-box">
        <div class="header-left">
          <button
            type="button"
            class="collapse-btn"
            :aria-label="sidebarToggleAriaLabel"
            :aria-expanded="sidebarToggleAriaExpanded"
            @click="toggleSidebar"
          >
            <el-icon :size="20" color="#1d2129" aria-hidden="true">
              <Fold v-if="!isCollapsed" />
              <Expand v-else />
            </el-icon>
          </button>
          <el-breadcrumb separator="/" class="breadcrumb" aria-label="面包屑导航">
            <el-breadcrumb-item :to="{ path: '/welcome' }">
              <span class="breadcrumb-home">
                <el-icon aria-hidden="true">
                  <HomeFilled />
                </el-icon>
                <span class="sr-only">欢迎页</span>
              </span>
            </el-breadcrumb-item>
            <el-breadcrumb-item
              v-for="(item, index) in breadcrumbTrail"
              :key="item.nameKey"
              :to="index < breadcrumbTrail.length - 1 ? item.to : undefined"
            >
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-popover
            placement="bottom-end"
            :width="320"
            trigger="click"
            popper-class="header-notify-popper"
          >
            <template #reference>
              <el-badge
                :value="todoSummary.pendingChanges || undefined"
                :hidden="notifyBadgeHidden"
                :is-dot="notifyDotOnly"
                class="notify-badge-wrap"
              >
                <button type="button" class="header-icon-btn" aria-label="通知与待办">
                  <el-icon :size="20" color="#1d2129" aria-hidden="true">
                    <Bell />
                  </el-icon>
                </button>
              </el-badge>
            </template>
            <div class="notify-popover" role="region" aria-label="通知与待办预览">
              <section class="notify-block">
                <div class="notify-popover-title">待办</div>
                <p v-if="todoSummary.pendingChanges > 0" class="notify-popover-line accent">
                  <template v-if="userStore.isAdmin"
                    >有 {{ todoSummary.pendingChanges }} 条学籍异动待审核。</template
                  >
                  <template v-else
                    >您有 {{ todoSummary.pendingChanges }} 条审核中的异动申请。</template
                  >
                </p>
                <p v-else class="notify-popover-muted">暂无待办。</p>
                <el-button type="primary" link class="notify-popover-link" @click="goChanges"
                  >前往学籍异动</el-button
                >
              </section>

              <el-divider class="notify-divider" />

              <section class="notify-block">
                <div class="notify-popover-title">最新公告</div>
                <ul v-if="announcementPreview.length" class="notify-announce-list">
                  <li v-for="a in announcementPreview" :key="a.id">
                    <span class="announce-dot"></span>
                    <span class="announce-text">{{ a.title }}</span>
                  </li>
                </ul>
                <p v-else class="notify-popover-muted">暂无公告。</p>
                <el-button type="primary" link class="notify-popover-link" @click="goNotifications"
                  >查看全部公告</el-button
                >
              </section>
            </div>
          </el-popover>

          <el-dropdown :aria-label="userMenuAriaLabel" popper-class="header-user-menu-popper">
            <div class="user-item">
              <el-avatar :size="34" :src="headerAvatarSrc" />
              <div v-if="!isCollapsed && userStore.userInfo" class="user-info">
                <span class="user-name">{{ userStore.userInfo.name }}</span>
              </div>
              <el-icon class="arrow">
                <ArrowDown />
              </el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="header-user-menu">
                <el-dropdown-item @click="$router.push('/profile')">
                  <el-icon>
                    <UserFilled /> </el-icon
                  >个人中心
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/notifications')">
                  <el-icon>
                    <Bell /> </el-icon
                  >通知公告
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/welcome')">
                  <el-icon>
                    <HomeFilled /> </el-icon
                  >返回欢迎页
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon>
                    <SwitchButton /> </el-icon
                  >退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content-box">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { resolveAvatarUrl } from '@/utils/avatarUrl.js'
import { resolveRouteTitle } from '@/utils/routeMeta.js'
import {
  ArrowDown,
  Bell,
  ChatLineRound,
  DataLine,
  Document,
  Expand,
  Fold,
  HomeFilled,
  List,
  Medal,
  Notebook,
  Printer,
  Reading,
  Refresh,
  School,
  SwitchButton,
  Trophy,
  TrendCharts,
  User,
  UserFilled
} from '@element-plus/icons-vue'
import { fetchAnnouncements, fetchTodoSummary, logoutApi } from '@/api/studentMs.js'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const isCollapsed = ref(false)
const isMobile = ref(window.matchMedia('(max-width: 992px)').matches)
const isMobileSidebarOpen = ref(false)
let viewportMql = null
const activeMenu = computed(() => route.path)

const breadcrumbTrail = computed(() => {
  const records = route.matched.filter((r) => r.name && (r.meta?.title || r.meta?.titles))
  return records.map((r, i) => ({
    nameKey: String(r.name),
    title: resolveRouteTitle(r.meta, { isAdmin: userStore.isAdmin }),
    to: i < records.length - 1 ? { name: r.name } : undefined
  }))
})

const sidebarToggleAriaLabel = computed(() => {
  if (isMobile.value) {
    return isMobileSidebarOpen.value ? '关闭导航菜单' : '打开导航菜单'
  }
  return isCollapsed.value ? '展开侧栏' : '收起侧栏'
})

const sidebarToggleAriaExpanded = computed(() => {
  if (isMobile.value) return isMobileSidebarOpen.value
  return !isCollapsed.value
})

const userMenuAriaLabel = computed(() => {
  const name = userStore.userInfo?.name
  return name ? `用户菜单，${name}` : '用户菜单'
})

const DEFAULT_AVATAR_ADMIN = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const DEFAULT_AVATAR_STUDENT = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const headerAvatarSrc = computed(() => {
  const url = userStore.userInfo?.avatarUrl?.trim()
  if (url) return resolveAvatarUrl(url)
  return userStore.isAdmin ? DEFAULT_AVATAR_ADMIN : DEFAULT_AVATAR_STUDENT
})

const todoSummary = ref({ pendingChanges: 0 })
const announcementPreview = ref([])

const notifyBadgeHidden = computed(
  () => !todoSummary.value.pendingChanges && announcementPreview.value.length === 0
)

const notifyDotOnly = computed(
  () => !todoSummary.value.pendingChanges && announcementPreview.value.length > 0
)

const loadHeaderNotify = async () => {
  try {
    const [todos, ann] = await Promise.all([fetchTodoSummary(), fetchAnnouncements()])
    todoSummary.value = todos || { pendingChanges: 0 }
    announcementPreview.value = Array.isArray(ann) ? ann.slice(0, 4) : []
  } catch {
    /* 顶栏静默失败，避免打断操作 */
  }
}

const goChanges = () => {
  router.push('/changes')
}

const goNotifications = () => {
  router.push('/notifications')
}

const todayText = computed(() =>
  new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
)

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '退出登录不会丢失任何数据，你仍可以重新登录此账号继续使用。',
      '确定退出登录？',
      {
        confirmButtonText: '退出登录',
        cancelButtonText: '取消',
        type: 'warning',
        showClose: true,
        customClass: 'logout-confirm-box',
        confirmButtonClass: 'logout-danger-btn',
        cancelButtonClass: 'logout-cancel-btn'
      }
    )

    try {
      await logoutApi()
    } catch {}

    userStore.logout()
    ElMessage.success('退出成功')
    router.replace({ path: '/login' })
  } catch {}
}

const toggleSidebar = () => {
  if (isMobile.value) {
    isMobileSidebarOpen.value = !isMobileSidebarOpen.value
  } else {
    isCollapsed.value = !isCollapsed.value
  }
}

const closeMobileSidebar = () => {
  isMobileSidebarOpen.value = false
}

const handleMenuSelect = () => {
  if (isMobile.value) {
    closeMobileSidebar()
  }
}

const handleViewportChange = (e) => {
  isMobile.value = e.matches
  if (!e.matches) {
    isMobileSidebarOpen.value = false
  }
}

let notifyInterval = null
const onAppDataUpdated = () => {
  loadHeaderNotify()
}

onMounted(() => {
  viewportMql = window.matchMedia('(max-width: 992px)')
  if (viewportMql.addEventListener) {
    viewportMql.addEventListener('change', handleViewportChange)
  } else {
    viewportMql.addListener(handleViewportChange)
  }

  loadHeaderNotify()
  notifyInterval = window.setInterval(loadHeaderNotify, 90_000)
  window.addEventListener('app-data-updated', onAppDataUpdated)
})

onUnmounted(() => {
  if (notifyInterval) {
    window.clearInterval(notifyInterval)
    notifyInterval = null
  }
  window.removeEventListener('app-data-updated', onAppDataUpdated)

  if (!viewportMql) return
  if (viewportMql.removeEventListener) {
    viewportMql.removeEventListener('change', handleViewportChange)
  } else {
    viewportMql.removeListener(handleViewportChange)
  }
})
</script>

<style scoped>
.app-wrapper {
  display: flex;
  width: 100vw;
  height: 100vh;
  background: var(--bg-color, #f8fafc);
  overflow: hidden;
}

.sidebar-container {
  width: 248px;
  height: calc(100% - 20px);
  margin: 10px 0 0px 12px;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #fcfdff 100%);
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 30px rgba(15, 23, 42, 0.06);
  transition:
    width 0.26s ease,
    box-shadow 0.26s ease;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 30;
}

.sidebar-container.collapse {
  width: 84px;
}

.sidebar-head {
  position: relative;
  padding: 14px 12px 12px;
  border-bottom: 1px solid #f1f5f9;
  background: linear-gradient(160deg, #f8fbff 0%, #eef5ff 45%, #ffffff 100%);
  overflow: hidden;
}

.brand-glow {
  position: absolute;
  border-radius: 50%;
  pointer-events: none;
}

.glow-1 {
  width: 110px;
  height: 110px;
  right: -42px;
  top: -44px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.18) 0%, rgba(59, 130, 246, 0) 70%);
}

.glow-2 {
  width: 88px;
  height: 88px;
  left: -30px;
  bottom: -40px;
  background: radial-gradient(circle, rgba(148, 163, 184, 0.22) 0%, rgba(148, 163, 184, 0) 70%);
}

.sidebar-logo {
  height: 48px;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  padding: 0 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94) 0%, rgba(248, 250, 252, 0.94) 100%);
  border: 1px solid #dbe7fb;
  box-shadow: 0 6px 18px rgba(37, 99, 235, 0.08);
  position: relative;
  z-index: 1;
}

.sidebar-container.collapse .sidebar-head {
  padding: 14px 10px 12px;
}

.sidebar-container.collapse .sidebar-logo {
  height: auto;
  min-height: 0;
  justify-content: center;
  align-items: center;
  padding: 0;
  gap: 0;
  background: transparent;
  border: none;
  box-shadow: none;
  border-radius: 0;
}

.sidebar-container.collapse .logo-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 11px;
}

.logo-icon-wrap {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.22);
}

.logo-text-group {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.logo-text {
  color: #0f172a;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.03em;
  white-space: nowrap;
}

.logo-subtext {
  margin-top: 1px;
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.04em;
}

.sidebar-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.mode-chip {
  border-radius: 999px;
  border: none;
}

.meta-date {
  font-size: 11px;
  color: #64748b;
  letter-spacing: 0.04em;
}

.sidebar-nav-wrap {
  flex: 1;
  min-height: 0;
  padding: 10px 10px 12px;
}

.sidebar-menu {
  height: 100%;
  border: none;
  overflow-y: auto;
  --el-menu-hover-bg-color: transparent;
  --el-menu-hover-text-color: #334155;
  --el-menu-bg-color: transparent;
}

.menu-title {
  margin: 14px 8px 6px;
  padding-left: 2px;
  font-size: 11px;
  color: #94a3b8;
  letter-spacing: 0.8px;
  font-weight: 600;
}

.sidebar-menu :deep(.el-menu-item) {
  height: 42px;
  margin: 4px 0;
  border-radius: 10px;
  color: #334155;
  font-weight: 500;
  transition:
    background 0.18s ease,
    color 0.18s ease,
    box-shadow 0.18s ease;
  position: relative;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 16px;
  margin-right: 10px;
  color: #64748b;
  transition: color 0.18s ease;
}

.sidebar-menu :deep(.el-menu-item:hover:not(.is-active)) {
  background: #f8fafc;
  color: #0f172a;
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: #eff6ff;
  color: #0f172a;
  font-weight: 600;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.12);
}

.sidebar-menu :deep(.el-menu-item.is-active:hover) {
  background: #eaf2ff;
  color: #0f172a;
}

.sidebar-menu :deep(.el-menu-item.is-active)::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 10px;
  width: 3px;
  height: 22px;
  border-radius: 999px;
  background: #2563eb;
}

.sidebar-menu :deep(.el-menu-item:hover .el-icon),
.sidebar-menu :deep(.el-menu-item.is-active .el-icon) {
  color: #2563eb;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item) {
  width: 44px;
  height: 44px;
  margin: 6px auto;
  padding: 0;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item:hover:not(.is-active)) {
  background: #f8fafc;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item.is-active) {
  background: #eff6ff;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.14);
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item.is-active)::before {
  content: none;
}

.sidebar-menu.el-menu--collapse :deep(.el-menu-item .el-icon) {
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-menu.el-menu--collapse :deep(.el-tooltip__trigger) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-foot {
  padding: 10px 12px 12px;
  border-top: 1px solid #f1f5f9;
  background: #fff;
}

.main-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  padding: 10px 10px 10px 12px;
}

.header-box {
  height: 56px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background: transparent;
  outline: none;
  transition: background 0.15s ease;
  padding: 0;
  font: inherit;
  color: inherit;
}

.collapse-btn:hover {
  background: #f1f5f9;
}

.collapse-btn:focus,
.collapse-btn:focus-visible {
  outline: none;
}

.breadcrumb {
  color: #475569;
  font-weight: 500;
}

.breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #0f172a;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-icon-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  cursor: pointer;
  border: none;
  background: transparent;
  padding: 0;
  font: inherit;
  color: inherit;
  transition:
    background 0.18s ease,
    transform 0.18s ease;
}

.header-icon-btn:hover {
  background: #eef2f7;
  transform: translateY(-1px);
}

.header-icon-btn:focus,
.header-icon-btn:focus-visible {
  outline: none;
}

.notify-badge-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 3px;
  border-radius: 12px;
  border: 1px solid #e6ebf2;
  background: #fff;
}

.notify-badge-wrap :deep(.el-badge__content) {
  transform: translate(5px, -2px);
}

.notify-popover {
  padding: 12px 14px;
}

.notify-block {
  border-radius: 10px;
}

.notify-popover-title {
  font-size: 11px;
  font-weight: 700;
  color: #64748b;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 10px;
}

.notify-popover-line {
  margin: 0 0 10px;
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.notify-popover-line.accent {
  background: linear-gradient(180deg, #eff6ff 0%, #f8fbff 100%);
  border-color: #bfdbfe;
}

.notify-popover-muted {
  margin: 0 0 10px;
  font-size: 13px;
  color: #94a3b8;
}

.notify-popover .notify-popover-link.el-button.is-link {
  padding: 0;
  height: auto;
  font-weight: 600;
  font-size: 13px;
}

.notify-divider {
  margin: 12px 0 10px;
}

.notify-announce-list {
  margin: 0 0 10px;
  padding-left: 0;
  list-style: none;
  font-size: 13px;
  color: #334155;
  line-height: 1.6;
}

.notify-announce-list li {
  margin-bottom: 6px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.announce-dot {
  width: 6px;
  height: 6px;
  margin-top: 7px;
  border-radius: 999px;
  background: #3b82f6;
  flex: 0 0 6px;
}

.announce-text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 6px;
  border-radius: 12px;
  cursor: pointer;
  border: none;
  background: #fff;
  outline: none;
  transition:
    background 0.18s ease,
    border-color 0.18s ease;
}

.user-item:hover {
  background: #f8fbff;
}

.user-item:focus,
.user-item:focus-visible {
  outline: none;
}

.header-right :deep(.el-dropdown [role='button']:focus),
.header-right :deep(.el-dropdown [role='button']:focus-visible) {
  outline: none;
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  white-space: nowrap;
}

.arrow {
  font-size: 11px;
  color: #94a3b8;
  margin-left: 1px;
}

.header-user-menu {
  min-width: 180px;
}

.header-user-menu :deep(.el-dropdown-menu__item) {
  border-radius: 10px;
  height: 38px;
  font-size: 14px;
  color: #334155;
  gap: 8px;
  margin: 2px 0;
}

.header-user-menu :deep(.el-dropdown-menu__item:hover) {
  background: #eff6ff;
  color: #1d4ed8;
}

.header-user-menu :deep(.el-dropdown-menu__item.is-divided) {
  margin-top: 8px;
  border-top: 1px solid #e2e8f0;
  padding-top: 8px;
}

.header-user-menu :deep(.el-dropdown-menu__item .el-icon) {
  font-size: 15px;
}

.content-box {
  flex: 1;
  margin-top: 10px;
  padding: 20px 22px 24px;
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
}

.content-box::before {
  content: none;
}

.sidebar-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  z-index: 25;
}

.fade-enter-active,
.fade-leave-active {
  transition:
    opacity 0.22s ease,
    transform 0.22s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(4px);
}

@media (max-width: 992px) {
  .app-wrapper {
    display: flex;
  }

  html body #app .app-wrapper aside.sidebar-container {
    position: fixed;
    top: 0;
    left: 0;
    margin: 0;
    width: min(84vw, 320px);
    height: 100vh;
    border-radius: 0 14px 14px 0;
    transform: translateX(-100%);
    transition: transform 0.24s ease;
  }

  .sidebar-container.mobile-open {
    transform: translateX(0);
  }

  html body #app .app-wrapper aside.sidebar-container.collapse {
    width: min(84vw, 320px);
  }

  .sidebar-nav-wrap {
    padding: 8px;
  }

  .sidebar-menu {
    max-height: none;
  }

  .main-box {
    width: 100%;
    padding: 8px;
  }

  .header-box {
    padding: 0 14px;
    height: 54px;
  }

  .content-box {
    margin-top: 8px;
    padding: 12px;
  }

  .user-info,
  .arrow {
    display: none;
  }
}
</style>
