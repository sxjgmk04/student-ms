<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>{{ pageTitle }}</span>
        <div class="header-actions" v-if="userStore.isAdmin">
          <el-input
            v-model="searchQuery"
            placeholder="搜索学号/姓名"
            class="toolbar-search"
            clearable
          >
            <template #prefix
              ><el-icon>
                <Search /> </el-icon
            ></template>
          </el-input>
          <el-button type="primary" class="toolbar-primary-btn" @click="handleAdd"
            >新增申请</el-button
          >
        </div>
        <div class="header-actions" v-else-if="userStore.isStudent">
          <el-tooltip
            :content="studentSubmitTooltip"
            :disabled="!studentSubmitTooltip"
            placement="bottom"
          >
            <span class="student-submit-wrap">
              <el-button
                type="primary"
                class="toolbar-primary-btn"
                :disabled="studentSubmitDisabled"
                @click="handleAdd"
              >
                提交异动申请
              </el-button>
            </span>
          </el-tooltip>
        </div>
      </div>
    </template>

    <div v-if="filteredData.length" class="table-scroll">
      <el-table
        :data="paginatedData"
        border
        stripe
        style="width: 100%"
        :row-style="{ height: '52px' }"
        class="mgmt-table"
        v-loading="loading"
      >
        <el-table-column prop="studentId" label="学号" align="center" width="120" />
        <el-table-column prop="name" label="姓名" align="center" width="110" />
        <el-table-column prop="type" label="异动类型" align="center" width="120" />
        <el-table-column
          prop="reason"
          label="申请原因"
          align="center"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column prop="status" label="审核状态" align="center" width="110">
          <template #default="{ row }">
            <el-tag
              :type="
                row.status === '已通过' ? 'success' : row.status === '审核中' ? 'warning' : 'danger'
              "
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="date" label="申请日期" align="center" width="130" />
        <el-table-column
          label="操作"
          width="200"
          align="center"
          class-name="operation-col"
          v-if="userStore.isAdmin"
        >
          <template #default="{ row }">
            <div class="table-action-group">
              <el-button
                type="success"
                size="small"
                class="table-action-btn"
                @click="handleAudit(row, '已通过')"
                :disabled="row.status !== '审核中'"
              >
                通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                class="table-action-btn"
                @click="handleAudit(row, '不通过')"
                :disabled="row.status !== '审核中'"
              >
                拒绝
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty
      v-else-if="!loading"
      class="page-empty"
      :image-size="120"
      :description="emptyDescription"
    />

    <el-pagination
      v-if="filteredData.length"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="filteredData.length"
      layout="total, sizes, prev, pager, next, jumper"
      class="page-pagination"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" append-to-body>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="92px" label-suffix="">
        <el-form-item v-if="userStore.isAdmin" label="学号" prop="studentId" required>
          <el-input v-model="form.studentId" />
        </el-form-item>
        <el-form-item v-if="userStore.isAdmin" label="姓名" prop="name" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <template v-else>
          <el-form-item label="学号">
            <el-input :model-value="form.studentId" disabled />
          </el-form-item>
          <el-form-item label="姓名">
            <el-input :model-value="form.name" disabled />
          </el-form-item>
        </template>
        <el-form-item label="异动类型" prop="type" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="转专业" value="转专业" />
            <el-option label="休学" value="休学" />
            <el-option label="复学" value="复学" />
            <el-option label="转学" value="转学" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请原因" prop="reason" required>
          <el-input v-model="form.reason" type="textarea" :rows="3" />
        </el-form-item>
        <el-divider content-position="left">AI 辅助填写（可选）</el-divider>
        <el-alert
          v-if="aiAssistStatusLoaded && !aiAssistReady"
          type="warning"
          :closable="false"
          show-icon
          class="ai-dialog-alert"
          title="大模型未就绪"
          description="AI 辅助需管理员在后端开启并填写 API Key（可选用国内平台，设置 AI_PRESET=siliconflow 等）。配置说明见侧栏「智能问答」页。"
        />
        <p class="ai-assist-hint">
          用口语描述您的诉求，AI 将尝试匹配异动类型并生成书面原因；提交前请务必自行核对、修改。
        </p>
        <el-form-item label="口语描述">
          <el-input
            v-model="aiAssistDraft"
            type="textarea"
            :rows="2"
            maxlength="600"
            show-word-limit
            placeholder="例如：家里有事想停学一年，明年再回来读。"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain :loading="aiAssistLoading" @click="runAiAssist"
            >填入类型与原因</el-button
          >
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交申请</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import {
  aiChangeAssist,
  createChange,
  fetchAiStatus,
  listChanges,
  patchChange
} from '@/api/studentMs.js'
import { emitDataUpdated } from '@/utils/dataSync.js'
import { filterFormXss } from '@/utils/security.js'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

const userStore = useUserStore()
const pageTitle = computed(() => (userStore.isAdmin ? '学籍异动管理' : '我的学籍异动'))
const dialogTitle = computed(() => (userStore.isAdmin ? '新增异动申请' : '提交异动申请'))
const studentHasPendingAudit = computed(
  () => userStore.isStudent && data.value.some((row) => row.status === '审核中')
)

const studentSubmitDisabled = computed(() => {
  if (!userStore.isStudent) return true
  if (!userStore.currentStudentId) return true
  return studentHasPendingAudit.value
})

