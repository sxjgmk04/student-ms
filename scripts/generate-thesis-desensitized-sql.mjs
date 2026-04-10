import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.join(__dirname, '..')
const outPath = path.join(root, 'server-spring/sql/thesis_desensitized_seed.sql')

const BCRYPT_123456 =
  '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG'

const N = 280

const MAJORS = [
  { major: '计算机科学与技术', abbr: '计科', sections: 4 },
  { major: '软件工程', abbr: '软工', sections: 3 },
  { major: '网络工程', abbr: '网工', sections: 2 },
  { major: '信息安全', abbr: '信安', sections: 2 },
  { major: '物联网工程', abbr: '物联', sections: 2 },
  { major: '数据科学与大数据技术', abbr: '数据', sections: 3 },
  { major: '智能科学与技术', abbr: '智科', sections: 2 },
  { major: '人工智能', abbr: '人智', sections: 2 },
  { major: '信息管理与信息系统', abbr: '信管', sections: 3 },
  { major: '工商管理', abbr: '工管', sections: 3 },
  { major: '会计学', abbr: '会计', sections: 3 },
  { major: '金融学', abbr: '金融', sections: 2 },
  { major: '法学', abbr: '法学', sections: 3 },
  { major: '汉语言文学', abbr: '中文', sections: 2 },
  { major: '英语', abbr: '英语', sections: 2 },
  { major: '机械设计制造及其自动化', abbr: '机械', sections: 3 },
  { major: '电气工程及其自动化', abbr: '电气', sections: 3 },
  { major: '土木工程', abbr: '土木', sections: 3 }
]

const COURSES_CS = [
  ['高等数学A', '2024-2025-1'],
  ['线性代数', '2024-2025-1'],
  ['大学英语I', '2024-2025-1'],
  ['程序设计基础', '2024-2025-1'],
  ['离散数学', '2024-2025-1'],
  ['思想道德与法治', '2024-2025-1'],
  ['体育I', '2024-2025-1'],
  ['概率论与数理统计', '2024-2025-2'],
  ['大学英语II', '2024-2025-2'],
  ['数据结构', '2024-2025-2'],
  ['计算机组成原理', '2024-2025-2'],
  ['大学物理B', '2024-2025-2'],
  ['体育II', '2024-2025-2']
]

const COURSES_BIZ = [
  ['高等数学B', '2024-2025-1'],
  ['管理学原理', '2024-2025-1'],
  ['大学英语I', '2024-2025-1'],
  ['微观经济学', '2024-2025-1'],
  ['会计学基础', '2024-2025-1'],
  ['思想道德与法治', '2024-2025-1'],
  ['体育I', '2024-2025-1'],
  ['线性代数', '2024-2025-2'],
  ['大学英语II', '2024-2025-2'],
  ['宏观经济学', '2024-2025-2'],
  ['统计学', '2024-2025-2'],
  ['市场营销学', '2024-2025-2'],
  ['体育II', '2024-2025-2']
]

const COURSES_LIBERAL = [
  ['大学语文', '2024-2025-1'],
  ['大学英语I', '2024-2025-1'],
  ['中国近现代史纲要', '2024-2025-1'],
  ['法理学', '2024-2025-1'],
  ['宪法学', '2024-2025-1'],
  ['思想道德与法治', '2024-2025-1'],
  ['体育I', '2024-2025-1'],
  ['民法总论', '2024-2025-2'],
  ['大学英语II', '2024-2025-2'],
  ['刑法总论', '2024-2025-2'],
  ['行政法与行政诉讼法', '2024-2025-2'],
  ['民事诉讼法', '2024-2025-2'],
  ['体育II', '2024-2025-2']
]

const COURSES_ENGLISH_MAJOR = [
  ['综合英语I', '2024-2025-1'],
  ['英语视听说I', '2024-2025-1'],
  ['大学英语I', '2024-2025-1'],
  ['英语写作基础', '2024-2025-1'],
  ['中国近现代史纲要', '2024-2025-1'],
  ['思想道德与法治', '2024-2025-1'],
  ['体育I', '2024-2025-1'],
  ['综合英语II', '2024-2025-2'],
  ['英语视听说II', '2024-2025-2'],
  ['大学英语II', '2024-2025-2'],
  ['英汉翻译基础', '2024-2025-2'],
  ['英国文学导论', '2024-2025-2'],
  ['体育II', '2024-2025-2']
]

const COURSES_CHINESE = [
  ['现代汉语', '2024-2025-1'],
  ['中国古代文学史', '2024-2025-1'],
  ['大学英语I', '2024-2025-1'],
  ['写作学', '2024-2025-1'],
  ['中国近现代史纲要', '2024-2025-1'],
  ['思想道德与法治', '2024-2025-1'],
  ['体育I', '2024-2025-1'],
  ['中国当代文学史', '2024-2025-2'],
  ['大学英语II', '2024-2025-2'],
  ['文学概论', '2024-2025-2'],
  ['外国文学史', '2024-2025-2'],
  ['语言学概论', '2024-2025-2'],
  ['体育II', '2024-2025-2']
]

