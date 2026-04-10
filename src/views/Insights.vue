<template>
  <div class="page-shell insights-page">
    <template v-if="loading">
      <el-skeleton :rows="8" animated />
    </template>
    <template v-else>
      <section class="insight-banner">
        <div class="banner-head">
          <div class="banner-title-wrap">
            <el-icon class="banner-icon">
              <InfoFilled />
            </el-icon>
            <h2 class="banner-title">学情智能分析（规则引擎 + 轻量预测模型）</h2>
          </div>
          <span class="banner-badge">可解释分析</span>
        </div>
        <p class="insight-summary-text">{{ data.summary }}</p>
        <p v-if="data.riskModelDescription" class="model-desc-text">
          {{ data.riskModelDescription }}
        </p>
        <p class="rule-text">
          当前规则：高风险（均分&lt;{{ data.highAvgThreshold }} 或挂科&gt;={{
            data.highRiskFailCount
          }}）， 中风险（均分&lt;{{ data.mediumAvgThreshold }} 或挂科&gt;={{
            data.mediumRiskFailCount
          }}）， 挂科线：{{ data.failScoreLine }} 分
        </p>
        <ul class="insight-highlights">
          <li v-for="(h, i) in data.highlights" :key="i">{{ h }}</li>
        </ul>
      </section>

      <el-row :gutter="16" class="stat-row">
        <el-col :xs="24" :sm="8" :md="4">
          <el-card shadow="hover" class="stat-mini">
            <div class="stat-mini-label">有成绩记录人数</div>
            <div class="stat-mini-num">{{ data.totalStudentsWithScores }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="4">
          <el-card shadow="hover" class="stat-mini warn">
            <div class="stat-mini-label">中风险关注</div>
            <div class="stat-mini-num">{{ data.riskMediumCount }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="4">
          <el-card shadow="hover" class="stat-mini danger">
            <div class="stat-mini-label">高风险关注</div>
            <div class="stat-mini-num">{{ data.riskHighCount }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="4">
          <el-card shadow="hover" class="stat-mini">
            <div class="stat-mini-label">待跟进</div>
            <div class="stat-mini-num">{{ followUpStats.pending }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="4">
          <el-card shadow="hover" class="stat-mini">
            <div class="stat-mini-label">已沟通</div>
            <div class="stat-mini-num">{{ followUpStats.communicated }}</div>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="8" :md="4">
          <el-card shadow="hover" class="stat-mini">
            <div class="stat-mini-label">已改善</div>
            <div class="stat-mini-num">{{ followUpStats.improved }}</div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="chart-card" shadow="hover">
        <template #header>
          <span class="card-title">风险治理趋势（近 20 次分析快照）</span>
        </template>
        <div v-if="trendSnapshotHistory.length" id="risk-trend-chart" class="chart-box trend-box" />
        <el-empty
          v-else
          description="暂无快照数据（需先成功加载学情；若浏览器禁用本地存储则无法累积趋势）"
        />
      </el-card>

      <el-card class="chart-card" shadow="hover">
        <template #header>
          <span class="card-title">各专业成绩均分对比（大数据聚合视角）</span>
        </template>
        <div v-if="data.majorStats?.length" id="major-avg-chart" class="chart-box" />
        <el-empty v-else description="暂无专业成绩样本（请确认库中学生有专业字段且已录入成绩）" />
      </el-card>

      <el-card shadow="hover" class="table-card">
        <template #header>
          <span class="card-title">学业风险预警名单（规则 + 逻辑回归式风险概率）</span>
        </template>
        <el-table
          v-if="data.riskStudents?.length"
          :data="data.riskStudents"
          border
          stripe
          size="small"
          class="full-table"
        >
          <el-table-column prop="studentId" label="学号" width="110" align="center" />
          <el-table-column prop="name" label="姓名" width="84" align="center" />
          <el-table-column prop="major" label="专业" min-width="116" show-overflow-tooltip />
          <el-table-column prop="avgScore" label="均分" width="74" align="center" />
          <el-table-column label="挂科/已修" width="96" align="center">
            <template #default="{ row }">{{ row.failCount }}/{{ row.courseCount }}</template>
          </el-table-column>
          <el-table-column label="模型风险 p" width="100" align="center">
            <template #default="{ row }">
              <span :title="row.modelExplanation">{{ formatModelP(row.modelRiskScore) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="模型档" width="76" align="center">
            <template #default="{ row }">
              <el-tag
                :type="
                  row.modelRiskLevel === '高'
                    ? 'danger'
                    : row.modelRiskLevel === '中'
                      ? 'warning'
                      : 'success'
                "
                size="small"
                >{{ row.modelRiskLevel }}</el-tag
              >
            </template>
          </el-table-column>
          <el-table-column prop="riskLevel" label="规则风险" width="88" align="center">
            <template #default="{ row }">
              <el-tag :type="row.riskLevel === '高' ? 'danger' : 'warning'" size="small">{{
                row.riskLevel
              }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="跟进状态" width="136" align="center">
            <template #default="{ row }">
              <el-select
                :model-value="row.followUpStatus || '待跟进'"
                size="small"
                style="width: 108px"
                :loading="updatingFollowUpStudentId === row.studentId"
                @change="(val) => updateFollowUp(row, val)"
              >
                <el-option label="待跟进" value="待跟进" />
                <el-option label="已沟通" value="已沟通" />
                <el-option label="已改善" value="已改善" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="模型说明" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="model-explain-cell">{{ row.modelExplanation }}</span>
            </template>
          </el-table-column>
          <el-table-column label="触发原因" min-width="126" show-overflow-tooltip>
            <template #default="{ row }">
              <span :title="`${row.triggerReason || ''}\n建议：${row.suggestion || ''}`">{{
                row.triggerReason
              }}</span>
            </template>
          </el-table-column>
          <el-table-column label="跟进备注" min-width="132">
            <template #default="{ row }">
              <div class="note-cell">
                <span class="note-text">{{ row.followUpNote || '暂无备注' }}</span>
                <el-button type="primary" link @click="editFollowUpNote(row)">编辑</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="当前无触发预警的学生（阈值内表现稳定）" />
      </el-card>
    </template>
  </div>
</template>

<script setup>
import { fetchAnalyticsInsights, patchRiskFollowUp } from '@/api/studentMs.js'
import { readJsonArrayFromLocalStorage, writeJsonToLocalStorage } from '@/utils/storage.js'
import echarts from '@/utils/echartsCore.js'
import { InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'

const TREND_STORAGE_KEY = 'analyticsRiskTrendHistory'

const loading = ref(true)
const chartTextColor = '#475569'
const chartAxisColor = '#cbd5e1'
const chartGridColor = '#e2e8f0'
const data = ref({
  summary: '',
  highlights: [],
  riskStudents: [],
  majorStats: [],
  totalStudentsWithScores: 0,
  riskHighCount: 0,
  riskMediumCount: 0,
  highAvgThreshold: 68,
  mediumAvgThreshold: 75,
  failScoreLine: 60,
  highRiskFailCount: 2,
  mediumRiskFailCount: 1,
  riskModelDescription: ''
})

/** 与本地存储同步，用于趋势图数据与 v-if 展示 */
const trendSnapshotHistory = ref(readJsonArrayFromLocalStorage(TREND_STORAGE_KEY, []))

let chart = null
let trendChart = null
function formatModelP(p) {
  if (p == null || Number.isNaN(Number(p))) return '—'
  return `${(Number(p) * 100).toFixed(1)}%`
}

const updatingFollowUpStudentId = ref('')
const followUpStats = computed(() => {
  const rows = data.value.riskStudents || []
  return {
    pending: rows.filter((r) => (r.followUpStatus || '待跟进') === '待跟进').length,
    communicated: rows.filter((r) => r.followUpStatus === '已沟通').length,
    improved: rows.filter((r) => r.followUpStatus === '已改善').length
  }
})

async function load() {
  loading.value = true
  try {
    data.value = await fetchAnalyticsInsights()
    saveTrendSnapshot()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '学情分析加载失败')
  } finally {
    loading.value = false
  }
  // 图表在 v-else 中，仅当 loading 为 false 后才挂载；必须在结束 loading 之后再初始化 ECharts
  await nextTick()
  await nextTick()
  renderChart()
  renderTrendChart()
  requestAnimationFrame(() => {
    chart?.resize()
    trendChart?.resize()
  })
}

function renderChart() {
  const el = document.getElementById('major-avg-chart')
  if (!el || !data.value.majorStats?.length) return
  chart?.dispose()
  chart = echarts.init(el)
  const majors = data.value.majorStats.map((m) => m.major)
  const avgs = data.value.majorStats.map((m) => m.avgScore ?? 0)
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      confine: true,
      backgroundColor: 'rgba(15, 23, 42, 0.88)',
      borderWidth: 0,
      textStyle: { color: '#fff' }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: majors.length > 8 ? '18%' : '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: majors,
      axisLine: { lineStyle: { color: chartAxisColor } },
      axisTick: { show: false },
      axisLabel: {
        rotate: majors.length > 6 ? 28 : 0,
        interval: 0,
        color: chartTextColor
      }
    },
    yAxis: {
      type: 'value',
      min: 50,
      max: 100,
      name: '均分',
      nameTextStyle: { color: chartTextColor },
      axisLine: { lineStyle: { color: chartAxisColor } },
      axisTick: { show: false },
      axisLabel: { color: chartTextColor },
      splitLine: { lineStyle: { color: chartGridColor } }
    },
    series: [
      {
        type: 'bar',
        data: avgs,
        itemStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: '#2563eb' },
              { offset: 1, color: '#93c5fd' }
            ]
          },
          borderRadius: [6, 6, 0, 0]
        },
        barMaxWidth: 36
      }
    ]
  })
}

