<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-base-image"></div>
      <div class="bg-scrim" aria-hidden="true"></div>
      <div class="bg-text-image"></div>
    </div>

    <div class="login-card-wrapper">
      <el-card class="login-card" shadow="hover" :body-style="{ padding: '0' }">
        <div class="login-content">
          <div class="login-header">
            <div class="login-icon">
              <el-icon :size="36" color="#2563eb">
                <School />
              </el-icon>
            </div>
            <h1 class="header-title">欢迎登录</h1>
            <p class="header-subtitle">学生学籍管理系统</p>
          </div>

          <div class="account-tip">
            <div class="account-tip-title">演示账号</div>
            <div class="account-tip-text">
              管理员：<code>admin / 123456</code
              >；内置学生：<code>student1</code>～<code>student8</code>；毕设脱敏批次：
              <code>stu202401001</code>～<code>stu202401280</code>，密码均为
              <code>123456</code>（虚构数据，非真实学籍）
            </div>
            <div class="account-tip-text account-tip-secondary">
              安全提示：同一用户名连续输错密码 5 次将被<strong>服务端</strong>锁定 10
              分钟，换设备或清理缓存后仍有效。
            </div>
          </div>

          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                clearable
                class="login-input"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                clearable
                @keyup.enter="handleLogin"
                class="login-input"
              />
            </el-form-item>

            <div v-if="errorText" :class="errorIsLock ? 'lock-tip' : 'error-tip'">
              <el-icon>
                <Warning />
              </el-icon>
              <span>{{ errorText }}</span>
            </div>

            <div class="forgot-password">
              <span @click="goToForgotPassword">忘记密码？</span>
            </div>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                @click="handleLogin"
                class="login-btn"
                :loading="loading"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <div class="agreement">
              登录即表示您同意
              <span class="link" @click="showAgreement('user')">《用户协议》</span>
              和
              <span class="link" @click="showAgreement('privacy')">《隐私政策》</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { Lock, School, User, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { isNavigationFailure } from 'vue-router'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)
const errorText = ref('')

