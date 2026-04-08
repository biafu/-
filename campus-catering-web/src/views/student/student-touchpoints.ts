import type { RouteLocationRaw } from 'vue-router'
import type { StudentMessageResponse, StoreSimpleResponse } from '@/api/student'

export type StudentHomeStore = StoreSimpleResponse & {
  score: number
  deliveryTime: string
  distance: string
  tags: string[]
  monthlySales: number
  avgPrice: number
  cover: string
}

export type StudentStoreSortMode = 'default' | 'distance' | 'minOrderAmount' | 'deliveryFee'

export type StudentMessageAction =
  | {
      type: 'order'
      to: RouteLocationRaw
    }
  | {
      type: 'store'
      to: RouteLocationRaw
    }

const CATEGORY_RULES: Array<{ category: string; keywords: string[] }> = [
  { category: '轻食', keywords: ['轻食', '沙拉', '低脂', '能量碗'] },
  { category: '饮品', keywords: ['奶茶', '咖啡', '果茶', '饮品', '茶饮'] },
  { category: '面食', keywords: ['面', '粉', '馄饨', '米线'] },
  { category: '甜品', keywords: ['蛋糕', '甜品', '布丁', '芋圆'] },
  { category: '美食', keywords: ['饭', '炒菜', '盖浇', '套餐', '炸鸡', '小馆'] },
]

const ORDER_HINTS = ['ORDER', '订单', 'DELIVERY', 'PAY', '退款']
const STORE_HINTS = ['STORE', '门店', '店铺']

export function deriveStoreCategory(store: Pick<StudentHomeStore, 'storeName' | 'notice'>) {
  const text = `${store.storeName} ${store.notice ?? ''}`.toLowerCase()
  const matched = CATEGORY_RULES.find((rule) =>
    rule.keywords.some((keyword) => text.includes(keyword.toLowerCase())),
  )
  return matched?.category ?? '美食'
}

const parseDistance = (distance: string) => {
  const text = String(distance ?? '').trim().toLowerCase()
  if (text.endsWith('km')) {
    return Number(text.slice(0, -2)) * 1000
  }
  if (text.endsWith('m')) {
    return Number(text.slice(0, -1))
  }
  return Number.POSITIVE_INFINITY
}

export function filterAndSortStores(
  stores: StudentHomeStore[],
  options: {
    keyword: string
    category: string
    sortMode: StudentStoreSortMode
  },
) {
  const keyword = options.keyword.trim().toLowerCase()

  const filtered = stores.filter((store) => {
    const matchesKeyword =
      !keyword ||
      store.storeName.toLowerCase().includes(keyword) ||
      store.notice?.toLowerCase().includes(keyword) ||
      store.address.toLowerCase().includes(keyword)

    const category = deriveStoreCategory(store)
    const matchesCategory = options.category === '全部' || category === options.category

    return matchesKeyword && matchesCategory
  })

  const sorted = [...filtered]
  if (options.sortMode === 'distance') {
    sorted.sort((left, right) => parseDistance(left.distance) - parseDistance(right.distance))
  } else if (options.sortMode === 'minOrderAmount') {
    sorted.sort((left, right) => Number(left.minOrderAmount) - Number(right.minOrderAmount))
  } else if (options.sortMode === 'deliveryFee') {
    sorted.sort((left, right) => Number(left.deliveryFee) - Number(right.deliveryFee))
  }

  return sorted
}

const hasHint = (source: string | undefined, keywords: string[]) => {
  const text = String(source ?? '').toUpperCase()
  return keywords.some((keyword) => text.includes(keyword.toUpperCase()))
}

export function resolveMessageAction(message: StudentMessageResponse): StudentMessageAction | null {
  if (typeof message.bizId !== 'number' || Number.isNaN(message.bizId)) {
    return null
  }

  if (
    hasHint(message.bizType, ORDER_HINTS) ||
    hasHint(message.messageType, ORDER_HINTS) ||
    hasHint(message.title, ORDER_HINTS)
  ) {
    const orderId = String(message.bizId)
    return {
      type: 'order',
      to: {
        path: `/student/orders/${orderId}`,
      },
    }
  }

  if (
    hasHint(message.bizType, STORE_HINTS) ||
    hasHint(message.messageType, STORE_HINTS) ||
    hasHint(message.title, STORE_HINTS)
  ) {
    return {
      type: 'store',
      to: {
        path: `/student/store/${message.bizId}`,
      },
    }
  }

  return null
}

const parseQueryNumber = (value: unknown) => {
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

export function parseTargetOrderQuery(query: Record<string, unknown>) {
  return {
    expandOrderId: parseQueryNumber(query.expandOrderId),
    highlightOrderId: parseQueryNumber(query.highlightOrderId),
  }
}
