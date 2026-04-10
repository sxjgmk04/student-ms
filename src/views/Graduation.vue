<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>毕业资格审核</span>
        <div class="header-actions" v-if="userStore.isAdmin">
          <el-input
            v-model="searchQuery"
            placeholder="搜索学号/姓名/专业"
            class="toolbar-search"
            clearable
          >
            <template #prefix
              ><el-icon>
                <Search /> </el-icon
            ></template>
          </el-input>
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
        <el-table-column
          prop="major"
          label="专业"
          align="center"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column prop="thesisScore" label="毕业论文分数" align="center" width="130" />
        <el-table-column prop="status" label="审核状态" align="center" width="110">
          <template #default="{ row }">
            <el-tag
              :type="
                row.status === '通过' ? 'success' : row.status === '待审核' ? 'warning' : 'danger'
              "
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="220"
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
                @click="handleAudit(row, '通过')"
                :disabled="row.status !== '待审核'"
              >
                审核通过
              </el-button>
              <el-button
                type="danger"
                size="small"
                class="table-action-btn"
                @click="handleAudit(row, '不通过')"
                :disabled="row.status !== '待审核'"
              >
                审核不通过
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
      description="暂无毕业审核数据，请先导入或新增毕业相关记录。"
    />

    <el-pagination
      v-if="userStore.isAdmin && filteredData.length"
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
import { listGraduation, patchGraduation } from '@/api/studentMs.js'
import { emitDataUpdated } from '@/utils/dataSync.js'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

const userStore = useUserStore()
const data = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const filteredData = computed(() => {
  if (!searchQuery.value.trim()) return data.value
  const keyword = searchQuery.value.trim()
  return data.value.filter(
    (item) =>
      item.studentId.includes(keyword) ||
      item.name.includes(keyword) ||
      item.major.includes(keyword)
  )
})

const paginatedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

watch(searchQuery, () => {
  currentPage.value = 1
})

const loadList = async () => {
  if (!userStore.isAdmin) return
  loading.value = true
  try {
    data.value = await listGraduation()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '加载失败')
  } finally {
    loading.value = false
  }
}

let onDataUpdated = null
onMounted(() => {
  loadList()
  onDataUpdated = () => userStore.isAdmin && loadList()
  window.addEventListener('app-data-updated', onDataUpdated)
})

onUnmounted(() => {
  if (onDataUpdated) window.removeEventListener('app-data-updated', onDataUpdated)
})

const handleAudit = async (row, status) => {
  if (!userStore.isAdmin) return
  try {
    const updated = await patchGraduation(row.id, { status })
    const idx = data.value.findIndex((x) => x.id === row.id)
    if (idx >= 0) data.value[idx] = updated
    ElMessage.success(`审核${status}成功`)
    emitDataUpdated()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '审核失败')
  }
}
</script>

<style scoped></style>
