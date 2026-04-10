function utf8ToBase64(str) {
  const bytes = new TextEncoder().encode(str)
  let binary = ''
  for (let i = 0; i < bytes.length; i++) binary += String.fromCharCode(bytes[i])
  return btoa(binary)
}

function base64ToUtf8(b64) {
  const binary = atob(b64)
  const bytes = new Uint8Array(binary.length)
  for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
  return new TextDecoder().decode(bytes)
}

export function encodeStoredJson(value) {
  return utf8ToBase64(JSON.stringify(value))
}

export function decodeStoredJson(b64) {
  try {
    return JSON.parse(base64ToUtf8(b64))
  } catch {
    return JSON.parse(atob(b64))
  }
}
