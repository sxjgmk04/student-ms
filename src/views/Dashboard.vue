<template>
  <div class="page-shell dashboard">
    <template v-if="loading">
      <div class="stat-card-container">
        <div class="stat-card" v-for="n in 5" :key="n">
          <el-skeleton :rows="2" animated />
        </div>
      </div>
      <div class="chart-card-container">
        <div class="chart-card" v-for="n in 2" :key="'sk1-' + n">
          <el-skeleton :rows="4" animated />
        </div>
      </div>
      <div class="chart-card-container">
        <div class="chart-card" v-for="n in userStore.isAdmin ? 3 : 2" :key="'sk2-' + n">
          <el-skeleton :rows="4" animated />
        </div>
      </div>
    </template>

    <template v-else>
      <div class="stat-card-container">
        <div
          class="stat-card"
          v-for="item in statCards"
          :key="item.label"
          :style="{ background: item.cardBg }"
        >
          <div class="stat-icon" :style="{ background: item.gradient }">
            <el-icon :size="26">
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </div>

      <template v-if="userStore.isAdmin">
        <div class="chart-card-container">
          <div class="chart-card">
            <div class="chart-title">学生专业分布统计</div>
            <div v-if="adminHasMajorData" id="major-chart" class="chart-container"></div>
            <el-empty v-else :image-size="96" description="暂无专业分布数据" />
          </div>
          <div class="chart-card">
            <div class="chart-title">全校成绩分段统计</div>
            <div v-if="adminHasScoreData" id="score-chart" class="chart-container"></div>
            <el-empty v-else :image-size="96" description="暂无成绩分段数据" />
          </div>
        </div>

        <div class="chart-card-container">
          <div class="chart-card">
            <div class="chart-title">学生学籍状态分布</div>
            <div v-if="adminHasStatusData" id="status-chart" class="chart-container"></div>
            <el-empty v-else :image-size="96" description="暂无学籍状态数据" />
          </div>
          <div class="chart-card">
            <div class="chart-title">学籍异动类型分布</div>
            <div v-if="adminHasChangeData" id="change-chart" class="chart-container"></div>
            <el-empty v-else :image-size="96" description="暂无异动类型数据" />
          </div>
          <div class="chart-card">
            <div class="chart-title">毕业审核进度</div>
            <div v-if="adminHasGraduationData" id="graduation-chart" class="chart-container"></div>
            <el-empty v-else :image-size="96" description="暂无毕业审核数据" />
          </div>
        </div>
      </template>

      <template v-if="userStore.isStudent">
        <div class="chart-card-container">
          <div class="chart-card">
            <div class="chart-title">我的个人成绩趋势</div>
            <div v-if="studentHasScores" id="student-score-chart" class="chart-container"></div>
            <el-empty v-else :image-size="96" description="暂无成绩趋势数据" />
          </div>
          <div class="chart-card">
            <div class="chart-title">我的成绩等级分布</div>
            <div v-if="studentHasScores" id="student-rate-chart" class="chart-container"></div>
            <el-empty v-else :image-size="96" description="暂无成绩等级分布" />
          </div>
        </div>

        <div class="chart-card-container">
          <div class="chart-card">
            <div class="chart-title">我的奖惩记录</div>
            <el-table
              v-if="myRewards.length"
              :data="myRewards"
              border
              stripe
              style="width: 100%"
              class="data-table"
            >
              <el-table-column prop="type" label="类型" align="center" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.type === '奖励' ? 'success' : 'danger'" size="small">{{
                    row.type
                  }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="内容" align="center" />
              <el-table-column prop="date" label="日期" align="center" width="140" />
            </el-table>
            <el-empty v-else :image-size="90" description="暂无奖惩记录" />
          </div>
          <div class="chart-card">
            <div class="chart-title">我的异动申请记录</div>
            <el-table
              v-if="myChanges.length"
              :data="myChanges"
              border
              stripe
              style="width: 100%"
              class="data-table"
            >
              <el-table-column prop="type" label="异动类型" align="center" />
              <el-table-column prop="status" label="审核状态" align="center" width="120">
                <template #default="{ row }">
                  <el-tag
                    :type="
                      row.status === '已通过'
                        ? 'success'
                        : row.status === '审核中'
                          ? 'warning'
                          : 'danger'
                    "
                    size="small"
                    >{{ row.status }}</el-tag
                  >
                </template>
              </el-table-column>
              <el-table-column prop="date" label="申请日期" align="center" width="140" />
            </el-table>
            <el-empty v-else :image-size="90" description="暂无异动申请记录" />
          </div>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup>
