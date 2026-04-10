/**
 * 将用户头像地址转为浏览器可直接请求的 URL（外链、data URL、本地上传路径 /uploads/...）。
 */
export function resolveAvatarUrl(url) {
  if (url == null) return ''
  const u = String(url).trim()
  if (!u) return ''
  if (u.startsWith('http://') || u.startsWith('https://') || u.startsWith('data:')) return u
  if (u.startsWith('/')) return u
  return u
}
