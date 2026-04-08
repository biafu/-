export type MerchantOrdersWsStatus = 'idle' | 'connecting' | 'connected' | 'reconnecting' | 'disconnected'

export type MerchantOrdersWsPayload = {
  event: string
  merchantId?: number
  orderId?: number
  orderNo?: number
  orderStatus?: string
  message?: string
}

type MerchantOrdersSocket = {
  readyState: number
  onopen: (() => void) | null
  onmessage: ((event: { data: string }) => void) | null
  onclose: (() => void) | null
  onerror: (() => void) | null
  send: (payload: string) => void
  close: () => void
}

type MerchantOrdersWsClientOptions = {
  token: string
  baseUrl?: string
  pageOrigin?: string
  pingIntervalMs?: number
  reconnectBaseDelayMs?: number
  reconnectMaxDelayMs?: number
  webSocketFactory?: (url: string) => MerchantOrdersSocket
  onStatusChange?: (status: MerchantOrdersWsStatus) => void
  onEvent?: (payload: MerchantOrdersWsPayload) => void
  onError?: (error: Error) => void
}

const OPEN_SOCKET_STATE = 1

function toWebSocketProtocol(protocol: string) {
  return protocol === 'https:' ? 'wss:' : 'ws:'
}

export function buildMerchantOrdersWsUrl(
  token: string,
  options: { baseUrl?: string; pageOrigin?: string } = {},
) {
  const source = options.baseUrl || options.pageOrigin
  const target = source ? new URL(source) : new URL(window.location.origin)
  const protocol = toWebSocketProtocol(target.protocol)
  return `${protocol}//${target.host}/ws/orders?token=${encodeURIComponent(token)}`
}

export function parseMerchantOrdersWsPayload(raw: string): MerchantOrdersWsPayload | { event: 'PONG' } | null {
  const text = String(raw || '').trim()
  if (!text) {
    return null
  }

  if (text.toLowerCase() === 'pong') {
    return { event: 'PONG' }
  }

  try {
    const payload = JSON.parse(text) as MerchantOrdersWsPayload
    return typeof payload?.event === 'string' ? payload : null
  } catch {
    return null
  }
}

export class MerchantOrdersWsClient {
  private readonly token: string
  private readonly baseUrl?: string
  private readonly pageOrigin?: string
  private readonly pingIntervalMs: number
  private readonly reconnectBaseDelayMs: number
  private readonly reconnectMaxDelayMs: number
  private readonly webSocketFactory: (url: string) => MerchantOrdersSocket
  private readonly onStatusChange?: (status: MerchantOrdersWsStatus) => void
  private readonly onEvent?: (payload: MerchantOrdersWsPayload) => void
  private readonly onError?: (error: Error) => void
  private socket: MerchantOrdersSocket | null = null
  private pingTimer: ReturnType<typeof setInterval> | null = null
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private reconnectAttempts = 0
  private status: MerchantOrdersWsStatus = 'idle'
  private shouldReconnect = true

  constructor(options: MerchantOrdersWsClientOptions) {
    this.token = options.token
    this.baseUrl = options.baseUrl
    this.pageOrigin = options.pageOrigin
    this.pingIntervalMs = options.pingIntervalMs ?? 20_000
    this.reconnectBaseDelayMs = options.reconnectBaseDelayMs ?? 1_000
    this.reconnectMaxDelayMs = options.reconnectMaxDelayMs ?? 10_000
    this.webSocketFactory = options.webSocketFactory ?? ((url) => new WebSocket(url) as MerchantOrdersSocket)
    this.onStatusChange = options.onStatusChange
    this.onEvent = options.onEvent
    this.onError = options.onError
  }

  connect() {
    this.shouldReconnect = true
    this.clearReconnectTimer()

    if (this.socket) {
      return
    }

    this.openSocket(this.reconnectAttempts > 0)
  }

  disconnect() {
    this.shouldReconnect = false
    this.reconnectAttempts = 0
    this.clearReconnectTimer()
    this.clearPingTimer()

    const activeSocket = this.socket
    this.socket = null
    if (activeSocket) {
      activeSocket.close()
    }

    this.setStatus('disconnected')
  }

  private openSocket(isReconnect: boolean) {
    const url = buildMerchantOrdersWsUrl(this.token, {
      baseUrl: this.baseUrl,
      pageOrigin: this.pageOrigin,
    })
    this.setStatus(isReconnect ? 'reconnecting' : 'connecting')

    const socket = this.webSocketFactory(url)
    this.socket = socket

    socket.onopen = () => {
      if (this.socket !== socket) {
        return
      }
      this.reconnectAttempts = 0
      this.setStatus('connected')
      this.startHeartbeat(socket)
    }

    socket.onmessage = (event) => {
      if (this.socket !== socket) {
        return
      }

      const payload = parseMerchantOrdersWsPayload(event.data)
      if (!payload || payload.event === 'PONG') {
        return
      }

      this.onEvent?.(payload)
    }

    socket.onerror = () => {
      this.onError?.(new Error('Merchant orders WebSocket error'))
    }

    socket.onclose = () => {
      if (this.socket !== socket) {
        return
      }

      this.socket = null
      this.clearPingTimer()

      if (!this.shouldReconnect) {
        this.setStatus('disconnected')
        return
      }

      this.scheduleReconnect()
    }
  }

  private startHeartbeat(socket: MerchantOrdersSocket) {
    this.clearPingTimer()
    this.pingTimer = setInterval(() => {
      if (socket.readyState === OPEN_SOCKET_STATE) {
        socket.send('ping')
      }
    }, this.pingIntervalMs)
  }

  private scheduleReconnect() {
    this.reconnectAttempts += 1
    this.setStatus('reconnecting')
    const delay = Math.min(this.reconnectBaseDelayMs * 2 ** (this.reconnectAttempts - 1), this.reconnectMaxDelayMs)
    this.clearReconnectTimer()
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      if (!this.shouldReconnect || this.socket) {
        return
      }
      this.openSocket(true)
    }, delay)
  }

  private clearPingTimer() {
    if (this.pingTimer) {
      clearInterval(this.pingTimer)
      this.pingTimer = null
    }
  }

  private clearReconnectTimer() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  private setStatus(status: MerchantOrdersWsStatus) {
    if (this.status === status) {
      return
    }

    this.status = status
    this.onStatusChange?.(status)
  }
}