function saveTrendSnapshot() {
  const history = readJsonArrayFromLocalStorage(TREND_STORAGE_KEY, [])
  const row = {
    ts: new Date().toISOString(),
    high: Number(data.value.riskHighCount || 0),
    medium: Number(data.value.riskMediumCount || 0),
    pending: Number(followUpStats.value.pending || 0),
    communicated: Number(followUpStats.value.communicated || 0),
    improved: Number(followUpStats.value.improved || 0)
  }
  const next = [...history, row].slice(-20)
  writeJsonToLocalStorage(TREND_STORAGE_KEY, next)
  trendSnapshotHistory.value = next
}

function renderTrendChart() {
  const history = trendSnapshotHistory.value || []
  const el = document.getElementById('risk-trend-chart')
  if (!el || !history.length) {
    trendChart?.dispose()
    trendChart = null
    return
  }
  trendChart?.dispose()
  trendChart = echarts.init(el)
  const x = history.map((h) => {
    const d = new Date(h.ts)
    if (Number.isNaN(d.getTime())) return '—'
    return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  })
  const labelRotate = x.length > 8 ? 28 : 0
  trendChart.setOption({
    tooltip: { trigger: 'axis', confine: true },
    legend: {
      data: ['高风险', '中风险', '待跟进', '已改善'],
      bottom: 10,
      left: 'center',
      itemGap: 18,
      icon: 'roundRect',
      itemWidth: 18,
      itemHeight: 8,
      textStyle: { color: chartTextColor, fontSize: 12 },
      padding: [10, 8, 6, 8]
    },
    grid: {
      left: 52,
      right: 28,
      top: 36,
      bottom: labelRotate ? 132 : 102,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: x,
      boundaryGap: false,
      axisLabel: {
        color: chartTextColor,
        fontSize: 11,
        interval: 0,
        rotate: labelRotate,
        margin: 16,
        hideOverlap: true
      },
      axisLine: { lineStyle: { color: chartAxisColor } },
      axisTick: { alignWithLabel: true }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: chartTextColor },
      splitLine: { lineStyle: { color: chartGridColor } }
    },
    series: [
      { name: '高风险', type: 'line', smooth: true, data: history.map((h) => h.high || 0) },
      { name: '中风险', type: 'line', smooth: true, data: history.map((h) => h.medium || 0) },
      { name: '待跟进', type: 'line', smooth: true, data: history.map((h) => h.pending || 0) },
      { name: '已改善', type: 'line', smooth: true, data: history.map((h) => h.improved || 0) }
    ]
  })
}

