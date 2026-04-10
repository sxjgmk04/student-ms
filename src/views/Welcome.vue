<template>
  <div class="page-shell welcome-page">
    <template v-if="loading">
      <el-card class="welcome-banner" shadow="hover">
        <el-skeleton :rows="3" animated />
      </el-card>
      <el-row :gutter="20" class="overview-row">
        <el-col :span="6" v-for="n in 4" :key="n">
          <el-card class="overview-card" shadow="never">
            <el-skeleton :rows="2" animated />
          </el-card>
        </el-col>
      </el-row>
      <el-card class="feature-card" shadow="never">
        <el-skeleton :rows="4" animated />
      </el-card>
    </template>

    <template v-else>
      <el-card class="welcome-banner" shadow="hover">
        <div class="banner-decoration decoration-1"></div>
        <div class="banner-decoration decoration-2"></div>
        <div class="banner-content">
          <div class="banner-text">
            <h1 class="welcome-title">
              {{ greetingPrefix }}，{{ userStore.userInfo?.name || '用户' }}！
              <el-icon class="wave-icon">
                <Promotion />
              </el-icon>
            </h1>
            <p class="welcome-subtitle">
              {{
                userStore.isAdmin
                  ? '这是您的高校学籍管理工作台。除传统学籍与成绩管理外，系统提供学情智能分析（规则引擎预警、专业大数据聚合），契合大数据与智能化教务的发展趋势。'
                  : '这是您的个人学籍工作台，可查看成绩、奖惩与异动；学业数据仅本人可见。'
              }}
            </p>
            <div class="quick-actions">
              <el-button
                type="primary"
                size="large"
                @click="$router.push('/dashboard')"
                class="action-btn primary-btn"
              >
                <el-icon>
                  <DataLine />
                </el-icon>
                查看数据统计
              </el-button>
              <el-button
                size="large"
                v-if="userStore.isAdmin"
                @click="$router.push('/students')"
                class="action-btn"
              >
                <el-icon>
                  <User />
                </el-icon>
                管理学生信息
              </el-button>
              <el-button
                size="large"
                v-if="userStore.isStudent"
                @click="$router.push('/scores')"
                class="action-btn"
              >
                <el-icon>
                  <Document />
                </el-icon>
                查看我的成绩
              </el-button>
            </div>
          </div>
          <div class="banner-illustration">
            <div class="illustration-box">
              <el-icon :size="120" color="#2563eb">
                <School />
              </el-icon>
            </div>
          </div>
        </div>
      </el-card>

      <el-card
        v-if="userStore.isStudent && studentInsight"
        class="student-insight-card"
        shadow="hover"
        :body-style="{ padding: '16px 20px' }"
      >
        <template #header>
          <div class="insight-card-header">
            <div class="insight-card-title-wrap">
              <el-icon class="insight-card-icon">
                <TrendCharts />
              </el-icon>
              <span class="insight-card-title">个人学情摘要</span>
            </div>
            <el-tag :type="insightTagType" size="small">{{ studentInsight.levelTag }}</el-tag>
          </div>
        </template>
        <div class="insight-body">
          <div class="insight-left">
            <div class="insight-kicker">与你所在专业对比</div>
            <p class="insight-vs">{{ studentInsight.vsMajorHint }}</p>
            <div class="insight-trend">
              <span class="trend-label">学期趋势：</span>
              <el-tag :type="trendTagType" size="small">{{
                studentInsight.trendLabel || '样本不足'
              }}</el-tag>
            </div>
            <div v-if="studentInsight.modelRiskHint" class="insight-model-line">
              <span class="trend-label">模型评估：</span>
              <span class="insight-model-hint">{{ studentInsight.modelRiskHint }}</span>
            </div>
          </div>
          <div class="insight-right">
            <div class="insight-kicker">建议要点</div>
            <ul class="insight-tips">
              <li v-for="(t, i) in studentInsight.tips" :key="i">{{ t }}</li>
            </ul>
          </div>
        </div>
        <div v-if="studentInsight.semesterTrends?.length" class="trend-row">
          <div class="insight-kicker">学期均分趋势</div>
          <div id="semester-trend-chart" class="trend-chart"></div>
          <div class="trend-chip-list">
            <span
              v-for="item in studentInsight.semesterTrends"
              :key="item.semester"
              class="trend-chip"
            >
              {{ item.semester }}：{{ item.avgScore }} 分（挂科 {{ item.failCount }}）
            </span>
          </div>
        </div>
        <div class="action-row">
          <div class="insight-kicker">行动清单</div>
          <ul class="insight-tips">
            <li v-for="(a, i) in actionChecklist" :key="`a-${i}`">{{ a }}</li>
          </ul>
        </div>
      </el-card>

      <el-row :gutter="20" class="overview-row">
        <el-col :span="6" v-for="item in overviewCards" :key="item.label">
          <el-card class="overview-card" shadow="hover">
            <div class="overview-content">
              <div class="overview-icon" :style="{ background: item.gradient }">
                <el-icon :size="28">
                  <component :is="item.icon" />
                </el-icon>
              </div>
              <div class="overview-text">
                <div class="overview-number">{{ item.value }}</div>
                <div class="overview-label">{{ item.label }}</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="feature-card" shadow="hover">
        <el-row :gutter="20" style="row-gap: 20px">
          <el-col :span="6" v-for="feature in visibleFeatures" :key="feature.title">
            <div class="feature-item" @click="$router.push(feature.path)">
              <div class="feature-icon-wrapper" :style="{ background: feature.bg }">
                <div class="feature-icon" :style="{ color: feature.color }">
                  <el-icon :size="32">
                    <component :is="feature.icon" />
                  </el-icon>
                </div>
              </div>
              <div class="feature-text">
                <h4>{{ feature.title }}</h4>
                <p>{{ feature.desc }}</p>
              </div>
              <div class="feature-arrow">
                <el-icon>
                  <ArrowRight />
                </el-icon>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { fetchMyStudentInsight, fetchSummary } from '@/api/studentMs.js'