const studentSubmitTooltip = computed(() => {
  if (!userStore.isStudent) return ''
  if (!userStore.currentStudentId) return '当前账号未绑定学号，无法提交。'
  if (studentHasPendingAudit.value) return '您已有审核中的申请，请等待管理员处理后再提交。'
  return ''
})

const emptyDescription = computed(() => {
  if (userStore.isAdmin) return '暂无异动申请数据，可点击右上角「新增申请」创建。'
  if (!userStore.currentStudentId) return '当前账号未绑定学号，无法在线提交异动申请，请联系管理员。'
  if (studentHasPendingAudit.value) {
    return '您有一条或多条审核中的异动，处理完毕前无法提交新申请。'
  }
  return '暂无与您学号相关的异动记录。可点击右上角「提交异动申请」发起。'
})
const data = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const aiAssistDraft = ref('')
const aiAssistLoading = ref(false)
const aiAssistReady = ref(false)
const aiAssistStatusLoaded = ref(false)
const formRef = ref(null)
const form = ref({
  studentId: '',
  name: '',
  type: '',
  reason: '',
  status: '审核中',
  date: new Date().toISOString().split('T')[0]
})

const reasonRules = [
  { required: true, message: '请输入申请原因', trigger: 'blur' },
  { min: 2, max: 500, message: '申请原因长度需在 2-500 之间', trigger: 'blur' }
]

const formRules = computed(() => {
  const typeRule = [{ required: true, message: '请选择异动类型', trigger: 'change' }]
  if (userStore.isAdmin) {
    return {
      studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
      name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
      type: typeRule,
      reason: reasonRules
    }
  }
  return {
    type: typeRule,
    reason: reasonRules
  }
})

const filteredData = computed(() => {
  if (!searchQuery.value.trim()) return data.value
  const q = searchQuery.value.trim()
  return data.value.filter((item) => item.studentId.includes(q) || item.name.includes(q))
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

watch(searchQuery, () => {
  currentPage.value = 1
})

const loadList = async () => {
  loading.value = true
  try {
    data.value = await listChanges()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '加载失败')
  } finally {
    loading.value = false
  }
}

let onDataUpdated = null
const loadAiAssistStatus = async () => {
  try {
    const s = await fetchAiStatus()
    aiAssistReady.value = !!s?.ready
  } catch {
    aiAssistReady.value = false
  } finally {
    aiAssistStatusLoaded.value = true
  }
}

onMounted(() => {
  loadList()
  loadAiAssistStatus()
  onDataUpdated = () => loadList()
  window.addEventListener('app-data-updated', onDataUpdated)
})

onUnmounted(() => {
  if (onDataUpdated) window.removeEventListener('app-data-updated', onDataUpdated)
})

const handleAdd = () => {
  aiAssistDraft.value = ''
  if (userStore.isAdmin) {
    form.value = {
      studentId: '',
      name: '',
      type: '',
      reason: '',
      status: '审核中',
      date: new Date().toISOString().split('T')[0]
    }
    dialogVisible.value = true
    return
  }
  if (!userStore.isStudent) return
  if (!userStore.currentStudentId) {
    ElMessage.warning('当前账号未绑定学号，无法提交申请')
    return
  }
  if (studentHasPendingAudit.value) {
    ElMessage.warning('您已有审核中的申请，请等待审核结果后再提交')
    return
  }
  form.value = {
    studentId: userStore.currentStudentId,
    name: userStore.userInfo?.name || '',
    type: '',
    reason: '',
    status: '审核中',
    date: new Date().toISOString().split('T')[0]
  }
  dialogVisible.value = true
}

const runAiAssist = async () => {
  const d = aiAssistDraft.value.trim()
  if (!d) {
    ElMessage.warning('请先输入口语描述')
    return
  }
  aiAssistLoading.value = true
  try {
    const res = await aiChangeAssist({ description: d })
    if (res?.type) form.value.type = res.type
    if (res?.reason) form.value.reason = res.reason
    ElMessage.success(res?.note || '已填入，请核对后再提交')
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '辅助失败')
  } finally {
    aiAssistLoading.value = false
  }
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const sanitized = filterFormXss({ ...form.value })
    try {
      if (userStore.isAdmin) {
        await createChange(sanitized)
      } else {
        await createChange({
          type: sanitized.type,
          reason: sanitized.reason,
          date: sanitized.date
        })
      }
      await loadList()
      emitDataUpdated()
      ElMessage.success(userStore.isAdmin ? '申请提交成功' : '申请已提交，请等待管理员审核')
      dialogVisible.value = false
      currentPage.value = 1
    } catch (e) {
      ElMessage.error(typeof e === 'string' ? e : '提交失败')
    }
  })
}

const handleAudit = async (row, status) => {
  if (!userStore.isAdmin) return
  try {
    const updated = await patchChange(row.id, { status })
    const idx = data.value.findIndex((x) => x.id === row.id)
    if (idx >= 0) data.value[idx] = updated
    ElMessage.success(`审核${status}成功`)
    emitDataUpdated()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '审核失败')
  }
}
</script>

<style scoped>
.student-submit-wrap {
  display: inline-flex;
  vertical-align: middle;
}

.ai-dialog-alert {
  margin: 0 0 12px;
}

.ai-assist-hint {
  margin: -8px 0 12px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}
</style>
