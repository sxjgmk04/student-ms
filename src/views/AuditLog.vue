<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>操作日志审计</span>
        <div class="header-actions">
          <el-input
            v-model="searchQuery"
            placeholder="搜索操作人/操作类型/操作详情"
            class="toolbar-search"
            clearable
          >
            <template #prefix
              ><el-icon>
                <Search /> </el-icon
            ></template>
          </el-input>
          <el-dropdown trigger="click" placement="bottom-end">
            <el-button plain class="toolbar-columns-btn" aria-label="列显示与隐藏">
              <el-icon aria-hidden="true">
                <Setting />
              </el-icon>
              列显示
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <div
                  class="audit-column-picker"
                  role="group"
                  aria-label="选择显示的表格列"
                  @click.stop
                >
                  <label v-for="col in columnDefs" :key="col.key" class="audit-column-picker-row">
                    <el-checkbox
                      :model-value="columnVisibility[col.key]"
                      @update:model-value="(v) => setColumnVisible(col.key, v)"
                    />
                    <span>{{ col.label }}</span>
                  </label>
                </div>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button
            type="success"
            class="toolbar-success-btn"
            @click="exportLogs"
            :disabled="!logs.length"
            >导出日志</el-button
          >
          <el-button
            type="danger"
            class="toolbar-danger-btn"
            @click="handleClearLogs"
            :disabled="!logs.length"
            >清空所有日志</el-button
          >
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
        class="mgmt-table audit-log-table"
        v-loading="loading"
      >
        <el-table-column
          v-if="columnVisibility.id"
          prop="id"
          label="日志ID"
          align="center"
          width="80"
        />
        <el-table-column
          v-if="columnVisibility.operatorName"
          prop="operatorName"
          label="操作人"
          align="center"
          width="100"
        />
        <el-table-column
          v-if="columnVisibility.operatorRole"
          prop="operatorRole"
          label="角色"
          align="center"
          width="110"
        >
          <template #default="{ row }">
            <el-tag :type="row.operatorRole === 'admin' ? 'primary' : 'info'" size="small">
              {{ row.operatorRole === 'admin' ? '管理员' : '学生' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          v-if="columnVisibility.action"
          prop="action"
          label="操作类型"
          align="center"
          width="160"
        />
        <el-table-column
          v-if="columnVisibility.detail"
          prop="detail"
          label="操作详情"
          align="center"
          show-overflow-tooltip
        />
        <el-table-column
          v-if="columnVisibility.ip"
          prop="ip"
          label="操作IP"
          align="center"
          width="120"
        />
        <el-table-column
          v-if="columnVisibility.operateTime"
          prop="operateTime"
          label="操作时间"
          align="center"
          width="180"
        />
      </el-table>
    </div>

    <el-empty
      v-else-if="!loading"
      class="page-empty"
      :image-size="120"
      description="当前没有操作日志，系统发生登录或业务操作后会自动记录。"
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
  </el-card>
</template>

<script setup>
import { clearAuditLogs, listAuditLogs } from '@/api/studentMs.js'
import { Search, Setting } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'

const columnDefs = [
  { key: 'id', label: '日志ID' },
  { key: 'operatorName', label: '操作人' },
  { key: 'operatorRole', label: '角色' },
  { key: 'action', label: '操作类型' },
  { key: 'detail', label: '操作详情' },
  { key: 'ip', label: '操作IP' },
  { key: 'operateTime', label: '操作时间' }
]

const columnVisibility = reactive({
  id: true,
  operatorName: true,
  operatorRole: true,
  action: true,
  detail: true,
  ip: true,
  operateTime: true
})

const setColumnVisible = (key, visible) => {
  if (!visible) {
    const otherOn = Object.entries(columnVisibility).some(([k, v]) => k !== key && v)
    if (!otherOn) {
      ElMessage.warning('至少保留一列')
      return
    }
  }
  columnVisibility[key] = visible
}

const logs = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchQuery.value.trim()) return logs.value
  const keyword = searchQuery.value.trim().toLowerCase()
  return logs.value.filter(
    (item) =>
      String(item.operatorName).toLowerCase().includes(keyword) ||
      String(item.action).toLowerCase().includes(keyword) ||
      String(item.detail).toLowerCase().includes(keyword) ||
      String(item.operateTime).includes(keyword)
  )
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

const loadLogs = async () => {
  loading.value = true
  try {
    logs.value = await listAuditLogs()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '加载失败')
  } finally {
    loading.value = false
  }
}

let onDataUpdated = null
onMounted(() => {
  loadLogs()
  onDataUpdated = () => loadLogs()
  window.addEventListener('app-data-updated', onDataUpdated)
})

onUnmounted(() => {
  if (onDataUpdated) window.removeEventListener('app-data-updated', onDataUpdated)
})

const exportLogs = () => {
  if (filteredData.value.length === 0) {
    ElMessage.warning('暂无日志可导出')
    return
  }

  const toCsvCell = (value) => {
    if (value === null || value === undefined) return '""'
    let str = String(value)
    const trimmed = str.replace(/^\s+/, '')
    if (/^[=+\-@]/.test(trimmed)) str = `'${str}`
    str = str.replace(/"/g, '""')
    return `"${str}"`
  }

  const headers = ['日志ID', '操作人', '角色', '操作类型', '操作详情', '操作IP', '操作时间']
  const csvContent = [
    headers.map(toCsvCell).join(','),
    ...filteredData.value.map((item) => {
      const row = [
        toCsvCell(item.id),
        toCsvCell(item.operatorName),
        toCsvCell(item.operatorRole === 'admin' ? '管理员' : '学生'),
        toCsvCell(item.action),
        toCsvCell(item.detail),
        toCsvCell(item.ip),
        toCsvCell(item.operateTime)
      ]
      return row.join(',')
    })
  ].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `学籍系统操作日志_${new Date().toISOString().split('T')[0]}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('日志导出成功')
}

const handleClearLogs = () => {
  ElMessageBox.confirm('确定要清空所有操作日志吗？此操作不可恢复！', '警告', { type: 'warning' })
    .then(async () => {
      try {
        await clearAuditLogs()
        logs.value = []
        ElMessage.success('所有日志已清空')
      } catch (e) {
        ElMessage.error(typeof e === 'string' ? e : '清空失败')
      }
    })
    .catch(() => {})
}
</script>

<style scoped>
.toolbar-columns-btn {
  min-width: 96px;
  height: 38px;
  border-radius: 10px;
  font-weight: 600;
}

.audit-column-picker {
  padding: 10px 14px;
  min-width: 168px;
}

.audit-column-picker-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  cursor: pointer;
  font-size: 13px;
  color: #334155;
  user-select: none;
}

.audit-column-picker-row :deep(.el-checkbox) {
  height: auto;
}
</style>
