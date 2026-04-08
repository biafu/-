const ABSOLUTE_URL_PATTERN = /^(?:[a-z]+:)?\/\//i
const DEFAULT_ORIGIN = 'http://localhost'

const getCurrentOrigin = () =>
  typeof window !== 'undefined' && window.location?.origin ? window.location.origin : DEFAULT_ORIGIN

export function resolveAssetUrl(rawUrl?: string, apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? '') {
  const trimmedUrl = rawUrl?.trim() ?? ''
  if (!trimmedUrl) {
    return ''
  }

  if (
    ABSOLUTE_URL_PATTERN.test(trimmedUrl) ||
    trimmedUrl.startsWith('data:') ||
    trimmedUrl.startsWith('blob:')
  ) {
    return trimmedUrl
  }

  if (trimmedUrl.startsWith('/')) {
    if (!apiBaseUrl) {
      return trimmedUrl
    }

    const baseUrl = new URL(apiBaseUrl, getCurrentOrigin())
    return `${baseUrl.origin}${trimmedUrl}`
  }

  if (!apiBaseUrl) {
    return trimmedUrl
  }

  return new URL(trimmedUrl, new URL(apiBaseUrl, getCurrentOrigin())).toString()
}
