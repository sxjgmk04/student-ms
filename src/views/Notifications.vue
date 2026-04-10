<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>通知公告</span>
        <div class="header-actions">
          <el-button class="toolbar-primary-btn" :loading="loading" @click="load">刷新</el-button>
        </div>
      </div>
    </template>

    <el-empty
      v-if="!loading && !list.length"
      description="暂无公告"
      class="page-empty"
      :image-size="120"
    />

    <el-timeline v-else v-loading="loading" class="announce-timeline">
      <el-timeline-item
        v-for="item in list"
        :key="item.id"
        :timestamp="item.createdAt"
        placement="top"
        type="primary"
        hollow
      >
        <div class="announce-item">
          <div class="announce-title">{{ item.title }}</div>
          <p class="announce-body">{{ item.content }}</p>
        </div>
      </el-timeline-item>
    </el-timeline>
  </el-card>
</template>

<script setup>
import { fetchAnnouncements } from '@/api/studentMs.js'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'

const list = ref([])
const loading = ref(false)

const load = async () => {
  loading.value = true
  try {
    list.value = await fetchAnnouncements()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
</script>

<style scoped>
.announce-timeline {
  padding: 4px 8px 8px;
  max-width: 720px;
}

.announce-item {
  padding: 4px 0 8px;
}

.announce-title {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 8px;
}

.announce-body {
  margin: 0;
  font-size: 14px;
  color: #475569;
  line-height: 1.75;
  white-space: pre-wrap;
}
</style>