import { fetchSummary } from '@/api/studentMs.js'
import { useUserStore } from '@/stores/user'
import echarts, { lineAreaGradient } from '@/utils/echartsCore.js'
import {
  CircleCheck,
  Document,
  Medal,
  Refresh,
  School,
  Trophy,
  User,
  Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'

const userStore = useUserStore()
const chartTextColor = '#475569'
const chartAxisColor = '#cbd5e1'
const chartGridColor = '#e2e8f0'
const chartPalette = ['#2563eb', '#16a34a', '#d97706', '#dc2626', '#4f46e5']
let chartInstanceList = []
let resizeTimer = null

const emptyBundle = () => ({
  students: [],
  changes: [],
  scores: [],
  rewards: [],
  graduation: []
})

const bundle = ref(emptyBundle())
const loading = ref(true)
let handleAppDataUpdated = null

async function loadBundle() {
  try {
    bundle.value = await fetchSummary()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '统计数据加载失败')
    bundle.value = emptyBundle()
  }
}

const statCards = computed(() => {
  const b = bundle.value
  if (userStore.isAdmin) {
    return [
      {
        label: '学生总数',
        value: b.students.length,
        icon: User,
        gradient: 'linear-gradient(135deg, #2563eb 0%, #93c5fd 100%)',
        cardBg: 'linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%)'
      },
      {
        label: '在读人数',
        value: b.students.filter((s) => s.status === '在读').length,
        icon: School,
        gradient: 'linear-gradient(135deg, #16a34a 0%, #86efac 100%)',
        cardBg: 'linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%)'
      },
      {
        label: '异动申请总数',
        value: b.changes.length,
        icon: Refresh,
        gradient: 'linear-gradient(135deg, #d97706 0%, #fdba74 100%)',
        cardBg: 'linear-gradient(135deg, #fffbeb 0%, #ffedd5 100%)'
      },
      {
        label: '奖惩记录总数',
        value: b.rewards.length,
        icon: Trophy,
        gradient: 'linear-gradient(135deg, #4f46e5 0%, #a5b4fc 100%)',
        cardBg: 'linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%)'
      },
      {
        label: '待审核毕业人数',
        value: b.graduation.filter((g) => g.status === '待审核').length,
        icon: Medal,
        gradient: 'linear-gradient(135deg, #dc2626 0%, #fca5a5 100%)',
        cardBg: 'linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%)'
      }
    ]
  }
  const myScores = b.scores.filter((s) => s.studentId === userStore.currentStudentId)
  const myRewardsList = b.rewards.filter((s) => s.studentId === userStore.currentStudentId)
  const avgScore =
    myScores.length > 0
      ? (myScores.reduce((sum, item) => sum + item.score, 0) / myScores.length).toFixed(1)
      : 0
  return [
    {
      label: '已修课程数',
      value: myScores.length,
      icon: Document,
      gradient: 'linear-gradient(135deg, #2563eb 0%, #93c5fd 100%)',
      cardBg: 'linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%)'
    },
    {
      label: '课程平均分',
      value: avgScore,
      icon: Trophy,
      gradient: 'linear-gradient(135deg, #16a34a 0%, #86efac 100%)',
      cardBg: 'linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%)'
    },
    {
      label: '奖惩记录数',
      value: myRewardsList.length,
      icon: Medal,
      gradient: 'linear-gradient(135deg, #d97706 0%, #fdba74 100%)',
      cardBg: 'linear-gradient(135deg, #fffbeb 0%, #ffedd5 100%)'
    },
    {
      label: '最高分数',
      value: myScores.length > 0 ? Math.max(...myScores.map((s) => s.score)) : 0,
      icon: CircleCheck,
      gradient: 'linear-gradient(135deg, #4f46e5 0%, #a5b4fc 100%)',
      cardBg: 'linear-gradient(135deg, #eef2ff 0%, #e0e7ff 100%)'
    },
    {
      label: '挂科科目数',
      value: myScores.filter((s) => s.score < 60).length,
      icon: Warning,
      gradient: 'linear-gradient(135deg, #dc2626 0%, #fca5a5 100%)',
      cardBg: 'linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%)'
    }
  ]
})