function onResize() {
  chart?.resize()
  trendChart?.resize()
}

async function updateFollowUp(row, status) {
  if (!row?.studentId || !status || row.followUpStatus === status) return
  updatingFollowUpStudentId.value = row.studentId
  try {
    const res = await patchRiskFollowUp(row.studentId, { status, note: row.followUpNote || '' })
    row.followUpStatus = res?.status || status
    row.followUpNote = res?.note || row.followUpNote || ''
    row.followUpUpdatedAt = res?.updatedAt || row.followUpUpdatedAt || ''
    ElMessage.success('跟进状态已更新')
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '跟进状态更新失败')
  } finally {
    updatingFollowUpStudentId.value = ''
  }
}

async function editFollowUpNote(row) {
  if (!row?.studentId) return
  try {
    const { value } = await ElMessageBox.prompt('请输入跟进备注（可为空）', '编辑跟进备注', {
      inputValue: row.followUpNote || '',
      inputPlaceholder: '例如：已电话沟通，已制定补考计划',
      confirmButtonText: '保存',
      cancelButtonText: '取消'
    })
    const res = await patchRiskFollowUp(row.studentId, {
      status: row.followUpStatus || '待跟进',
      note: value || ''
    })
    row.followUpStatus = res?.status || row.followUpStatus
    row.followUpNote = res?.note || ''
    row.followUpUpdatedAt = res?.updatedAt || row.followUpUpdatedAt || ''
    ElMessage.success('跟进备注已更新')
  } catch {}
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  chart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.insight-banner {
  margin-bottom: 18px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
  background: linear-gradient(145deg, #ffffff 0%, #f8fafc 55%, #f1f5f9 100%);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  padding: 14px 16px 12px;
}
.banner-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.banner-title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.banner-icon {
  color: #2563eb;
  font-size: 18px;
  flex-shrink: 0;
}
.banner-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.35;
}
.banner-badge {
  flex-shrink: 0;
  border-radius: 999px;
  padding: 3px 10px;
  font-size: 12px;
  font-weight: 600;
  color: #1d4ed8;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
}
.insight-summary-text {
  margin: 10px 0 0;
  line-height: 1.75;
  color: #334155;
  font-size: 14px;
}
.model-desc-text {
  margin: 10px 0 0;
  padding: 10px 12px;
  font-size: 13px;
  line-height: 1.65;
  color: #475569;
  background: #f8fafc;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
}
.model-explain-cell {
  font-size: 12px;
  color: #475569;
  line-height: 1.45;
}
.rule-text {
  margin: 8px 0 0;
  color: #334155;
  font-size: 13px;
  line-height: 1.6;
}
.insight-highlights {
  margin: 10px 0 0;
  padding-left: 20px;
  color: #475569;
  font-size: 13px;
  line-height: 1.8;
  display: grid;
  gap: 2px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-mini {
  text-align: center;
  border: 1px solid var(--border-color);
  background: #fff;
  border-radius: 12px;
}
.stat-mini.warn {
  background: linear-gradient(135deg, #fffbeb 0%, #fff 100%);
}
.stat-mini.danger {
  background: linear-gradient(135deg, #fef2f2 0%, #fff 100%);
}
.stat-mini-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.stat-mini-num {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-color-primary);
  margin-top: 6px;
}
.stat-mini.warn .stat-mini-num {
  color: var(--el-color-warning);
}
.stat-mini.danger .stat-mini-num {
  color: var(--el-color-danger);
}
.chart-card,
.table-card {
  margin-bottom: 16px;
}
.card-title {
  font-weight: 600;
  color: #0f172a;
}
.full-table {
  width: 100%;
}
.full-table :deep(.el-table__cell) {
  padding: 7px 4px;
}
.note-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}
.note-text {
  color: #475569;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chart-box {
  width: 100%;
  height: 360px;
}
.trend-box {
  height: 330px;
  padding: 4px 4px 8px;
  box-sizing: border-box;
}
.hint-card {
  background: #fff;
  border: 1px dashed var(--border-color);
}
.hint-text {
  margin: 0;
  font-size: 13px;
  line-height: 1.75;
  color: var(--el-text-color-secondary);
}
@media (max-width: 768px) {
  .insight-banner {
    padding: 12px 12px 10px;
  }
  .banner-head {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }
  .banner-title {
    font-size: 15px;
  }
}
</style>
