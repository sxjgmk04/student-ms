import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.join(__dirname, '..')
const csvPath = path.join(root, 'server-spring/data/open/uci_extract/student-mat.csv')
const outPath = path.join(root, 'server-spring/sql/import_uci_student_mat.sql')

const BCRYPT_123456 =
  '$2a$10$AJR8E3bXWqF3XtpdaHrPPOPug5p/lrGFwv3SrCmMZ2ftns0aByFgG'

function parseSemicolonLine(line) {
  const parts = []
  let cur = ''
  let inQ = false
  for (let i = 0; i < line.length; i++) {
    const c = line[i]
    if (c === '"') {
      inQ = !inQ
      continue
    }
    if (c === ';' && !inQ) {
      parts.push(cur)
      cur = ''
      continue
    }
    cur += c
  }
  parts.push(cur)
  return parts.map((p) => p.replace(/^"|"$/g, ''))
}

function esc(s) {
  return String(s).replace(/\\/g, '\\\\').replace(/'/g, "''")
}

function chunk(arr, size) {
  const out = []
  for (let i = 0; i < arr.length; i += size) out.push(arr.slice(i, i + size))
  return out
}

if (!fs.existsSync(csvPath)) {
  console.error('缺少 CSV：', csvPath)
  console.error('请先解压 UCI student.zip 到 data/open/uci_extract/')
  process.exit(1)
}

const text = fs.readFileSync(csvPath, 'utf8')
const lines = text.trim().split(/\r?\n/)
const header = parseSemicolonLine(lines[0])
const col = Object.fromEntries(header.map((h, i) => [h, i]))

const studentValues = []
const userValues = []
const scoreValues = []

for (let r = 1; r < lines.length; r++) {
  const parts = parseSemicolonLine(lines[r])
  if (parts.length < 33) continue

  const school = parts[col.school] || ''
  const sex = parts[col.sex] || ''
  const g1 = parseInt(parts[col.G1] || '0', 10) || 0
  const g2 = parseInt(parts[col.G2] || '0', 10) || 0
  const g3 = parseInt(parts[col.G3] || '0', 10) || 0

  const num = String(r).padStart(4, '0')
  const sid = `UCI-MAT-${num}`
  const uname = `uci_mat_${num}`
  const gender = sex === 'F' ? '女' : '男'
  const name = `学生${num}`
  const major = '数学（UCI 中学课程）'
  const className = `${school}校`
  const grade = 2021
  const status = '在读'
  const dataSource = 'UCI: Student Performance — student-mat.csv'

  studentValues.push(
    `('${esc(sid)}','${esc(name)}','${esc(gender)}','${esc(major)}','${esc(className)}',${grade},'${esc(status)}','${esc(dataSource)}')`
  )

  userValues.push(
    `('${esc(uname)}','${BCRYPT_123456}','student','${esc(name)}','${esc(sid)}')`
  )

  const s1 = Math.min(100, Math.round(g1 * 5))
  const s2 = Math.min(100, Math.round(g2 * 5))
  const s3 = Math.min(100, Math.round(g3 * 5))
  scoreValues.push(
    `('${esc(sid)}','${esc(name)}','数学平时成绩G1',${s1},'UCI-20分制')`,
    `('${esc(sid)}','${esc(name)}','数学平时成绩G2',${s2},'UCI-20分制')`,
    `('${esc(sid)}','${esc(name)}','数学结业成绩G3',${s3},'UCI-20分制')`
  )
}

const linesOut = []
linesOut.push('SET NAMES utf8mb4;')
linesOut.push(
  "SET @uci_ds := (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'students' AND column_name = 'data_source');"
)
linesOut.push(
  "SET @uci_sql := IF(@uci_ds = 0, 'ALTER TABLE students ADD COLUMN data_source VARCHAR(255) NULL', 'SELECT 1');"
)
linesOut.push('PREPARE uci_stmt FROM @uci_sql;')
linesOut.push('EXECUTE uci_stmt;')
linesOut.push('DEALLOCATE PREPARE uci_stmt;')
linesOut.push('')
linesOut.push("DELETE FROM scores WHERE student_id LIKE 'UCI-MAT-%';")
linesOut.push("DELETE FROM students WHERE student_id LIKE 'UCI-MAT-%';")
linesOut.push("DELETE FROM users WHERE username LIKE 'uci_mat_%';")
linesOut.push('')

function emitBatches(table, columns, rows, batchSize) {
  for (const batch of chunk(rows, batchSize)) {
    linesOut.push(`INSERT INTO ${table} (${columns}) VALUES`)
    linesOut.push(batch.join(',\n') + ';')
    linesOut.push('')
  }
}

emitBatches(
  'students',
  'student_id, name, gender, major, class_name, grade, status, data_source',
  studentValues,
  80
)
emitBatches(
  'users',
  'username, password_hash, role, name, student_id',
  userValues,
  80
)
emitBatches(
  'scores',
  'student_id, name, course, score, semester',
  scoreValues,
  120
)

fs.writeFileSync(outPath, linesOut.join('\n'), 'utf8')
console.log('已写入:', outPath)
console.log('学生:', studentValues.length, '用户:', userValues.length, '成绩行:', scoreValues.length)