const myRewards = computed(() =>
  bundle.value.rewards.filter((s) => s.studentId === userStore.currentStudentId)
)
const myChanges = computed(() =>
  bundle.value.changes.filter((s) => s.studentId === userStore.currentStudentId)
)
const studentHasScores = computed(() =>
  bundle.value.scores.some((s) => s.studentId === userStore.currentStudentId)
)
const adminHasMajorData = computed(() => bundle.value.students.length > 0)
const adminHasScoreData = computed(() => bundle.value.scores.length > 0)
const adminHasStatusData = computed(() => bundle.value.students.length > 0)
const adminHasChangeData = computed(() => bundle.value.changes.length > 0)
const adminHasGraduationData = computed(() => bundle.value.graduation.length > 0)

const initAdminCharts = () => {
  const b = bundle.value
  const majorEl = document.getElementById('major-chart')
  if (majorEl) {
    const majorChart = echarts.init(majorEl)
    const majorMap = {}
    b.students.forEach((student) => {
      majorMap[student.major] = (majorMap[student.major] || 0) + 1
    })
    majorChart.setOption({
      toolbox: {
        feature: { saveAsImage: { type: 'png', name: '专业分布图表', title: '下载图片' } },
        right: 10
      },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'category',
        data: Object.keys(majorMap),
        axisLine: { lineStyle: { color: chartAxisColor } },
        axisLabel: { color: chartTextColor }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLine: { lineStyle: { color: chartAxisColor } },
        axisLabel: { color: chartTextColor },
        splitLine: { lineStyle: { color: chartGridColor } }
      },
      series: [
        {
          type: 'bar',
          data: Object.values(majorMap),
          itemStyle: { color: '#2563eb', borderRadius: [6, 6, 0, 0] },
          barWidth: '50%'
        }
      ]
    })
    chartInstanceList.push(majorChart)
  }

  const scoreEl = document.getElementById('score-chart')
  if (scoreEl) {
    const scoreChart = echarts.init(scoreEl)
    const sectionData = [
      { name: '不及格(<60)', count: b.scores.filter((s) => s.score < 60).length },
      { name: '及格(60-79)', count: b.scores.filter((s) => s.score >= 60 && s.score < 80).length },
      { name: '良好(80-89)', count: b.scores.filter((s) => s.score >= 80 && s.score < 90).length },
      { name: '优秀(90+)', count: b.scores.filter((s) => s.score >= 90).length }
    ]
    scoreChart.setOption({
      toolbox: {
        feature: { saveAsImage: { type: 'png', name: '成绩分布图表', title: '下载图片' } },
        right: 10
      },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'category',
        data: sectionData.map((item) => item.name),
        axisLine: { lineStyle: { color: chartAxisColor } },
        axisLabel: { color: chartTextColor }
      },
      yAxis: {
        type: 'value',
        minInterval: 1,
        axisLine: { lineStyle: { color: chartAxisColor } },
        axisLabel: { color: chartTextColor },
        splitLine: { lineStyle: { color: chartGridColor } }
      },
      series: [
        {
          type: 'bar',
          data: sectionData.map((item) => item.count),
          itemStyle: {
            color: (params) =>
              [chartPalette[3], chartPalette[2], chartPalette[0], chartPalette[1]][
                params.dataIndex
              ],
            borderRadius: [6, 6, 0, 0]
          },
          barWidth: '50%'
        }
      ]
    })
    chartInstanceList.push(scoreChart)
  }

  const statusEl = document.getElementById('status-chart')
  if (statusEl) {
    const statusChart = echarts.init(statusEl)
    statusChart.setOption({
      toolbox: {
        feature: { saveAsImage: { type: 'png', name: '学籍状态图表', title: '下载图片' } },
        right: 10
      },
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left', textStyle: { color: chartTextColor } },
      series: [
        {
          type: 'pie',
          radius: ['45%', '70%'],
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          data: [
            {
              name: '在读',
              value: b.students.filter((s) => s.status === '在读').length,
              itemStyle: { color: chartPalette[0] }
            },
            {
              name: '休学',
              value: b.students.filter((s) => s.status === '休学').length,
              itemStyle: { color: chartPalette[2] }
            },
            {
              name: '毕业',
              value: b.students.filter((s) => s.status === '毕业').length,
              itemStyle: { color: chartPalette[1] }
            }
          ]
        }
      ]
    })
    chartInstanceList.push(statusChart)
  }

  const changeEl = document.getElementById('change-chart')
  if (changeEl) {
    const changeChart = echarts.init(changeEl)
    const changeMap = {}
    b.changes.forEach((item) => {
      changeMap[item.type] = (changeMap[item.type] || 0) + 1
    })
    changeChart.setOption({
      toolbox: {
        feature: { saveAsImage: { type: 'png', name: '异动类型图表', title: '下载图片' } },
        right: 10
      },
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left', textStyle: { color: chartTextColor } },
      series: [
        {
          type: 'pie',
          radius: '65%',
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          data: Object.keys(changeMap).map((key, index) => ({
            name: key,
            value: changeMap[key],
            itemStyle: { color: chartPalette[index % 5] }
          }))
        }
      ]
    })
    chartInstanceList.push(changeChart)
  }

  const graduationEl = document.getElementById('graduation-chart')
  if (graduationEl) {
    const graduationChart = echarts.init(graduationEl)
    graduationChart.setOption({
      toolbox: {
        feature: { saveAsImage: { type: 'png', name: '毕业审核图表', title: '下载图片' } },
        right: 10
      },
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left', textStyle: { color: chartTextColor } },
      series: [
        {
          type: 'pie',
          radius: ['45%', '70%'],
          itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
          data: [
            {
              name: '待审核',
              value: b.graduation.filter((g) => g.status === '待审核').length,
              itemStyle: { color: chartPalette[2] }
            },
            {
              name: '已通过',
              value: b.graduation.filter((g) => g.status === '通过').length,
              itemStyle: { color: chartPalette[1] }
            },
            {
              name: '不通过',
              value: b.graduation.filter((g) => g.status === '不通过').length,
              itemStyle: { color: chartPalette[3] }
            }
          ]
        }
      ]
    })
    chartInstanceList.push(graduationChart)
  }
}