import { useUserStore } from '@/stores/user'
import echarts, { lineAreaGradient } from '@/utils/echartsCore.js'
import {
  ArrowRight,
  CircleCheck,
  DataLine,
  Document,
  Medal,
  Printer,
  Promotion,
  Refresh,
  School,
  Trophy,
  TrendCharts,
  User
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

const userStore = useUserStore()

const emptySummary = () => ({
  students: [],
  changes: [],
  scores: [],
  rewards: [],
  graduation: []
})

const summary = ref(emptySummary())
const loading = ref(true)
const studentInsight = ref(null)
let trendChart = null

async function loadSummary() {
  try {
    summary.value = await fetchSummary()
    if (userStore.isStudent) {
      try {
        studentInsight.value = await fetchMyStudentInsight()
      } catch {
        studentInsight.value = null
      }
    } else {
      studentInsight.value = null
    }
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '概览数据加载失败')
    summary.value = emptySummary()
    studentInsight.value = null
  } finally {
    loading.value = false
  }
}
async function renderTrendChart() {
  if (!userStore.isStudent || !studentInsight.value?.semesterTrends?.length) {
    trendChart?.dispose()
    trendChart = null
    return
  }
  await nextTick()
  const el = document.getElementById('semester-trend-chart')
  if (!el) return
  trendChart?.dispose()
  trendChart = echarts.init(el)
  const semesters = studentInsight.value.semesterTrends.map((x) => x.semester)
  const avgs = studentInsight.value.semesterTrends.map((x) => x.avgScore)
  trendChart.setOption({
    grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: semesters, axisLabel: { color: '#475569' } },
    yAxis: {
      type: 'value',
      min: 50,
      max: 100,
      axisLabel: { color: '#475569' },
      splitLine: { lineStyle: { color: '#e2e8f0' } }
    },
    series: [
      {
        type: 'line',
        data: avgs,
        smooth: true,
        lineStyle: { color: '#2563eb', width: 3 },
        itemStyle: { color: '#2563eb' },
        areaStyle: {
          color: lineAreaGradient([
            { offset: 0, color: 'rgba(37,99,235,0.25)' },
            { offset: 1, color: 'rgba(37,99,235,0.03)' }
          ])
        }
      }
    ]
  })
}

