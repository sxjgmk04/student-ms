import {
  ArrowDown,
  ArrowRight,
  Bell,
  ChatLineRound,
  CircleCheck,
  DataLine,
  Document,
  Expand,
  Fold,
  HomeFilled,
  InfoFilled,
  List,
  Lock,
  Medal,
  Notebook,
  Printer,
  Promotion,
  Reading,
  Refresh,
  School,
  Search,
  Setting,
  SwitchButton,
  Trophy,
  TrendCharts,
  User,
  UserFilled,
  Warning
} from '@element-plus/icons-vue'
import { ElLoading } from 'element-plus'
import 'element-plus/es/components/loading/style/css'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { useUserStore } from './stores/user'
import request from './utils/request'
import './styles/global.css'

const icons = {
  ArrowDown,
  ArrowRight,
  Bell,
  ChatLineRound,
  CircleCheck,
  DataLine,
  Document,
  Expand,
  Fold,
  HomeFilled,
  InfoFilled,
  List,
  Lock,
  Medal,
  Notebook,
  Printer,
  Promotion,
  Reading,
  Refresh,
  School,
  Search,
  Setting,
  SwitchButton,
  Trophy,
  TrendCharts,
  User,
  UserFilled,
  Warning
}

const app = createApp(App)
const pinia = createPinia()
const userStore = useUserStore(pinia)

for (const [key, component] of Object.entries(icons)) {
  app.component(key, component)
}

app.use(ElLoading)
app.use(pinia)
userStore.restoreFromStorage()
app.use(router)
app.mount('#app')

async function validateSession() {
  if (!userStore.userInfo || !userStore.token) return
  try {
    const me = await request.get('/auth/me')
    userStore.syncMe(me)
  } catch {
    userStore.logout()
    router.replace('/login')
  }
}

validateSession()

window.addEventListener('auth-invalid', () => {
  userStore.logout()
  router.replace('/login')
})
