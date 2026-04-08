import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  MerchantOrdersWsClient,
  buildMerchantOrdersWsUrl,
  parseMerchantOrdersWsPayload,
} from '@/utils/merchant-order-ws'

class FakeSocket {
  static readonly OPEN = 1
  static readonly CLOSED = 3

  readyState = 0
  sent: string[] = []
  onopen: (() => void) | null = null
  onmessage: ((event: { data: string }) => void) | null = null
  onclose: (() => void) | null = null
  onerror: (() => void) | null = null

  send(payload: string) {
    this.sent.push(payload)
  }

  close() {
    this.readyState = FakeSocket.CLOSED
    this.onclose?.()
  }

  open() {
    this.readyState = FakeSocket.OPEN
    this.onopen?.()
  }

  message(data: string) {
    this.onmessage?.({ data })
  }
}

describe('merchant order websocket helpers', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('builds a websocket url from either api base url or current page origin', () => {
    expect(buildMerchantOrdersWsUrl('demo-token', { baseUrl: 'http://localhost:8080' })).toBe(
      'ws://localhost:8080/ws/orders?token=demo-token',
    )
    expect(buildMerchantOrdersWsUrl('demo-token', { baseUrl: 'https://example.com/api' })).toBe(
      'wss://example.com/ws/orders?token=demo-token',
    )
    expect(buildMerchantOrdersWsUrl('demo-token', { pageOrigin: 'https://web.example.com' })).toBe(
      'wss://web.example.com/ws/orders?token=demo-token',
    )
  })

  it('parses json payloads and filters the pong heartbeat reply', () => {
    expect(parseMerchantOrdersWsPayload('pong')).toEqual({ event: 'PONG' })
    expect(
      parseMerchantOrdersWsPayload(
        JSON.stringify({
          event: 'ORDER_CREATED',
          orderId: 99,
          orderNo: 202604080001,
          message: 'new order',
        }),
      ),
    ).toEqual({
      event: 'ORDER_CREATED',
      orderId: 99,
      orderNo: 202604080001,
      message: 'new order',
    })
    expect(parseMerchantOrdersWsPayload('not-json')).toBeNull()
  })

  it('connects, sends heartbeat pings, and surfaces parsed events', () => {
    vi.useFakeTimers()
    const sockets: FakeSocket[] = []
    const statuses: string[] = []
    const events: Array<{ event: string; orderId?: number }> = []
    const client = new MerchantOrdersWsClient({
      token: 'merchant-token',
      baseUrl: 'http://localhost:8080',
      pingIntervalMs: 5_000,
      webSocketFactory: (url) => {
        expect(url).toBe('ws://localhost:8080/ws/orders?token=merchant-token')
        const socket = new FakeSocket()
        sockets.push(socket)
        return socket
      },
      onStatusChange: (status) => statuses.push(status),
      onEvent: (payload) => events.push(payload),
    })

    client.connect()
    expect(statuses).toEqual(['connecting'])

    sockets[0]!.open()
    expect(statuses).toEqual(['connecting', 'connected'])

    vi.advanceTimersByTime(5_000)
    expect(sockets[0]!.sent).toEqual(['ping'])

    sockets[0]!.message(JSON.stringify({ event: 'ORDER_CREATED', orderId: 12 }))
    expect(events).toEqual([{ event: 'ORDER_CREATED', orderId: 12 }])
  })

  it('reconnects after an unexpected close and stops reconnecting after manual disconnect', () => {
    vi.useFakeTimers()
    const sockets: FakeSocket[] = []
    const statuses: string[] = []
    const client = new MerchantOrdersWsClient({
      token: 'merchant-token',
      baseUrl: 'http://localhost:8080',
      reconnectBaseDelayMs: 1_000,
      webSocketFactory: () => {
        const socket = new FakeSocket()
        sockets.push(socket)
        return socket
      },
      onStatusChange: (status) => statuses.push(status),
    })

    client.connect()
    sockets[0]!.open()
    sockets[0]!.close()
    expect(statuses).toEqual(['connecting', 'connected', 'reconnecting'])

    vi.advanceTimersByTime(1_000)
    expect(sockets).toHaveLength(2)

    client.disconnect()
    expect(statuses.at(-1)).toBe('disconnected')

    sockets[1]!.close()
    vi.advanceTimersByTime(10_000)
    expect(sockets).toHaveLength(2)
  })
})
