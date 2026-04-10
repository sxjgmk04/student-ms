<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>干预记录时间轴</span>
        <el-input
          v-model="searchQuery"
          placeholder="搜索学号/备注/操作者"
          clearable
          style="max-width: 320px"
        />
      </div>
    </template>
    <el-empty v-if="!filtered.length" description="暂无干预记录" />
    <el-timeline v-else>
      <el-timeline-item
        v-for="item in filtered"
        :key="item.id"
        :timestamp="item.operateTime"
        placement="top"
      >
        <el-card shadow="never">
          <div class="row">
            <strong>{{ item.action }}</strong>
            <el-tag size="small" type="info">{{ item.operatorName || '系统' }}</el-tag>
          </div>
          <div class="detail">{{ item.detail }}</div>
        </el-card>
      </el-timeline-item>
    </el-timeline>
  </el-card>
</template>

<script setup>
import { listAuditLogs } from '@/api/studentMs.js'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'

const logs = ref([])
const searchQuery = ref('')

const filtered = computed(() => {
  const rows = logs.value.filter((x) => String(x.action || '').includes('学业风险跟进状态更新'))
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return rows
  return rows.filter(
    (x) =>
      String(x.detail || '')
        .toLowerCase()
        .includes(q) ||
      String(x.operatorName || '')
        .toLowerCase()
        .includes(q) ||
      String(x.action || '')
        .toLowerCase()
        .includes(q)
  )
})

onMounted(async () => {
  try {
    logs.value = await listAuditLogs()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '加载干预记录失败')
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.detail {
  color: #475569;
  font-size: 13px;
  line-height: 1.7;
}
</style>
