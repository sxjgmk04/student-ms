/**
 * 从路由 meta 解析展示标题（支持管理员/学生不同文案）。
 */
export function resolveRouteTitle(meta, { isAdmin }) {
  if (!meta) return '页面'
  if (meta.titles) {
    const t = meta.titles
    if (isAdmin && t.admin) return t.admin
    if (!isAdmin && t.student) return t.student
    if (t.default) return t.default
  }
  if (meta.title) return meta.title
  return '页面'
}
