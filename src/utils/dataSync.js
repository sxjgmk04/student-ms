export function emitDataUpdated() {
  try {
    window.dispatchEvent(new Event('app-data-updated'))
  } catch {}
}
