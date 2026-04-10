<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>{{ pageTitle }}</span>
        <div class="header-actions" v-if="userStore.isAdmin">
          <el-input
            v-model="searchQuery"
            placeholder="搜索学号/姓名/奖惩内容"
            class="toolbar-search"
            clearable
          >
            <template #prefix
              ><el-icon>
                <Search /> </el-icon
            ></template>
          </el-input>
          <el-button type="primary" class="toolbar-primary-btn" @click="handleAdd"
            >新增记录</el-button
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
        <el-table-column prop="type" label="类型" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === '奖励' ? 'success' : 'danger'">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="title"
          label="奖惩内容"
          align="center"
          min-width="220"
          show-overflow-tooltip
        />
        <el-table-column prop="date" label="生效日期" align="center" width="130" />
      </el-table>
    </div>

    <el-empty
      v-else-if="!loading"
      class="page-empty"
      :image-size="120"
      :description="
        userStore.isAdmin
          ? '暂无奖惩记录，可点击右上角「新增记录」补充数据。'
          : '当前没有奖惩记录。'
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

    <el-dialog v-model="dialogVisible" title="新增奖惩记录" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="92px" label-suffix="">
        <el-form-item label="学号" prop="studentId" required
          ><el-input v-model="form.studentId"
        /></el-form-item>
        <el-form-item label="姓名" prop="name" required
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="类型" prop="type" required>
          <el-select v-model="form.type" style="width: 100%">
            <el-option label="奖励" value="奖励" />
            <el-option label="惩罚" value="惩罚" />
          </el-select>
        </el-form-item>
        <el-form-item label="奖惩内容" prop="title" required
          ><el-input v-model="form.title"
        /></el-form-item>
        <el-form-item label="生效日期" prop="date" required>
          <el-date-picker
            v-model="form.date"
            type="date"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认添加</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { createReward, listRewards } from '@/api/studentMs.js'
import { emitDataUpdated } from '@/utils/dataSync.js'
import { filterFormXss } from '@/utils/security.js'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

const userStore = useUserStore()
const pageTitle = computed(() => (userStore.isAdmin ? '奖惩管理' : '我的奖惩'))
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
  type: '',
  title: '',
  date: ''
})

const rules = {
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  title: [
    { required: true, message: '请输入奖惩内容', trigger: 'blur' },
    { min: 2, max: 200, message: '内容长度需在 2-200 之间', trigger: 'blur' }
  ],
  date: [{ required: true, message: '请选择生效日期', trigger: 'change' }]
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
        item.title.includes(keyword)
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
    data.value = await listRewards()
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
    type: '',
    title: '',
    date: ''
  }
  dialogVisible.value = true
}

const handleSubmit = () => {
  if (!userStore.isAdmin) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const sanitized = filterFormXss({ ...form.value })
    try {
      await createReward(sanitized)
      await loadList()
      emitDataUpdated()
      ElMessage.success('记录添加成功')
      dialogVisible.value = false
      currentPage.value = 1
    } catch (e) {
      ElMessage.error(typeof e === 'string' ? e : '添加失败')
    }
  })
}
</script>

<style scoped></style>
