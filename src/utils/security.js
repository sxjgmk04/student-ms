export const xssFilter = (str) => {
  if (!str) return str
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
    .replace(/javascript:/g, '')
    .replace(/onerror=/g, '')
    .replace(/onclick=/g, '')
}

export const filterFormXss = (formObj) => {
  const filtered = {}
  for (const key in formObj) {
    if (typeof formObj[key] === 'string') {
      filtered[key] = xssFilter(formObj[key])
    } else {
      filtered[key] = formObj[key]
    }
  }
  return filtered
}