const loginForm = ref({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const passwordResetUrl = 'https://password-reset.example.com'

const errorIsLock = computed(
  () => typeof errorText.value === 'string' && errorText.value.includes('登录尝试过多')
)

const goToForgotPassword = () => {
  window.open(passwordResetUrl, '_blank', 'noopener,noreferrer')
}

const showAgreement = (type) => {
  const title = type === 'user' ? '用户协议' : '隐私政策'
  const userAgreementHtml = `
<div class="agreement-content">
  <div class="agreement-lead">请仔细阅读以下内容，使用系统即表示您同意本协议。</div>
  <div class="agreement-section">
    <div class="agreement-section-title">一、总则</div>
    <p>1. 欢迎使用学生学籍管理系统。</p>
    <p>2. 使用系统即视为同意本协议条款。</p>
  </div>
  <div class="agreement-section">
    <div class="agreement-section-title">二、账号管理</div>
    <p>1. 请妥善保管您的账号和密码。</p>
    <p>2. 严禁将账号转借他人使用。</p>
    <p>3. 如发现账号异常，请立即联系管理员。</p>
  </div>
  <div class="agreement-section">
    <div class="agreement-section-title">三、使用规范</div>
    <p>1. 请确保填写信息真实有效。</p>
    <p>2. 请在权限范围内操作，不得越权访问。</p>
    <p>3. 严禁利用系统从事违法违规活动。</p>
  </div>
  <div class="agreement-section">
    <div class="agreement-section-title">四、免责声明</div>
    <p>因不可抗力、网络故障等原因导致服务中断时，系统不承担责任。</p>
  </div>
</div>
  `
  const privacyAgreementHtml = `
<div class="agreement-content">
  <div class="agreement-lead">请仔细阅读以下内容，了解系统如何处理您的信息。</div>
  <div class="agreement-section">
    <div class="agreement-section-title">一、信息收集</div>
    <p>我们仅收集学籍管理所需的必要信息（如姓名、学号、专业等）。</p>
  </div>
  <div class="agreement-section">
    <div class="agreement-section-title">二、信息使用</div>
    <p>收集的信息仅用于学校学籍管理工作，不用于商业用途。</p>
  </div>
  <div class="agreement-section">
    <div class="agreement-section-title">三、信息保护</div>
    <p>我们采用加密与权限控制机制保护您的信息安全。</p>
  </div>
  <div class="agreement-section">
    <div class="agreement-section-title">四、信息共享</div>
    <p>未经您同意，不会向第三方共享信息，法律法规要求的情况除外。</p>
  </div>
  <div class="agreement-section">
    <div class="agreement-section-title">五、您的权利</div>
    <p>您有权查询和更正个人信息，如有疑问请联系管理员。</p>
  </div>
</div>
  `
  const content = type === 'user' ? userAgreementHtml : privacyAgreementHtml

  ElMessageBox.alert(content, title, {
    confirmButtonText: '我知道了',
    customClass: 'simple-agreement-dialog',
    showClose: false,
    width: '480px',
    closeOnClickModal: true,
    closeOnPressEscape: true,
    dangerouslyUseHTMLString: true
  })
}

const handleLogin = async () => {
  loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    errorText.value = ''
    loading.value = true
    try {
      await userStore.loginWithPassword(loginForm.value.username, loginForm.value.password)
      errorText.value = ''
      ElMessage.success('登录成功！')
      try {
        await router.push({ path: '/welcome' })
      } catch (navErr) {
        if (!isNavigationFailure(navErr)) throw navErr
      }
    } catch (msg) {
      const text = typeof msg === 'string' ? msg : '登录失败'
      errorText.value = text
      ElMessage.error(text)
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  position: relative;
  overflow: hidden;
  padding-right: 15%;
  background: var(--bg-color);
}

.login-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.bg-base-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url(../assets/images/Login-bg.png);
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  z-index: 1;
}

.bg-scrim {
  position: absolute;
  inset: 0;
  z-index: 2;
  pointer-events: none;
  background: linear-gradient(
    105deg,
    rgba(15, 23, 42, 0.5) 0%,
    rgba(15, 23, 42, 0.18) 42%,
    rgba(15, 23, 42, 0.06) 100%
  );
}

.bg-text-image {
  position: absolute;
  top: 40%;
  left: 25%;
  width: 504px;
  height: 100px;
  background-image: url(../assets/images/Login-bg-text.png);
  background-size: contain;
  background-position: left center;
  background-repeat: no-repeat;
  z-index: 3;
}

.login-card-wrapper {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.login-card {
  width: 400px;
  max-width: calc(100vw - 32px);
  border: 1px solid rgba(255, 255, 255, 0.65);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px) saturate(1.2);
  box-shadow:
    0 0 0 1px rgba(15, 23, 42, 0.04),
    0 20px 50px -12px rgba(15, 23, 42, 0.18);
}

.login-content {
  padding: 32px 28px 28px;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-icon {
  width: 52px;
  height: 52px;
  background: linear-gradient(160deg, #eff6ff 0%, #dbeafe 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 14px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.12);
}

.header-title {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.02em;
  color: #0f172a;
}

.header-subtitle {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  letter-spacing: 0.06em;
}

.login-form {
  margin-bottom: 20px;
}

.account-tip {
  margin-bottom: 20px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.account-tip-title {
  font-size: 11px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 6px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.account-tip-text {
  font-size: 12px;
  color: #64748b;
  line-height: 1.65;
}

.account-tip-secondary {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #e2e8f0;
}

.login-input {
  --el-input-height: 46px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 9px;
  box-shadow: 0 0 0 1px #dbe3ef inset;
  transition: all 0.2s ease;
  background-color: rgba(255, 255, 255, 0.96);
}

.login-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #93c5fd inset;
}

.login-input :deep(.el-input__wrapper.is-focus) {
  box-shadow:
    0 0 0 1px #2563eb inset,
    0 0 0 4px rgba(37, 99, 235, 0.14);
}

.lock-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #f53f3f;
  font-size: 13px;
  margin-bottom: 18px;
  padding: 10px 14px;
  background: #fff1f0;
  border-radius: 8px;
  border: 1px solid #ffccc7;
}

.error-tip {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  color: #d92d20;
  font-size: 13px;
  margin-bottom: 18px;
  padding: 10px 12px;
  background: #fff7ed;
  border-radius: 8px;
  border: 1px solid #fed7aa;
  line-height: 1.5;
}

.forgot-password {
  text-align: right;
  margin-bottom: 18px;
}

.forgot-password span {
  color: #2563eb;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.forgot-password span:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.12em;
  border-radius: 10px;
  background: #2563eb;
  border: none;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.35);
  transition:
    background 0.2s ease,
    box-shadow 0.2s ease;
}

.login-btn:hover {
  transform: none;
  background: #1d4ed8;
  box-shadow: 0 4px 16px rgba(37, 99, 235, 0.4);
}

.login-btn:active {
  transform: none;
  opacity: 0.96;
}

.login-footer {
  text-align: center;
  padding-top: 8px;
}

.agreement {
  font-size: 12px;
  color: #64748b;
}

.agreement .link {
  color: #2563eb;
  cursor: pointer;
  transition: all 0.15s ease;
}

.agreement .link:hover {
  color: #1d4ed8;
  text-decoration: underline;
}

@media (max-width: 1024px) {
  .login-container {
    justify-content: center;
    padding-right: 0;
  }

  .bg-text-image {
    left: 50%;
    top: 20%;
    width: min(72vw, 420px);
    transform: translateX(-50%);
  }
}

@media (max-width: 768px) {
  .login-container {
    padding: 16px;
  }

  .bg-text-image {
    display: none;
  }

  .login-card {
    width: min(100%, 360px);
  }

  .login-content {
    padding: 26px 20px 20px;
  }
}
</style>

<style>
.simple-agreement-dialog {
  border-radius: 12px;
  overflow: hidden;
  padding: 0;
}

.simple-agreement-dialog .el-message-box__header {
  padding: 18px 24px;
  margin: 0;
  text-align: center;
  border-bottom: 1px solid #e5e6eb;
}

.simple-agreement-dialog .el-message-box__title {
  font-size: 20px;
  font-weight: 700;
  color: #0f172a;
  justify-content: center;
}

.simple-agreement-dialog .el-message-box__content {
  padding: 0;
  margin: 0;
  color: #4e5969;
  font-size: 14px;
  max-height: 380px;
  overflow-y: auto;
  border-top: 1px solid #e5e6eb;
  border-bottom: 1px solid #e5e6eb;
}

.simple-agreement-dialog .el-message-box__btns {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 84px;
  padding: 0;
  margin: 0;
}

.simple-agreement-dialog .el-button--primary {
  background-color: #1677ff;
  border-color: #1677ff;
  border-radius: 8px;
  padding: 10px 28px;
  font-weight: 500;
}

.simple-agreement-dialog .el-button--primary:hover {
  background-color: #4096ff;
  border-color: #4096ff;
}

.simple-agreement-dialog .agreement-content {
  padding: 22px 24px;
}

.simple-agreement-dialog .agreement-lead {
  margin-bottom: 14px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.7;
}

.simple-agreement-dialog .agreement-section {
  margin-bottom: 14px;
}

.simple-agreement-dialog .agreement-section:last-child {
  margin-bottom: 0;
}

.simple-agreement-dialog .agreement-section-title {
  margin-bottom: 6px;
  color: #0f172a;
  font-size: 16px;
  font-weight: 700;
}

.simple-agreement-dialog .agreement-section p {
  margin: 0 0 4px;
  color: #334155;
  line-height: 1.8;
}
</style>
