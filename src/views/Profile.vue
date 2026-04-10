<template>
  <el-card class="page-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span>个人中心</span>
      </div>
    </template>

    <div class="profile-grid">
      <section class="panel-card profile-section">
        <div class="panel-head">
          <h3 class="section-title">基本资料</h3>
          <div class="identity-chips">
            <el-tag size="small" effect="plain">{{ roleLabel }}</el-tag>
            <el-tag size="small" type="info" effect="plain">{{ displayName }}</el-tag>
          </div>
        </div>
        <p class="section-hint">
          点击头像可更换（JPG / PNG / GIF / WebP，最大 2MB）；也可在下方填写公网可访问的
          http(s) 图片链接。邮箱与手机用于联系与找回（演示环境可随意填写）。
        </p>
        <el-form :model="profileForm" label-width="100px" class="profile-form">
          <el-form-item label="头像">
            <div class="avatar-row">
              <button
                type="button"
                class="avatar-hit"
                :disabled="uploadingAvatar"
                aria-label="选择图片更换头像"
                @click="triggerPick"
              >
                <el-avatar :size="92" :src="avatarDisplaySrc" />
                <span class="avatar-hit-label">{{
                  uploadingAvatar ? '上传中…' : '点击更换头像'
                }}</span>
              </button>
              <input
                ref="fileInputRef"
                type="file"
                class="sr-only"
                accept="image/jpeg,image/png,image/gif,image/webp,.jpg,.jpeg,.png,.gif,.webp"
                @change="onAvatarFile"
              />
            </div>
          </el-form-item>
          <el-form-item label="头像链接">
            <el-input
              v-model="profileForm.avatarUrl"
              placeholder="https://... 或上传后自动填充"
              clearable
            />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" placeholder="选填" clearable />
          </el-form-item>
          <el-form-item label="手机">
            <el-input v-model="profileForm.phone" placeholder="选填" clearable />
          </el-form-item>
          <el-form-item class="action-row">
            <el-button type="primary" :loading="savingProfile" @click="submitProfile"
              >保存资料</el-button
            >
          </el-form-item>
        </el-form>
      </section>

      <section class="panel-card profile-section">
        <div class="panel-head">
          <h3 class="section-title">修改密码</h3>
          <span class="password-tip">建议定期更换密码，保障账号安全</span>
        </div>
        <el-form
          ref="pwdFormRef"
          :model="pwdForm"
          :rules="pwdRules"
          label-width="100px"
          class="profile-form"
        >
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input
              v-model="pwdForm.oldPassword"
              type="password"
              show-password
              autocomplete="current-password"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="pwdForm.newPassword"
              type="password"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="pwdForm.confirmPassword"
              type="password"
              show-password
              autocomplete="new-password"
            />
          </el-form-item>
          <el-form-item class="action-row">
            <el-button type="primary" :loading="savingPwd" @click="submitPassword"
              >更新密码</el-button
            >
          </el-form-item>
        </el-form>
      </section>
    </div>
  </el-card>
</template>

<script setup>
import { changePassword, patchProfile, uploadAvatar } from '@/api/studentMs.js'
import { resolveAvatarUrl } from '@/utils/avatarUrl.js'
import { filterFormXss } from '@/utils/security.js'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'

const userStore = useUserStore()
const savingProfile = ref(false)
const savingPwd = ref(false)
const uploadingAvatar = ref(false)
const pwdFormRef = ref(null)
const fileInputRef = ref(null)
const localObjectUrl = ref(null)

const defaultAdminAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'
const defaultStudentAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const profileForm = reactive({
  email: '',
  phone: '',
  avatarUrl: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 64, message: '新密码长度 6～64 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_r, v, cb) => {
        if (v !== pwdForm.newPassword) cb(new Error('两次输入的新密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

const avatarDisplaySrc = computed(() => {
  if (localObjectUrl.value) return localObjectUrl.value
  const u = profileForm.avatarUrl?.trim()
  if (u) return resolveAvatarUrl(u)
  if (userStore.userInfo?.avatarUrl) return resolveAvatarUrl(userStore.userInfo.avatarUrl)
  return userStore.isAdmin ? defaultAdminAvatar : defaultStudentAvatar
})

const displayName = computed(
  () => userStore.userInfo?.name || userStore.userInfo?.username || '当前用户'
)
const roleLabel = computed(() => (userStore.isAdmin ? '管理员账号' : '学生账号'))

function clearLocalPreview() {
  if (localObjectUrl.value) {
    URL.revokeObjectURL(localObjectUrl.value)
    localObjectUrl.value = null
  }
}

onMounted(() => {
  const u = userStore.userInfo
  if (!u) return
  profileForm.email = u.email || ''
  profileForm.phone = u.phone || ''
  profileForm.avatarUrl = u.avatarUrl || ''
})

onUnmounted(() => {
  clearLocalPreview()
})

const triggerPick = () => {
  if (uploadingAvatar.value) return
  fileInputRef.value?.click()
}

const onAvatarFile = async (e) => {
  const input = e.target
  const file = input.files?.[0]
  input.value = ''
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.error('图片不能超过 2MB')
    return
  }

  clearLocalPreview()
  localObjectUrl.value = URL.createObjectURL(file)

  uploadingAvatar.value = true
  try {
    const me = await uploadAvatar(file)
    userStore.syncMe(me)
    profileForm.avatarUrl = me.avatarUrl || ''
    clearLocalPreview()
    ElMessage.success('头像已更新')
  } catch (err) {
    clearLocalPreview()
    ElMessage.error(typeof err === 'string' ? err : '上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

const submitProfile = async () => {
  savingProfile.value = true
  try {
    const ft = filterFormXss({
      email: profileForm.email,
      phone: profileForm.phone
    })
    const body = {
      email: ft.email,
      phone: ft.phone,
      avatarUrl: (profileForm.avatarUrl || '').trim()
    }
    const me = await patchProfile(body)
    userStore.syncMe(me)
    ElMessage.success('资料已保存')
  } catch (e) {
    ElMessage.error(typeof e === 'string' ? e : '保存失败')
  } finally {
    savingProfile.value = false
  }
}

const submitPassword = async () => {
  await pwdFormRef.value?.validate(async (valid) => {
    if (!valid) return
    savingPwd.value = true
    try {
      await changePassword({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
      pwdForm.confirmPassword = ''
      pwdFormRef.value?.resetFields()
      ElMessage.success('密码已更新')
    } catch (e) {
      ElMessage.error(typeof e === 'string' ? e : '修改失败')
    } finally {
      savingPwd.value = false
    }
  })
}
</script>

<style scoped>
.profile-grid {
  width: 100%;
  max-width: none;
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  align-items: stretch;
  gap: 20px;
}

.panel-card {
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  padding: 18px 18px 8px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}

.identity-chips {
  display: inline-flex;
  gap: 6px;
  flex-wrap: wrap;
}

.section-title {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: 0.2px;
}

.section-hint {
  margin: 0 0 18px;
  font-size: 13px;
  color: #6b7280;
  line-height: 1.7;
}

.password-tip {
  font-size: 12px;
  color: #94a3b8;
}

.profile-form {
  margin-top: 8px;
  flex: 1;
}

.profile-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.profile-form :deep(.el-input__wrapper) {
  min-height: 42px;
  border-radius: 10px;
}

.profile-form :deep(.el-textarea__inner) {
  border-radius: 10px;
}

.avatar-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 18px;
}

.avatar-hit {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border: 1px dashed #bfdbfe;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  cursor: pointer;
  border-radius: 12px;
  font: inherit;
  color: #64748b;
  min-width: 132px;
  transition: all 0.16s ease;
}

.avatar-hit:hover {
  border-color: #60a5fa;
  box-shadow: 0 10px 26px rgba(37, 99, 235, 0.12);
  transform: translateY(-1px);
}

.avatar-hit:disabled {
  cursor: not-allowed;
  opacity: 0.75;
}

.avatar-hit:focus-visible {
  outline: 2px solid #2563eb;
  outline-offset: 4px;
}

.avatar-hit-label {
  font-size: 12px;
  font-weight: 600;
}

.action-row :deep(.el-form-item__content) {
  justify-content: center;
}

.action-row :deep(.el-button) {
  min-width: 126px;
  border-radius: 11px;
  height: 40px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
    gap: 14px;
  }

  .panel-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .panel-card {
    padding: 14px 12px 2px;
  }
}
</style>
