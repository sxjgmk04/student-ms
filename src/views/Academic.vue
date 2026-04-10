<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>{{ pageTitle }}</span>
      </div>
    </template>

    <div class="overview-strip">
      <div class="overview-item">
        <span class="overview-label">{{
          userStore.isAdmin ? '开课总量（演示）' : '本学期课程（演示）'
        }}</span>
        <strong class="overview-value">{{
          userStore.isAdmin ? adminCourseRows.length : studentScheduleRows.length
        }}</strong>
      </div>
      <div class="overview-item">
        <span class="overview-label">{{
          userStore.isAdmin ? '教室资源（演示）' : '本周上课天数（演示）'
        }}</span>
        <strong class="overview-value">{{ userStore.isAdmin ? adminRoomRows.length : 5 }}</strong>
      </div>
      <div class="overview-item">
        <span class="overview-label">{{
          userStore.isAdmin ? '平均选课率（演示）' : '本学期总学分（演示）'
        }}</span>
        <strong class="overview-value">{{ userStore.isAdmin ? '95%' : '16' }}</strong>
      </div>
    </div>

    <!-- 学生端：个人课表 + 培养/选课说明 -->
    <template v-if="!userStore.isAdmin">
      <p class="lead student">
        以下内容为教学运行演示数据（课表、培养要点、选课说明），后续可按实际教务接口进行替换。
      </p>

      <el-tabs v-model="studentTab" class="academic-tabs">
        <el-tab-pane label="本学期课表" name="schedule">
          <div class="table-card">
            <el-table
              :data="studentScheduleRows"
              border
              stripe
              class="mgmt-table"
              style="width: 100%"
            >
              <el-table-column prop="weekday" label="星期" width="100" align="center" />
              <el-table-column prop="slot" label="节次" width="120" align="center" />
              <el-table-column prop="course" label="课程" min-width="160" />
              <el-table-column prop="teacher" label="教师" width="120" align="center" />
              <el-table-column prop="room" label="教室" width="140" align="center" />
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="培养方案要点" name="plan">
          <div class="note-card">
            <ul class="plan-list">
              <li>
                <strong>通识必修：</strong
                >思想政治、外语、体育等按年级学期修读，具体以学院下达计划为准。
              </li>
              <li>
                <strong>学科基础：</strong>专业导论、核心数学/程序设计类课程为学位课，不及格须重修。
              </li>
              <li><strong>专业选修：</strong>在导师指导下按模块选修，满足最低学分即可毕业审核。</li>
              <li>
                <strong>实践环节：</strong
                >包含实验课、课程设计、实习与毕业设计，按培养方案时间节点完成。
              </li>
            </ul>
          </div>
        </el-tab-pane>

        <el-tab-pane label="选课说明" name="elective">
          <div class="note-card">
            <ul class="plan-list">
              <li>选课开放时间内登录教务系统，在「选课管理」中提交志愿；本演示页仅作流程说明。</li>
              <li>热门课程可能抽签或先到先得，请关注学院通知与容量提示。</li>
              <li>退选须在退选截止前完成，逾期将影响成绩单与学分统计。</li>
              <li>跨年级、跨专业选课需满足先修要求并经学院审批。</li>
            </ul>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>

    <!-- 管理端：开课、教室资源、运行备忘 -->
    <template v-else>
      <p class="lead admin">
        以下内容为本学期开课、教室资源与教学备忘等演示数据，后续可对接排课与教室管理系统。
      </p>

      <el-tabs v-model="adminTab" class="academic-tabs">
        <el-tab-pane label="本学期开课概览" name="courses">
          <div class="table-card">
            <el-table :data="adminCourseRows" border stripe class="mgmt-table" style="width: 100%">
              <el-table-column prop="code" label="课程代码" width="110" align="center" />
              <el-table-column prop="name" label="课程名称" min-width="140" show-overflow-tooltip />
              <el-table-column
                prop="major"
                label="面向专业"
                min-width="120"
                show-overflow-tooltip
              />
              <el-table-column prop="credits" label="学分" width="72" align="center" />
              <el-table-column prop="teacher" label="主讲" width="100" align="center" />
              <el-table-column prop="capacity" label="计划容量" width="100" align="center" />
              <el-table-column prop="enrolled" label="已选人数" width="100" align="center" />
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="教室资源" name="rooms">
          <div class="table-card">
            <el-table :data="adminRoomRows" border stripe class="mgmt-table" style="width: 100%">
              <el-table-column prop="building" label="楼宇" width="120" align="center" />
              <el-table-column prop="roomNo" label="教室" width="100" align="center" />
              <el-table-column prop="type" label="类型" width="100" align="center" />
              <el-table-column prop="seats" label="座位数" width="90" align="center" />
              <el-table-column
                prop="usage"
                label="本周利用率（演示）"
                min-width="160"
                align="center"
              />
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="教学运行备忘" name="ops">
          <div class="note-card">
            <ul class="plan-list">
              <li>
                <strong>排课与调停：</strong
                >节假日前后、大型考试周期间暂停办理教室借用；临时调课须在教学管理系统备案并通知学生。
              </li>
              <li>
                <strong>选课与容量：</strong
                >关注通识课与热门专业课容量预警，必要时协调增开平行班或扩容。
              </li>
              <li>
                <strong>学籍联动：</strong
                >休学、复学学生的选课资格与培养方案版本需在异动生效后同步教务库。
              </li>
              <li>
                <strong>成绩与考务：</strong
                >成绩录入截止、补考安排与监考排班由教务处统一发通知，学院配合落实。
              </li>
            </ul>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
  </el-card>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { computed, ref, watch } from 'vue'

