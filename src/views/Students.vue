<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>学生信息管理</span>
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
            >新增学生</el-button
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
        <el-table-column prop="gender" label="性别" align="center" width="90" />
        <el-table-column
          prop="major"
          label="专业"
          align="center"
          min-width="160"
          show-overflow-tooltip
        />
        <el-table-column
          prop="dataSource"
          label="数据来源"
          align="center"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="class" label="班级" align="center" width="120" />
        <el-table-column prop="grade" label="年级" align="center" width="100" />
        <el-table-column prop="status" label="状态" align="center" width="110">
          <template #default="{ row }">
            <el-tag
              :type="row.status === '在读' ? 'success' : row.status === '休学' ? 'warning' : 'info'"
            >
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="190"
          align="center"
          class-name="operation-col"
          v-if="userStore.isAdmin"
        >
          <template #default="{ row }">
            <div class="table-action-group">
              <el-button
                type="primary"
                size="small"
                class="table-action-btn"
                @click="handleEdit(row)"
                >编辑</el-button
              >
              <el-button
                type="danger"
                size="small"
                class="table-action-btn"
                @click="handleDelete(row.id)"
                >删除</el-button
              >
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-empty
      v-else-if="!loading"
      class="page-empty"
      :image-size="120"
      description="暂无学生数据，点击右上角「新增学生」开始录入。"
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" append-to-body>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="92px" label-suffix="">
        <el-form-item label="学号" prop="studentId"
          ><el-input v-model="form.studentId"
        /></el-form-item>
        <el-form-item label="姓名" prop="name"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业" prop="major"><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="班级" prop="class"><el-input v-model="form.class" /></el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input-number v-model="form.grade" :min="2000" :max="2030" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="在读" value="在读" />
            <el-option label="休学" value="休学" />
            <el-option label="毕业" value="毕业" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { createStudent, listStudents, removeStudent, updateStudent } from '@/api/studentMs.js'
import { emitDataUpdated } from '@/utils/dataSync.js'
import { filterFormXss } from '@/utils/security.js'
import { useUserStore } from '@/stores/user'
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

const userStore = useUserStore()
const students = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const isEdit = ref(false)
const form = ref({
  id: null,
  studentId: '',
  name: '',
  gender: '',
  major: '',
  class: '',
  grade: 2024,
  status: '在读'
})
const rules = {
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const filteredData = computed(() => {
  if (!searchQuery.value.trim()) return students.value
  const q = searchQuery.value.trim()
  return students.value.filter((item) => item.studentId.includes(q) || item.name.includes(q))
})

const paginatedData = computed(() => {
  const startIndex = (currentPage.value - 1) * pageSize.value
  const endIndex = startIndex + pageSize.value
  return filteredData.value.slice(startIndex, endIndex)
})

watch(searchQuery, () => {
  currentPage.value = 1
})

const loadList = async () => {
  if (!userStore.isAdmin) return
  loading.value = true
  try {
    students.value = await listStudents()
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '加载失败')
  } finally {
    loading.value = false
  }
}

let onDataUpdated = null
onMounted(() => {
  loadList()
  onDataUpdated = () => {
    if (userStore.isAdmin) loadList()
  }
  window.addEventListener('app-data-updated', onDataUpdated)
})

onUnmounted(() => {
  if (onDataUpdated) window.removeEventListener('app-data-updated', onDataUpdated)
})

const handleAdd = () => {
  if (!userStore.isAdmin) return
  isEdit.value = false
  dialogTitle.value = '新增学生'
  form.value = {
    id: null,
    studentId: '',
    name: '',
    gender: '',
    major: '',
    class: '',
    grade: 2024,
    status: '在读'
  }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  if (!userStore.isAdmin) return
  isEdit.value = true
  dialogTitle.value = '编辑学生'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = () => {
  if (!userStore.isAdmin) return
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const sanitized = filterFormXss({ ...form.value })
    try {
      if (isEdit.value) {
        await updateStudent(sanitized.id, sanitized)
        ElMessage.success('编辑成功')
      } else {
        const rest = { ...sanitized }
        delete rest.id
        await createStudent(rest)
        ElMessage.success('新增成功')
      }
      await loadList()
      emitDataUpdated()
      dialogVisible.value = false
      currentPage.value = 1
    } catch (e) {
      ElMessage.error(typeof e === 'string' ? e : '保存失败')
    }
  })
}

const handleDelete = (id) => {
  if (!userStore.isAdmin) return
  ElMessageBox.confirm('确定要删除这条学生数据吗？', '删除提示', {
    type: 'warning'
  })
    .then(async () => {
      try {
        await removeStudent(id)
        await loadList()
        emitDataUpdated()
        ElMessage.success('删除成功')
      } catch (e) {
        ElMessage.error(typeof e === 'string' ? e : '删除失败')
      }
    })
    .catch(() => {})
}
</script>

<style scoped></style>
