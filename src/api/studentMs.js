import request from '@/utils/request.js'

export const fetchSummary = () => request.get('/summary')

export const fetchAnalyticsInsights = () => request.get('/analytics/insights')

export const fetchMyStudentInsight = () => request.get('/analytics/my-insight')
export const patchRiskFollowUp = (studentId, body) =>
  request.patch(`/analytics/risk-follow-up/${encodeURIComponent(studentId)}`, body)

export const fetchMyStudentProfile = () => request.get('/students/me')

export const listStudents = () => request.get('/students')
export const createStudent = (body) => request.post('/students', body)
export const updateStudent = (id, body) => request.put(`/students/${id}`, body)
export const removeStudent = (id) => request.delete(`/students/${id}`)

export const listChanges = () => request.get('/changes')
export const createChange = (body) => request.post('/changes', body)
export const patchChange = (id, body) => request.patch(`/changes/${id}`, body)

export const listScores = () => request.get('/scores')
export const createScore = (body) => request.post('/scores', body)

export const listRewards = () => request.get('/rewards')
export const createReward = (body) => request.post('/rewards', body)

export const listGraduation = () => request.get('/graduation')
export const patchGraduation = (id, body) => request.patch(`/graduation/${id}`, body)

export const listAuditLogs = () => request.get('/audit-logs')
export const clearAuditLogs = () => request.delete('/audit-logs')

export const logoutApi = () => request.post('/auth/logout')

export const fetchTodoSummary = () => request.get('/me/todos')

export const fetchAnnouncements = () => request.get('/announcements')

export const patchProfile = (body) => request.patch('/auth/profile', body)

export const changePassword = (body) => request.post('/auth/password', body)

export const uploadAvatar = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/auth/avatar', fd)
}

const aiTimeoutMs = 120000

const aiSafeAuthOptions = { skipAuthInvalid: true }

export const fetchAiStatus = () => request.get('/ai/status', aiSafeAuthOptions)
export const fetchAiModelConfig = () => request.get('/ai/model-config', aiSafeAuthOptions)
export const switchAiModel = (body) => request.post('/ai/model-config', body, aiSafeAuthOptions)

export const aiPolicyQa = (body) =>
  request.post('/ai/policy-qa', body, { timeout: aiTimeoutMs, ...aiSafeAuthOptions })

export const aiChangeAssist = (body) =>
  request.post('/ai/change-assist', body, { timeout: aiTimeoutMs, ...aiSafeAuthOptions })