const insightTagType = computed(() => {
  const l = studentInsight.value?.levelTag
  if (l === '预警') return 'danger'
  if (l === '关注') return 'warning'
  if (l === '稳定') return 'success'
  return 'info'
})
const trendTagType = computed(() => {
  const tag = studentInsight.value?.trendLabel
  if (tag === '上升') return 'success'
  if (tag === '下降') return 'danger'
  if (tag === '平稳') return 'warning'
  return 'info'
})
const actionChecklist = computed(() => {
  const level = studentInsight.value?.levelTag
  const trend = studentInsight.value?.trendLabel
  if (level === '预警') {
    return [
      '优先处理挂科课程，形成补考/重修时间表并设置每周检查点。',
      '每周至少完成一次薄弱科目专项复盘，记录错因与改进点。',
      trend === '下降'
        ? '建议与辅导员沟通学习压力与时间分配问题。'
        : '建议持续跟踪近期测验成绩，验证改进效果。'
    ]
  }
  if (level === '关注') {
    return [
      '对照培养方案核验学分进度，避免后续集中补修。',
      '针对 1-2 门关键课程制定短周期提升目标。',
      trend === '下降'
        ? '本学期建议增加自习和答疑时段，优先稳住核心课程。'
        : '保持稳定学习节奏，减少成绩波动。'
    ]
  }
  return [
    '保持当前学习节奏，优先巩固核心基础课程。',
    '可适当拓展竞赛/实践项目，提升综合能力。',
    trend === '上升'
      ? '继续保持上升趋势，建议设定更高阶段性目标。'
      : '定期回顾学习计划，防止成绩回落。'
  ]
})

let onDataUpdated = null
onMounted(() => {
  loadSummary()
  onDataUpdated = () => loadSummary()
  window.addEventListener('app-data-updated', onDataUpdated)
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  if (onDataUpdated) window.removeEventListener('app-data-updated', onDataUpdated)
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
  trendChart = null
})
watch(
  () => studentInsight.value?.semesterTrends,
  () => {
    renderTrendChart()
  },
  { deep: true }
)
function onResize() {
  trendChart?.resize()
}

const overviewCards = computed(() => {
  const s = summary.value
  if (userStore.isAdmin) {
    return [
      {
        label: '学生总数',
        value: s.students.length,
        icon: User,
        gradient: 'linear-gradient(135deg, #2563eb 0%, #93c5fd 100%)'
      },
      {
        label: '在读人数',
        value: s.students.filter((x) => x.status === '在读').length,
        icon: School,
        gradient: 'linear-gradient(135deg, #16a34a 0%, #86efac 100%)'
      },
      {
        label: '待审核异动',
        value: s.changes.filter((c) => c.status === '审核中').length,
        icon: Refresh,
        gradient: 'linear-gradient(135deg, #d97706 0%, #fdba74 100%)'
      },
      {
        label: '待审核毕业',
        value: s.graduation.filter((g) => g.status === '待审核').length,
        icon: Medal,
        gradient: 'linear-gradient(135deg, #dc2626 0%, #fca5a5 100%)'
      }
    ]
  }
  const myScores = s.scores.filter((x) => x.studentId === userStore.currentStudentId)
  const avgScore =
    myScores.length > 0
      ? (myScores.reduce((sum, item) => sum + item.score, 0) / myScores.length).toFixed(1)
      : 0
  return [
    {
      label: '已修课程',
      value: myScores.length,
      icon: Document,
      gradient: 'linear-gradient(135deg, #2563eb 0%, #93c5fd 100%)'
    },
    {
      label: '平均成绩',
      value: avgScore,
      icon: Trophy,
      gradient: 'linear-gradient(135deg, #16a34a 0%, #86efac 100%)'
    },
    {
      label: '奖惩记录',
      value: s.rewards.filter((r) => r.studentId === userStore.currentStudentId).length,
      icon: Medal,
      gradient: 'linear-gradient(135deg, #d97706 0%, #fdba74 100%)'
    },
    {
      label: '最高分数',
      value: myScores.length > 0 ? Math.max(...myScores.map((x) => x.score)) : 0,
      icon: DataLine,
      gradient: 'linear-gradient(135deg, #4f46e5 0%, #a5b4fc 100%)'
    }
  ]
})

