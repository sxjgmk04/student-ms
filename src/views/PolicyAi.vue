<template>
  <el-card class="page-card chat-page" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>{{ pageTitle }}</span>
        <div class="header-actions">
          <el-button text type="primary" :loading="statusLoading" @click="loadStatus"
            >刷新状态</el-button
          >
        </div>
      </div>
    </template>

    <div class="status-strip" :class="statusClass">
      <span class="dot"></span>
      <span class="status-text">{{ statusLine }}</span>
    </div>

    <div v-if="isAdmin" class="model-switch-card" v-loading="modelConfigLoading">
      <div class="model-switch-head">
        <div>
          <p class="model-switch-title">模型切换（管理员）</p>
          <p class="model-switch-sub">
            当前：{{ modelConfig?.currentLabel || '默认配置' }} ·
            {{ modelConfig?.currentModel || '未配置模型' }}
          </p>
        </div>
      </div>
      <div class="model-switch-body">
        <el-select v-model="selectedModelKey" class="model-select" placeholder="请选择模型方案">
          <el-option
            v-for="opt in modelOptions"
            :key="opt.key"
            :label="`${opt.label}（${opt.model || '跟随配置'}）`"
            :value="opt.key"
          />
        </el-select>
        <el-button type="primary" :loading="modelConfigSaving" @click="applyModelSwitch"
          >应用模型</el-button
        >
      </div>
    </div>

    <div class="chat-shell">
      <div ref="messageListRef" class="message-list">
        <div v-if="!messages.length" class="empty-state">
          <div class="empty-icon">AI</div>
          <p class="empty-title">{{ emptyTitle }}</p>
          <p class="empty-subtitle">{{ emptySubtitle }}</p>
        </div>

        <template v-else>
          <div v-for="msg in messages" :key="msg.id" class="message-row" :class="msg.role">
            <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
            <div class="bubble">
              <el-skeleton v-if="msg.pending" animated :rows="3" />
              <template v-else>{{ msg.content }}</template>
            </div>
          </div>
        </template>
      </div>

      <div class="composer">
        <el-input
          v-model="question"
          type="textarea"
          :rows="4"
          maxlength="800"
          show-word-limit
          resize="none"
          :placeholder="inputPlaceholder"
          @keydown.ctrl.enter.prevent="submit"
        />
        <div class="composer-actions">
          <span class="hotkey-tip">Ctrl + Enter 发送</span>
          <div class="btn-group">
            <el-button :disabled="loading || !messages.length" @click="clearConversation"
              >新对话</el-button
            >
            <el-button type="primary" :loading="loading" @click="submit">发送</el-button>
          </div>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { aiPolicyQa, fetchAiModelConfig, fetchAiStatus, switchAiModel } from '@/api/studentMs.js'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { computed, nextTick, onMounted, ref } from 'vue'

const userStore = useUserStore()
const question = ref('')
const loading = ref(false)
const aiStatus = ref(null)
const statusLoading = ref(true)
const statusError = ref('')
const messages = ref([])
const messageListRef = ref(null)
const modelConfigLoading = ref(false)
const modelConfigSaving = ref(false)
const modelConfig = ref(null)
const selectedModelKey = ref('')

const isAdmin = computed(() => userStore.isAdmin)
const pageTitle = computed(() => (isAdmin.value ? '智能问答（管理端）' : '智能问答（学生端）'))
const emptyTitle = computed(() => (isAdmin.value ? '开始管理智能问答' : '开始学生智能问答'))
const emptySubtitle = computed(() =>
  isAdmin.value
    ? '例如：休学审批需要哪些依据？复学申请审核重点是什么？'
    : '例如：休学怎么申请？复学需要准备哪些材料？'
)
const inputPlaceholder = computed(() =>
  isAdmin.value
    ? '请输入管理视角问题（审核口径/适用条件/边界情形）...'
    : '请输入你要咨询的办理问题（流程/材料/时限），支持多轮追问...'
)
const modelOptions = computed(() => modelConfig.value?.options || [])

const isAuthInvalidError = (e) => {
  const msg = typeof e === 'string' ? e : e?.message || ''
  return /未登录|登录已失效|401|unauthorized/i.test(msg)
}

const statusLine = computed(() => {
  if (statusLoading.value) return '正在检测模型状态...'
  if (statusError.value) return `状态获取失败：${statusError.value}`
  if (!aiStatus.value) return '模型状态未知'
  if (!aiStatus.value.ready) return '模型未就绪，请联系管理员检查 AI 配置'
  const parts = []
  if (aiStatus.value.providerLabel) parts.push(aiStatus.value.providerLabel)
  if (aiStatus.value.effectiveModel) parts.push(aiStatus.value.effectiveModel)
  return `模型已就绪 · ${parts.join(' · ')}`
})

const statusClass = computed(() => {
  if (statusLoading.value) return 'pending'
  if (statusError.value) return 'error'
  return aiStatus.value?.ready ? 'ready' : 'warn'
})

const loadStatus = async () => {
  statusLoading.value = true
  statusError.value = ''
  try {
    aiStatus.value = await fetchAiStatus()
  } catch (e) {
    aiStatus.value = null
    statusError.value = typeof e === 'string' ? e : '请检查网络或登录状态'
  } finally {
    statusLoading.value = false
  }
}

const loadModelConfig = async () => {
  if (!isAdmin.value) return
  modelConfigLoading.value = true
  try {
    const cfg = await fetchAiModelConfig()
    modelConfig.value = cfg
    selectedModelKey.value = cfg?.currentKey || 'default'
  } catch (e) {
    if (isAuthInvalidError(e)) return
    ElMessage.error(typeof e === 'string' ? e : '加载模型配置失败')
  } finally {
    modelConfigLoading.value = false
  }
}

