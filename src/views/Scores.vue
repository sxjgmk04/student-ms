<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>{{ pageTitle }}</span>
        <div class="header-actions" v-if="userStore.isAdmin">
          <el-input
            v-model="searchQuery"
            placeholder="搜索学号/姓名/课程名"
            class="toolbar-search"
            clearable
          >
            <template #prefix
              ><el-icon>
                <Search /> </el-icon
            ></template>
          </el-input>
          <el-button type="primary" class="toolbar-primary-btn" @click="handleAdd"
            >录入成绩</el-button
          >
        </div>
        <div class="header-actions" v-else-if="userStore.isStudent">
          <el-button type="primary" plain @click="$router.push('/transcript')"
            >成绩单打印</el-button
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
        class="mgmt-table"
        v-loading="loading"
      >
        <el-table-column prop="studentId" label="学号" align="center" width="120" />
        <el-table-column prop="name" label="姓名" align="center" width="110" />
        <el-table-column
          prop="course"
          label="课程名称"
          align="center"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column prop="score" label="考试分数" align="center" width="110">
          <template #default="{ row }">
            <span :class="scoreClass(row.score)">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="所属学期" align="center" width="140" />
      </el-table>
    </div>

    <el-empty
      v-else-if="!loading"
      class="page-empty"
      :image-size="120"
      :description="
        userStore.isAdmin
          ? '暂无成绩数据，可点击右上角「录入成绩」开始维护。'
          : '当前还没有录入成绩，请联系管理员。'
      "
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

    <el-dialog v-model="dialogVisible" title="成绩录入" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="92px" label-suffix="">
        <el-form-item label="学号" prop="studentId" required
          ><el-input v-model="form.studentId"
        /></el-form-item>
        <el-form-item label="姓名" prop="name" required
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="课程名称" prop="course" required
          ><el-input v-model="form.course"
        /></el-form-item>
        <el-form-item label="考试分数" prop="score" required>
          <el-input-number v-model="form.score" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="所属学期" prop="semester" required>
          <el-input v-model="form.semester" placeholder="例：2023-2024-1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认录入</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { createScore, listScores } from '@/api/studentMs.js'
import { emitDataUpdated } from '@/utils/dataSync.js'
import { filterFormXss } from '@/utils/security.js'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

const userStore = useUserStore()
const pageTitle = computed(() => (userStore.isAdmin ? '成绩管理' : '我的成绩'))
const data = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formRef = ref(null)
const form = ref({
  studentId: '',
  name: '',
  course: '',
  score: 0,
  semester: ''
})

const scoreClass = (score) => (Number(score) < 60 ? 'score-bad' : 'score-good')

const rules = {
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  course: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  score: [
    { required: true, message: '请输入分数', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '分数需在 0-100 之间', trigger: 'change' }
  ],
  semester: [{ required: true, message: '请输入所属学期', trigger: 'blur' }]
}

const filteredData = computed(() => {
  let result = [...data.value]
  if (userStore.isStudent) {
    result = result.filter((item) => item.studentId === userStore.currentStudentId)
  }
  if (userStore.isAdmin && searchQuery.value.trim()) {
    const keyword = searchQuery.value.trim()
    result = result.filter(
      (item) =>
        item.studentId.includes(keyword) ||
        item.name.includes(keyword) ||
        item.course.includes(keyword)
    )
  }
  return result
})

const paginatedData = computed(() => {
  if (userStore.isStudent) return filteredData.value
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

watch(searchQuery, () => {
  currentPage.value = 1
})

const loadList = async () => {
  loading.value = true
  try {
    data.value = await listScores()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '加载失败')
  } finally {
    loading.value = false
  }
}

let onDataUpdated = null
onMounted(() => {
  loadList()
  onDataUpdated = () => loadList()
  window.addEventListener('app-data-updated', onDataUpdated)
})

onUnmounted(() => {
  if (onDataUpdated) window.removeEventListener('app-data-updated', onDataUpdated)
})

const handleAdd = () => {
  if (!userStore.isAdmin) return
  form.value = {
    studentId: '',
    name: '',
    course: '',
    score: 0,
    semester: ''
  }
  dialogVisible.value = true
}

const handleSubmit = () => {
  if (!userStore.isAdmin) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const sanitized = filterFormXss({ ...form.value })
    try {
      await createScore(sanitized)
      await loadList()
      emitDataUpdated()
      ElMessage.success('成绩录入成功')
      dialogVisible.value = false
      currentPage.value = 1
    } catch (e) {
      ElMessage.error(typeof e === 'string' ? e : '录入失败')
    }
  })
}
</script>

<style scoped>
.score-good {
  color: var(--el-color-success);
  font-weight: 700;
}
.score-bad {
  color: var(--el-color-danger);
  font-weight: 700;
}
</style>