const initStudentCharts = () => {
  const myScores = bundle.value.scores.filter((s) => s.studentId === userStore.currentStudentId)
  if (myScores.length === 0) return
  const studentScoreEl = document.getElementById('student-score-chart')
  if (!studentScoreEl) return
  const scoreChart = echarts.init(studentScoreEl)
  scoreChart.setOption({
    toolbox: {
      feature: { saveAsImage: { type: 'png', name: '个人成绩图表', title: '下载图片' } },
      right: 10
    },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: myScores.map((item) => item.course),
      axisLine: { lineStyle: { color: chartAxisColor } },
      axisLabel: { color: chartTextColor }
    },
    yAxis: {
      type: 'value',
      min: 0,
      max: 100,
      axisLine: { lineStyle: { color: chartAxisColor } },
      axisLabel: { color: chartTextColor },
      splitLine: { lineStyle: { color: chartGridColor } }
    },
    series: [
      {
        type: 'line',
        data: myScores.map((item) => item.score),
        lineStyle: { color: '#2563eb', width: 3 },
        itemStyle: { color: '#2563eb' },
        smooth: true,
        areaStyle: {
          color: lineAreaGradient([
            { offset: 0, color: 'rgba(37, 99, 235, 0.28)' },
            { offset: 1, color: 'rgba(37, 99, 235, 0.04)' }
          ])
        },
        markLine: {
          data: [
            {
              yAxis: 60,
              name: '及格线',
              lineStyle: { color: chartPalette[3], type: 'dashed', width: 2 }
            }
          ]
        }
      }
    ]
  })
  chartInstanceList.push(scoreChart)

  const studentRateEl = document.getElementById('student-rate-chart')
  if (!studentRateEl) return
  const rateChart = echarts.init(studentRateEl)
  const sectionData = [
    {
      name: '不及格(<60)',
      value: myScores.filter((s) => s.score < 60).length,
      itemStyle: { color: chartPalette[3] }
    },
    {
      name: '及格(60-79)',
      value: myScores.filter((s) => s.score >= 60 && s.score < 80).length,
      itemStyle: { color: chartPalette[2] }
    },
    {
      name: '良好(80-89)',
      value: myScores.filter((s) => s.score >= 80 && s.score < 90).length,
      itemStyle: { color: chartPalette[0] }
    },
    {
      name: '优秀(90+)',
      value: myScores.filter((s) => s.score >= 90).length,
      itemStyle: { color: chartPalette[1] }
    }
  ].filter((item) => item.value > 0)
  rateChart.setOption({
    toolbox: {
      feature: { saveAsImage: { type: 'png', name: '成绩等级图表', title: '下载图片' } },
      right: 10
    },
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left', textStyle: { color: chartTextColor } },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        data: sectionData
      }
    ]
  })
  chartInstanceList.push(rateChart)
}