const COURSES_ENG = [
  ['高等数学A', '2024-2025-1'],
  ['线性代数', '2024-2025-1'],
  ['大学英语I', '2024-2025-1'],
  ['工程制图', '2024-2025-1'],
  ['大学物理A', '2024-2025-1'],
  ['程序设计基础(C)', '2024-2025-1'],
  ['体育I', '2024-2025-1'],
  ['概率论与数理统计', '2024-2025-2'],
  ['大学英语II', '2024-2025-2'],
  ['材料力学', '2024-2025-2'],
  ['电工电子学', '2024-2025-2'],
  ['机械原理', '2024-2025-2'],
  ['体育II', '2024-2025-2']
]

const COURSES_CIVIL = [
  ['高等数学A', '2024-2025-1'],
  ['线性代数', '2024-2025-1'],
  ['大学英语I', '2024-2025-1'],
  ['工程制图', '2024-2025-1'],
  ['大学物理A', '2024-2025-1'],
  ['土木工程概论', '2024-2025-1'],
  ['体育I', '2024-2025-1'],
  ['概率论与数理统计', '2024-2025-2'],
  ['大学英语II', '2024-2025-2'],
  ['理论力学', '2024-2025-2'],
  ['材料力学', '2024-2025-2'],
  ['土力学基础', '2024-2025-2'],
  ['体育II', '2024-2025-2']
]

function courseListForMajor(majorName) {
  if (
    [
      '计算机科学与技术',
      '软件工程',
      '网络工程',
      '信息安全',
      '物联网工程',
      '数据科学与大数据技术',
      '智能科学与技术',
      '人工智能'
    ].includes(majorName)
  ) {
    return COURSES_CS
  }
  if (
    ['工商管理', '会计学', '金融学', '信息管理与信息系统'].includes(majorName)
  ) {
    return COURSES_BIZ
  }
  if (majorName === '法学') return COURSES_LIBERAL
  if (majorName === '英语') return COURSES_ENGLISH_MAJOR
  if (majorName === '汉语言文学') return COURSES_CHINESE
  if (majorName === '土木工程') return COURSES_CIVIL
  return COURSES_ENG
}

function esc(s) {
  return String(s).replace(/\\/g, '\\\\').replace(/'/g, "''")
}

function realisticScore(seed, courseIdx, courseName) {
  let t = (seed * 1103515245 + 12345 + courseIdx * 7919 + courseName.length * 97) >>> 0
  const u1 = ((t % 100000) + 1) / 100001
  t = (t * 1103515245 + courseIdx) >>> 0
  const u2 = ((t % 100000) + 1) / 100001
  let g = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2)
  let mean = 81
  let spread = 8
  if (courseName.includes('体育')) {
    mean = 84
    spread = 5
  }
  if (courseName.includes('思想道德') || courseName.includes('史纲')) {
    mean = 83
    spread = 6
  }
  let s = Math.round(mean + g * spread)
  if (courseName.includes('英语') && (seed % 7 === 0)) s -= 5
  if (s < 52) s = 52 + (seed % 6)
  if (s > 99) s = 99
  return s
}

function classNameFor(majorIdx, studentIndex) {
  const m = MAJORS[majorIdx]
  const sec = (studentIndex % m.sections) + 1
  return `${m.abbr}2024-${sec}`
}

function majorForStudent(i) {
  return MAJORS[(i - 1) % MAJORS.length]
}

const lines = []

lines.push('SET NAMES utf8mb4;')
lines.push('')
lines.push("DELETE FROM scores WHERE student_id LIKE 'UCI-MAT-%';")
lines.push("DELETE FROM students WHERE student_id LIKE 'UCI-MAT-%';")
lines.push("DELETE FROM users WHERE username LIKE 'uci_mat_%';")
lines.push('')
lines.push("DELETE FROM scores WHERE student_id LIKE '202401%';")
lines.push("DELETE FROM graduation WHERE student_id LIKE '202401%';")
lines.push("DELETE FROM rewards WHERE student_id LIKE '202401%';")
lines.push("DELETE FROM changes WHERE student_id LIKE '202401%';")
lines.push("DELETE FROM students WHERE student_id LIKE '202401%';")
lines.push("DELETE FROM users WHERE username LIKE 'stu202401%';")
lines.push('')

lines.push(
  "SET @tds := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'students' AND column_name = 'data_source');"
)
lines.push(
  "SET @tsql := IF(@tds = 0, 'ALTER TABLE students ADD COLUMN data_source VARCHAR(255) NULL', 'SELECT 1');"
)
lines.push('PREPARE tds_stmt FROM @tsql;')
lines.push('EXECUTE tds_stmt;')
lines.push('DEALLOCATE PREPARE tds_stmt;')
lines.push('')

const studentRows = []
const userRows = []

