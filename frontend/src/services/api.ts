
const API_BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ApiError extends Error {
  readonly status: number
  readonly body?: unknown

  constructor(message: string, status: number, body?: unknown) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {},
  token?: string,
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })

  if (!response.ok) {
    const raw = await response.text()
    let body: unknown = raw
    try { body = JSON.parse(raw) } catch { /* response is plain text */ }
    const record = typeof body === 'object' && body !== null ? body as Record<string, unknown> : {}
    const msg = typeof record.message === 'string'
      ? record.message
      : typeof record.detail === 'string'
        ? record.detail
        : raw || `Request failed: ${response.status}`
    throw new ApiError(msg, response.status, body)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}
