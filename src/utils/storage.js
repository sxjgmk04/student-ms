export const readJsonFromLocalStorage = (key, fallback) => {
  try {
    const raw = localStorage.getItem(key)
    if (raw === null || raw === undefined || raw === '') return fallback
    return JSON.parse(raw)
  } catch (e) {
    console.warn(`[storage] readJsonFromLocalStorage failed: ${key}`, e)
    return fallback
  }
}

export const readJsonArrayFromLocalStorage = (key, fallback) => {
  const value = readJsonFromLocalStorage(key, null)
  return Array.isArray(value) ? value : fallback
}

export const readJsonObjectFromLocalStorage = (key, fallback) => {
  const value = readJsonFromLocalStorage(key, null)
  if (value && typeof value === 'object' && !Array.isArray(value)) return value
  return fallback
}

export const writeJsonToLocalStorage = (key, value) => {
  try {
    localStorage.setItem(key, JSON.stringify(value))
    return true
  } catch (e) {
    console.warn(`[storage] writeJsonToLocalStorage failed: ${key}`, e)
    return false
  }
}

export const removeLocalStorageItem = (key) => {
  try {
    localStorage.removeItem(key)
    return true
  } catch (e) {
    console.warn(`[storage] removeLocalStorageItem failed: ${key}`, e)
    return false
  }
}