const userStore = useUserStore()

const pageTitle = computed(() => (userStore.isAdmin ? '教学运行一览' : '教务一览'))

const studentTab = ref('schedule')
const adminTab = ref('courses')

watch(
  () => userStore.isAdmin,
  () => {
    studentTab.value = 'schedule'
    adminTab.value = 'courses'
  }
)

const studentScheduleRows = [
  { weekday: '周一', slot: '1–2 节', course: '数据结构', teacher: '王老师', room: '教学楼 A301' },
  { weekday: '周二', slot: '3–4 节', course: '操作系统', teacher: '李老师', room: '教学楼 B205' },
  { weekday: '周三', slot: '1–2 节', course: '大学英语', teacher: '赵老师', room: '外语楼 102' },
  { weekday: '周四', slot: '6–7 节', course: '数据库系统', teacher: '刘老师', room: '实验楼 C410' },
  { weekday: '周五', slot: '3–4 节', course: '软件工程', teacher: '陈老师', room: '教学楼 A208' }
]

const adminCourseRows = [
  {
    code: 'CS201',
    name: '数据结构',
    major: '计算机类',
    credits: 4,
    teacher: '王某',
    capacity: 120,
    enrolled: 118
  },
  {
    code: 'CS305',
    name: '操作系统',
    major: '计算机类',
    credits: 3,
    teacher: '李某',
    capacity: 100,
    enrolled: 97
  },
  {
    code: 'ENG102',
    name: '大学英语（二）',
    major: '全校通识',
    credits: 2,
    teacher: '赵某',
    capacity: 200,
    enrolled: 186
  },
  {
    code: 'SE210',
    name: '软件工程',
    major: '软件工程',
    credits: 3,
    teacher: '陈某',
    capacity: 90,
    enrolled: 88
  },
  {
    code: 'NET220',
    name: '计算机网络',
    major: '网络工程',
    credits: 3,
    teacher: '周某',
    capacity: 80,
    enrolled: 76
  }
]

const adminRoomRows = [
  { building: '教学楼 A', roomNo: 'A301', type: '多媒体', seats: 120, usage: '约 78%' },
  { building: '教学楼 B', roomNo: 'B205', type: '多媒体', seats: 100, usage: '约 65%' },
  { building: '实验楼 C', roomNo: 'C410', type: '机房', seats: 60, usage: '约 92%' },
  { building: '外语楼', roomNo: '102', type: '语音室', seats: 45, usage: '约 55%' },
  { building: '教学楼 A', roomNo: 'A208', type: '普通教室', seats: 80, usage: '约 70%' }
]
</script>

<style scoped>
.overview-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.overview-item {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.overview-label {
  font-size: 12px;
  color: #64748b;
}

.overview-value {
  font-size: 20px;
  line-height: 1.1;
  color: #0f172a;
  font-weight: 700;
}

.lead {
  margin: 0 0 18px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.7;
  max-width: 960px;
  border-radius: 10px;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
}

.lead.student {
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
}

.lead.admin {
  background: linear-gradient(180deg, #f8fafc 0%, #ffffff 100%);
}

.academic-tabs {
  max-width: none;
}

.academic-tabs :deep(.el-tabs__item) {
  font-weight: 600;
}

.academic-tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: #e2e8f0;
}

.table-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.table-card :deep(.el-table th.el-table__cell) {
  background: #f8fafc;
  color: #334155;
  font-weight: 600;
}

.note-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #fff;
  padding: 8px 4px 6px 2px;
}

.plan-list {
  margin: 8px 0 0;
  padding-left: 20px;
  color: #334155;
  line-height: 1.9;
  font-size: 14px;
}

.plan-list li {
  margin-bottom: 10px;
}

@media (max-width: 900px) {
  .overview-strip {
    grid-template-columns: 1fr;
    gap: 10px;
  }
}
</style>