const featureDefs = [
  {
    title: '数据统计',
    descAdmin: '查看全校数据统计与可视化图表',
    descStudent: '查看个人数据概览与统计图表',
    icon: DataLine,
    path: '/dashboard',
    color: '#2563eb',
    bg: '#eff6ff',
    requireAdmin: false
  },
  {
    title: '学情智能分析',
    descAdmin: '学业风险预警、专业均分对比与趋势分析',
    icon: TrendCharts,
    path: '/insights',
    color: '#0f766e',
    bg: '#ecfeff',
    requireAdmin: true
  },
  {
    title: '干预记录',
    descAdmin: '查看学业风险干预时间轴与跟进进展',
    icon: CircleCheck,
    path: '/risk-interventions',
    color: '#2563eb',
    bg: '#eff6ff',
    requireAdmin: true
  },
  {
    title: '学生信息',
    descAdmin: '维护全校学生基本信息',
    icon: User,
    path: '/students',
    color: '#16a34a',
    bg: '#f0fdf4',
    requireAdmin: true
  },
  {
    title: '成绩管理',
    descAdmin: '录入与查询全校学生成绩',
    descStudent: '查询个人各科成绩',
    icon: Document,
    path: '/scores',
    color: '#b45309',
    bg: '#fffbeb',
    requireAdmin: false
  },
  {
    title: '成绩单打印',
    descStudent: '正式版式成绩单，支持打印或另存为 PDF',
    icon: Printer,
    path: '/transcript',
    color: '#0369a1',
    bg: '#e0f2fe',
    studentOnly: true
  },
  {
    title: '奖惩管理',
    descAdmin: '录入与查询学生奖惩记录',
    descStudent: '查看个人奖惩记录',
    icon: Trophy,
    path: '/rewards',
    color: '#4f46e5',
    bg: '#eef2ff',
    requireAdmin: false
  },
  {
    title: '学籍异动',
    descAdmin: '审核与处理转专业、休学等异动申请',
    icon: Refresh,
    path: '/changes',
    color: '#dc2626',
    bg: '#fef2f2',
    requireAdmin: true
  },
  {
    title: '毕业审核',
    descAdmin: '审核学生毕业资格与相关材料',
    icon: Medal,
    path: '/graduation',
    color: '#0f766e',
    bg: '#ecfeff',
    requireAdmin: true
  }
]

const visibleFeatures = computed(() => {
  const isAdmin = userStore.isAdmin
  const isStudent = userStore.isStudent
  return featureDefs
    .filter((f) => {
      if (f.studentOnly && !isStudent) return false
      if (f.requireAdmin && !isAdmin) return false
      return true
    })
    .map((f) => ({
      ...f,
      desc: isAdmin ? f.descAdmin : (f.descStudent ?? f.descAdmin)
    }))
})

const greetingPrefix = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})
</script>

<style scoped>
.welcome-page {
  width: 100%;
}

.student-insight-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: 1px solid #dbe7fb;
  background: linear-gradient(145deg, #ffffff 0%, #f8fbff 56%, #f1f6ff 100%);
  position: relative;
  overflow: hidden;
}
.student-insight-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 70% 60% at 15% 0%, rgba(37, 99, 235, 0.08), transparent 60%),
    radial-gradient(ellipse 50% 40% at 85% 100%, rgba(148, 163, 184, 0.12), transparent 55%);
  pointer-events: none;
}
.insight-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.insight-card-title-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.insight-card-icon {
  color: #2563eb;
  font-size: 16px;
  flex-shrink: 0;
}
.insight-card-title {
  font-weight: 700;
  font-size: 15px;
  color: #0f172a;
}
.insight-body {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  gap: 14px 18px;
}
.insight-left,
.insight-right,
.trend-row,
.action-row {
  border: 1px solid #e2e8f0;
  background: rgba(255, 255, 255, 0.88);
  border-radius: 10px;
  padding: 12px 13px;
}
.insight-kicker {
  font-size: 12px;
  font-weight: 600;
  color: #334155;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 8px;
}
.insight-vs {
  margin: 0;
  font-size: 14px;
  color: #0f172a;
  line-height: 1.8;
  font-weight: 500;
}
.insight-trend {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.insight-model-line {
  margin-top: 10px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}
.insight-model-hint {
  font-size: 13px;
  color: #0f172a;
  line-height: 1.65;
  flex: 1;
  min-width: 0;
}
.trend-label {
  font-size: 13px;
  color: #334155;
  font-weight: 600;
}
.insight-tips {
  margin: 0;
  padding-left: 18px;
  font-size: 13px;
  color: #334155;
  line-height: 1.9;
  display: grid;
  gap: 4px;
}
.insight-tips li {
  padding-left: 2px;
}
.trend-row {
  margin-top: 12px;
}
.trend-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.trend-chart {
  width: 100%;
  height: 240px;
  margin-bottom: 8px;
}
.trend-chip {
  font-size: 12px;
  color: #1e293b;
  background: #f8fbff;
  border: 1px solid #dbe7fb;
  border-radius: 999px;
  padding: 3px 10px;
  font-weight: 500;
}
.action-row {
  margin-top: 12px;
}
@media (max-width: 768px) {
  .insight-body {
    grid-template-columns: 1fr;
  }
}

.welcome-banner {
  margin-bottom: 20px;
  border-radius: 14px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 55%, #f1f5f9 100%);
  position: relative;
  overflow: hidden;
}

.banner-decoration {
  position: absolute;
  border-radius: 50%;
  background: rgba(37, 99, 235, 0.08);
}

.decoration-1 {
  width: 180px;
  height: 180px;
  top: -70px;
  right: -40px;
}

.decoration-2 {
  width: 120px;
  height: 120px;
  bottom: -40px;
  left: 28%;
}

.banner-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 22px;
  position: relative;
  z-index: 1;
}