const applyModelSwitch = async () => {
  if (!isAdmin.value) return
  if (!selectedModelKey.value) {
    ElMessage.warning('请选择模型')
    return
  }
  modelConfigSaving.value = true
  try {
    const cfg = await switchAiModel({ optionKey: selectedModelKey.value })
    modelConfig.value = cfg
    selectedModelKey.value = cfg?.currentKey || selectedModelKey.value
    await loadStatus()
    ElMessage.success(`已切换为：${cfg?.currentLabel || '新模型'}`)
  } catch (e) {
    if (isAuthInvalidError(e)) return
    ElMessage.error(typeof e === 'string' ? e : '模型切换失败')
  } finally {
    modelConfigSaving.value = false
  }
}

const scrollToBottom = async () => {
  await nextTick()
  const el = messageListRef.value
  if (!el) return
  el.scrollTop = el.scrollHeight
}

const submit = async () => {
  const q = question.value.trim()
  if (!q) {
    ElMessage.warning('请输入问题')
    return
  }

  const userId = Date.now() + Math.random()
  const aiId = userId + 1
  messages.value.push({ id: userId, role: 'user', content: q, pending: false })
  messages.value.push({ id: aiId, role: 'assistant', content: '', pending: true })
  question.value = ''
  loading.value = true
  await scrollToBottom()

  try {
    const res = await aiPolicyQa({ question: q })
    const reply = (res?.answer || '').trim()
    const idx = messages.value.findIndex((m) => m.id === aiId)
    if (idx >= 0) {
      messages.value[idx] = {
        id: aiId,
        role: 'assistant',
        content:
          reply ||
          (isAdmin.value
            ? '未获取到有效回答，请稍后重试或改用更具体的管理场景问题。'
            : '抱歉，未收到有效回答，请稍后重试或换一种提问方式。'),
        pending: false
      }
    }
  } catch (e) {
    const idx = messages.value.findIndex((m) => m.id === aiId)
    if (idx >= 0) {
      messages.value[idx] = {
        id: aiId,
        role: 'assistant',
        content:
          typeof e === 'string'
            ? e
            : isAdmin.value
              ? '请求失败，请稍后重试；如持续失败请检查模型配置或平台额度。'
              : '请求失败，请稍后重试；若多次失败请联系管理员。',
        pending: false
      }
    }
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

const clearConversation = () => {
  if (loading.value) return
  messages.value = []
  question.value = ''
}

onMounted(() => {
  loadStatus()
  loadModelConfig()
})
</script>

<style scoped>
.chat-page {
  overflow: hidden;
  /* 与右侧 content-box 同高，避免 vh 估算误差导致外层滚动 */
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.chat-page :deep(.el-card__body) {
  height: 100%;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.chat-page :deep(.el-card__header) {
  flex-shrink: 0;
}

.status-strip {
  display: flex;
  align-items: center;
  gap: 8px;
  border-radius: 10px;
  padding: 10px 12px;
  margin-bottom: 14px;
  font-size: 13px;
}

.status-strip .dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex: 0 0 8px;
}

.status-strip.ready {
  color: #166534;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
}

.status-strip.ready .dot {
  background: #22c55e;
}

.status-strip.warn {
  color: #854d0e;
  background: #fefce8;
  border: 1px solid #fde68a;
}

.status-strip.warn .dot {
  background: #eab308;
}

.status-strip.error {
  color: #991b1b;
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.status-strip.error .dot {
  background: #ef4444;
}

.status-strip.pending {
  color: #0f172a;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.status-strip.pending .dot {
  background: #64748b;
}

.model-switch-card {
  margin-bottom: 12px;
  border: 1px solid #dbeafe;
  border-radius: 12px;
  background: linear-gradient(180deg, #eff6ff 0%, #f8fbff 100%);
  padding: 12px;
}

.model-switch-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.model-switch-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #1e3a8a;
}

.model-switch-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: #64748b;
}

.model-switch-body {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.model-select {
  min-width: 320px;
  max-width: 560px;
}

.chat-shell {
  flex: 1;
  min-height: 0;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #fff;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 18px;
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.08), transparent 34%),
    linear-gradient(180deg, #fbfdff 0%, #f8fafc 100%);
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: #94a3b8;
}

.empty-icon {
  width: 50px;
  height: 50px;
  border-radius: 14px;
  margin-bottom: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  border: 1px solid #dbeafe;
  background: linear-gradient(135deg, #eff6ff, #ffffff);
}

.empty-title {
  margin: 0;
  font-size: 16px;
  color: #334155;
  font-weight: 600;
}

.empty-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
}

.message-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.message-row.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  flex: 0 0 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.message-row.user .avatar {
  color: #fff;
  background: linear-gradient(135deg, #111827, #374151);
}

.message-row.assistant .avatar {
  color: #0369a1;
  background: linear-gradient(135deg, #e0f2fe, #f0f9ff);
  border: 1px solid rgba(125, 211, 252, 0.6);
}

.bubble {
  max-width: min(74%, 780px);
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.75;
  font-size: 14px;
  border-radius: 14px;
  padding: 10px 12px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.05);
}

.message-row.user .bubble {
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}

.message-row.assistant .bubble {
  color: #1f2937;
  background: #fff;
  border: 1px solid #e5e7eb;
}

.composer {
  border-top: 1px solid #e5e7eb;
  padding: 14px 14px 12px;
  background: #fff;
}

.composer-actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.hotkey-tip {
  color: #94a3b8;
  font-size: 12px;
}

.btn-group {
  display: flex;
  gap: 8px;
}
</style>