const destroyAllCharts = () => {
  chartInstanceList.forEach((chart) => {
    if (chart && !chart.isDisposed()) chart.dispose()
  })
  chartInstanceList = []
}

onMounted(async () => {
  await loadBundle()
  loading.value = false
  await nextTick()
  destroyAllCharts()
  if (userStore.isAdmin) initAdminCharts()
  else initStudentCharts()

  handleAppDataUpdated = async () => {
    await loadBundle()
    loading.value = false
    await nextTick()
    destroyAllCharts()
    if (userStore.isAdmin) initAdminCharts()
    else initStudentCharts()
  }

  window.addEventListener('app-data-updated', handleAppDataUpdated)
  window.addEventListener('resize', handleResize)
})

watch(
  () => userStore.userInfo?.role,
  async () => {
    await loadBundle()
    loading.value = false
    await nextTick()
    destroyAllCharts()
    if (userStore.isAdmin) initAdminCharts()
    else initStudentCharts()
  },
  { immediate: false }
)

const handleResize = () => {
  if (resizeTimer) return
  resizeTimer = window.setTimeout(() => {
    resizeTimer = null
    chartInstanceList.forEach((chart) => {
      if (chart && !chart.isDisposed()) chart.resize()
    })
  }, 150)
}

onUnmounted(() => {
  destroyAllCharts()
  window.removeEventListener('resize', handleResize)
  if (resizeTimer) {
    window.clearTimeout(resizeTimer)
    resizeTimer = null
  }

  if (handleAppDataUpdated) window.removeEventListener('app-data-updated', handleAppDataUpdated)
  handleAppDataUpdated = null
})
</script>

<style scoped>
.dashboard {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-card-container {
  width: 100%;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 200px;
  height: 108px;
  display: flex;
  align-items: center;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  position: relative;
  overflow: hidden;
}

.stat-card::after {
  content: none;
}

.stat-card:hover {
  transform: none;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 5px 12px rgba(15, 23, 42, 0.15);
  transition: all 0.3s ease;
  margin-right: 18px;
}

.stat-card:hover .stat-icon {
  transform: scale(1.04);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  line-height: 1.2;
  color: #0f172a;
  margin-bottom: 6px;
  transition: all 0.3s ease;
}

.stat-card:hover .stat-number {
  color: #2563eb;
}

.stat-label {
  color: #475569;
  font-size: 14px;
  font-weight: 500;
}

.chart-card-container {
  width: 100%;
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
}

.chart-card {
  flex: 1;
  min-width: 360px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  transition:
    border-color 0.2s ease,
    box-shadow 0.2s ease;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.chart-card:hover {
  transform: none;
  box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}

.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 16px;
  padding-left: 10px;
  border-left: 3px solid #2563eb;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.data-table {
  border-radius: 12px;
  overflow: hidden;
}

.data-table :deep(.el-table__header-wrapper) {
  border-radius: 12px 12px 0 0;
}

.data-table :deep(.el-table th.el-table__cell) {
  background: linear-gradient(180deg, #f9fbff 0%, #f3f8ff 100%);
  color: #334155;
  font-weight: 600;
}

.data-table :deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background-color: #fafafa;
}

@media (max-width: 1200px) {
  .chart-card {
    min-width: 48%;
  }
}

@media (max-width: 768px) {
  .dashboard {
    gap: 12px;
  }

  .stat-card-container,
  .chart-card-container {
    gap: 10px;
  }

  .stat-card {
    min-width: 100%;
    height: auto;
    padding: 16px;
  }

  .stat-number {
    font-size: 24px;
  }

  .chart-card {
    min-width: 100%;
    padding: 14px;
  }

  .chart-container {
    height: 240px;
  }
}
</style>