.banner-text {
  flex: 1;
  color: #0f172a;
}

.welcome-title {
  font-size: 30px;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
}

.wave-icon {
  color: #2563eb;
  animation: wave 1.2s infinite;
}

@keyframes wave {
  0%,
  100% {
    transform: rotate(0deg);
  }

  25% {
    transform: rotate(20deg);
  }

  75% {
    transform: rotate(-20deg);
  }
}

.welcome-subtitle {
  font-size: 14px;
  margin: 0 0 20px 0;
  color: #334155;
  line-height: 1.65;
  max-width: 620px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-btn {
  height: 40px;
  padding: 0 18px;
  font-weight: 500;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.2s ease;
}

.primary-btn {
  background: #2563eb;
  color: #fff;
  border: none;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.25);
}

.primary-btn:hover {
  transform: none;
  background: #1d4ed8;
}

.banner-illustration {
  margin-left: 24px;
}

.illustration-box {
  width: 140px;
  height: 140px;
  background: #fff;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.06);
}

.overview-row {
  margin-bottom: 20px;
}

.overview-card {
  height: 156px;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
  cursor: pointer;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.overview-card:hover {
  transform: none;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

.overview-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.overview-icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 6px 12px rgba(15, 23, 42, 0.14);
  transition: all 0.3s ease;
}

.overview-card:hover .overview-icon {
  transform: scale(1.04);
}

.overview-text {
  text-align: center;
}

.overview-number {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  color: #0f172a;
  margin-bottom: 4px;
  transition: all 0.3s ease;
}

.overview-card:hover .overview-number {
  color: #2563eb;
}

.overview-label {
  font-size: 14px;
  color: #64748b;
  white-space: nowrap;
  font-weight: 500;
}

.feature-card {
  margin-bottom: 20px;
  border-radius: 14px;
}

.feature-header {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feature-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.feature-subtitle {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 400;
}

.feature-item {
  display: flex;
  align-items: center;
  padding: 18px;
  border-radius: 12px;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
  background: #fff;
  height: 100%;
  gap: 14px;
  border: 1px solid #e2e8f0;
  position: relative;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.feature-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 2px;
  height: 100%;
  background: #2563eb;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.feature-item:hover {
  background: #f8fafc;
  transform: none;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

.feature-item:hover::before {
  opacity: 1;
}

.feature-icon-wrapper {
  width: 54px;
  height: 54px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.3s ease;
}

.feature-item:hover .feature-icon-wrapper {
  transform: scale(1.02);
}

.feature-text {
  flex: 1;
}

.feature-text h4 {
  margin: 0 0 6px 0;
  font-size: 16px;
  color: #0f172a;
  font-weight: 600;
}

.feature-text p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.feature-arrow {
  color: #94a3b8;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.feature-item:hover .feature-arrow {
  color: #2563eb;
  transform: translateX(3px);
}

@media (max-width: 1200px) {
  .overview-row :deep(.el-col),
  .feature-card :deep(.el-col) {
    max-width: 50%;
    flex: 0 0 50%;
  }
}

@media (max-width: 768px) {
  .banner-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
  }

  .welcome-title {
    font-size: 24px;
  }

  .welcome-subtitle {
    font-size: 13px;
  }

  .banner-illustration {
    margin-left: 0;
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }

  .illustration-box {
    width: 110px;
    height: 110px;
    border-radius: 14px;
  }

  .overview-row :deep(.el-col),
  .feature-card :deep(.el-col) {
    max-width: 100%;
    flex: 0 0 100%;
  }
}
</style>
