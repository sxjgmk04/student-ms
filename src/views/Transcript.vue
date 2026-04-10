<template>
  <div class="transcript-page">
    <div class="toolbar no-print">
      <el-button type="primary" :icon="Printer" :loading="loading" @click="handlePrint"
        >打印 / 另存为 PDF</el-button
      >
      <el-button :icon="Refresh" @click="loadAll">刷新</el-button>
      <span class="toolbar-hint">在打印对话框中选择「另存为 PDF」即可导出电子版成绩单。</span>
    </div>

    <template v-if="loading">
      <el-skeleton :rows="10" animated />
    </template>
    <template v-else-if="errorMsg">
      <el-alert type="warning" :title="errorMsg" show-icon :closable="false" class="no-print" />
    </template>
    <template v-else>
      <div class="sheet" id="transcript-print-area">
        <header class="sheet-head">
          <h1 class="school-title">学生成绩证明（演示）</h1>
          <p class="sheet-sub">
            本页由学籍管理系统生成，仅供教学演示；正式证明以教务部门盖章文件为准。
          </p>
        </header>

        <section class="meta-grid">
          <div class="meta-item">
            <span class="k">姓名</span><span class="v">{{ profile?.name || '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="k">学号</span
            ><span class="v">{{ profile?.studentId || userStore.currentStudentId }}</span>
          </div>
          <div class="meta-item">
            <span class="k">性别</span><span class="v">{{ profile?.gender || '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="k">专业</span><span class="v">{{ profile?.major || '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="k">班级</span><span class="v">{{ profile?.class || '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="k">年级</span
            ><span class="v">{{ profile?.grade != null ? profile.grade : '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="k">学籍状态</span><span class="v">{{ profile?.status || '—' }}</span>
          </div>
          <div class="meta-item">
            <span class="k">出具日期</span><span class="v">{{ issuedDate }}</span>
          </div>
        </section>

        <table class="grade-table">
          <thead>
            <tr>
              <th class="col-idx">序号</th>
              <th class="col-course">课程名称</th>
              <th class="col-score">成绩</th>
              <th class="col-sem">学期</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, i) in rows" :key="i">
              <td class="center">{{ i + 1 }}</td>
              <td>{{ row.course }}</td>
              <td class="center score-cell">
                <span :class="Number(row.score) < 60 ? 'fail' : ''">{{ row.score }}</span>
              </td>
              <td class="center muted">{{ row.semester }}</td>
            </tr>
          </tbody>
        </table>

        <footer class="sheet-foot">
          <div class="stat-line">
            <span
              >记录门数：<strong>{{ rows.length }}</strong></span
            >
            <span
              >算术平均分：<strong>{{ avgText }}</strong></span
            >
            <span
              >不及格门数：<strong>{{ failCount }}</strong></span
            >
          </div>
          <p class="foot-note">
            算术平均分为已列课程分数的简单平均；同一课程多次修读以列表记录为准。
          </p>
        </footer>
      </div>
    </template>
  </div>
</template>

<script setup>
import { fetchMyStudentProfile, listScores } from '@/api/studentMs.js'
import { useUserStore } from '@/stores/user'
import { Printer, Refresh } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'

const userStore = useUserStore()
const loading = ref(true)
const errorMsg = ref('')
const profile = ref(null)
const scores = ref([])

const issuedDate = computed(() =>
  new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
)

const rows = computed(() => {
  const list = [...scores.value]
  list.sort((a, b) => {
    const s = (a.semester || '').localeCompare(b.semester || '', 'zh-CN')
    if (s !== 0) return s
    return (a.course || '').localeCompare(b.course || '', 'zh-CN')
  })
  return list
})

const avgText = computed(() => {
  if (!rows.value.length) return '—'
  const sum = rows.value.reduce((acc, r) => acc + Number(r.score), 0)
  return (sum / rows.value.length).toFixed(2)
})

const failCount = computed(() => rows.value.filter((r) => Number(r.score) < 60).length)

async function loadAll() {
  loading.value = true
  errorMsg.value = ''
  try {
    const [p, list] = await Promise.all([fetchMyStudentProfile(), listScores()])
    profile.value = p
    scores.value = Array.isArray(list) ? list : []
  } catch (e) {
    profile.value = null
    scores.value = []
    errorMsg.value = typeof e === 'string' ? e : '加载失败，请确认学籍档案已录入且学号与账号一致。'
  } finally {
    loading.value = false
  }
}

function handlePrint() {
  window.print()
}

onMounted(() => {
  loadAll()
})
</script>

<style scoped>
.transcript-page {
  max-width: 920px;
  margin: 0 auto;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.toolbar-hint {
  font-size: 13px;
  color: #64748b;
  margin-left: 4px;
}

.sheet {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 28px 32px 32px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.06);
}

.sheet-head {
  text-align: center;
  margin-bottom: 22px;
  padding-bottom: 16px;
  border-bottom: 2px solid #0f172a;
}

.school-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.12em;
}

.sheet-sub {
  margin: 0;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px 24px;
  margin-bottom: 20px;
  font-size: 14px;
}

.meta-item {
  display: flex;
  gap: 8px;
  border-bottom: 1px dashed #e2e8f0;
  padding-bottom: 6px;
}

.meta-item .k {
  color: #64748b;
  min-width: 72px;
  flex-shrink: 0;
}

.meta-item .v {
  color: #0f172a;
  font-weight: 500;
}

.grade-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.grade-table th,
.grade-table td {
  border: 1px solid #cbd5e1;
  padding: 10px 12px;
}

.grade-table thead th {
  background: #f1f5f9;
  font-weight: 600;
  color: #334155;
}

.col-idx {
  width: 56px;
}
.col-score {
  width: 88px;
}
.col-sem {
  width: 140px;
}

.center {
  text-align: center;
}

.muted {
  color: #475569;
}

.score-cell .fail {
  color: #dc2626;
  font-weight: 700;
}

.sheet-foot {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.stat-line {
  display: flex;
  flex-wrap: wrap;
  gap: 20px 28px;
  font-size: 14px;
  color: #334155;
}

.stat-line strong {
  color: #0f172a;
}

.foot-note {
  margin: 12px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
}
</style>

<style>
/* 打印时隐藏侧栏、顶栏与操作条（#app 提高优先级，避免与屏幕样式抢层叠） */
@media print {
  html body #app .app-wrapper .sidebar-container,
  html body #app .app-wrapper .sidebar-mask,
  html body #app .app-wrapper .header-box,
  html body #app .app-wrapper .no-print {
    display: none;
  }

  html body #app .app-wrapper .main-box {
    margin-left: 0;
    width: 100%;
    min-height: auto;
  }

  html body #app .app-wrapper .main-box .content-box {
    padding: 0;
  }

  html body #app .app-wrapper {
    background: #fff;
  }

  html body #app .app-wrapper .transcript-page {
    max-width: none;
  }

  html body #app .app-wrapper .transcript-page .sheet {
    border: none;
    box-shadow: none;
    border-radius: 0;
    padding: 12mm;
  }
}
</style>