for (let i = 1; i <= N; i++) {
  const sid = `202401${String(i).padStart(3, '0')}`
  const name = `学生${String(i).padStart(3, '0')}`
  const uname = `stu${sid}`
  const majorIdx = (i - 1) % MAJORS.length
  const { major } = MAJORS[majorIdx]
  const clazz = classNameFor(majorIdx, i)
  const gender = i % 2 === 1 ? '男' : '女'
  const note = '毕业论文演示（虚构脱敏）'

  studentRows.push(
    `('${esc(sid)}','${esc(name)}','${esc(gender)}','${esc(major)}','${esc(clazz)}',2024,'在读','${esc(note)}')`
  )
  userRows.push(
    `('${esc(uname)}','${BCRYPT_123456}','student','${esc(name)}','${esc(sid)}')`
  )
}

lines.push('INSERT INTO students (student_id, name, gender, major, class_name, grade, status, data_source) VALUES')
lines.push(studentRows.join(',\n') + ';')
lines.push('')
lines.push('INSERT INTO users (username, password_hash, role, name, student_id) VALUES')
lines.push(userRows.join(',\n') + ';')
lines.push('')

const scoreRows = []
for (let i = 1; i <= N; i++) {
  const sid = `202401${String(i).padStart(3, '0')}`
  const name = `学生${String(i).padStart(3, '0')}`
  const m = majorForStudent(i)
  const courses = courseListForMajor(m.major)
  let ci = 0
  for (const [courseName, sem] of courses) {
    const sc = realisticScore(i * 9973 + ci * 13, ci, courseName)
    scoreRows.push(
      `('${esc(sid)}','${esc(name)}','${esc(courseName)}',${sc},'${esc(sem)}')`
    )
    ci++
  }
}

function emitScoreBatches(rows, batchSize) {
  const out = []
  for (let b = 0; b < rows.length; b += batchSize) {
    const chunk = rows.slice(b, b + batchSize)
    out.push('INSERT INTO scores (student_id, name, course, score, semester) VALUES')
    out.push(chunk.join(',\n') + ';')
    out.push('')
  }
  return out
}

lines.push(...emitScoreBatches(scoreRows, 150))

const changeSamples = []
for (let k = 0; k < 12; k++) {
  const idx = 1 + k * 23
  if (idx > N) break
  const sid = `202401${String(idx).padStart(3, '0')}`
  const nm = `学生${String(idx).padStart(3, '0')}`
  const types = ['转专业', '休学', '复学', '保留学籍']
  const reasons = [
    '个人发展规划调整',
    '因病申请休学一学期',
    '参军入伍保留学籍',
    '参加交流项目申请保留学籍'
  ]
  changeSamples.push(
    `('${esc(sid)}','${esc(nm)}','${types[k % types.length]}','${esc(reasons[k % reasons.length])}','审核中','2025-0${1 + (k % 3)}-${String(10 + k).padStart(2, '0')}')`
  )
}

const rewardSamples = []
const rewardData = [
  ['奖励', '校级综合奖学金三等'],
  ['奖励', '国家励志奖学金（演示）'],
  ['奖励', '数学建模校赛二等奖'],
  ['惩罚', '晚归通报（演示）'],
  ['奖励', '优秀学生干部'],
  ['奖励', '志愿服务先进个人'],
  ['惩罚', '课程旷课预警（演示）'],
  ['奖励', '程序设计竞赛铜奖']
]
for (let k = 0; k < rewardData.length; k++) {
  const idx = 3 + k * 31
  if (idx > N) break
  const sid = `202401${String(idx).padStart(3, '0')}`
  const nm = `学生${String(idx).padStart(3, '0')}`
  const [tp, title] = rewardData[k]
  rewardSamples.push(
    `('${esc(sid)}','${esc(nm)}','${esc(tp)}','${esc(title)}','2025-0${1 + (k % 4)}-${String(5 + k).padStart(2, '0')}')`
  )
}

const gradSamples = []
for (let k = 0; k < 15; k++) {
  const idx = 5 + k * 17
  if (idx > N) break
  const sid = `202401${String(idx).padStart(3, '0')}`
  const nm = `学生${String(idx).padStart(3, '0')}`
  const mj = majorForStudent(idx).major
  const thesis = 72 + ((idx * 7) % 22)
  const stat = k % 4 === 0 ? '已通过' : '待审核'
  gradSamples.push(
    `('${esc(sid)}','${esc(nm)}','${esc(mj)}','${esc(stat)}',${thesis})`
  )
}

lines.push('INSERT INTO changes (student_id, name, type, reason, status, date) VALUES')
lines.push(changeSamples.join(',\n') + ';')
lines.push('')
lines.push('INSERT INTO rewards (student_id, name, type, title, date) VALUES')
lines.push(rewardSamples.join(',\n') + ';')
lines.push('')
lines.push('INSERT INTO graduation (student_id, name, major, status, thesis_score) VALUES')
lines.push(gradSamples.join(',\n') + ';')

fs.writeFileSync(outPath, lines.join('\n'), 'utf8')
console.log('已写入:', outPath)
console.log(
  `学生 ${N} 人，约 ${scoreRows.length} 条成绩，登录 stu202401001 ~ stu202401${String(N).padStart(3, '0')}，密码 123456`
)
console.log(`专业种类 ${MAJORS.length} 个`)
