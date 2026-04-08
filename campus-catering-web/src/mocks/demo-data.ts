export type StudentStore = {
  id: number
  name: string
  desc: string
  score: number
  deliveryTime: string
  deliveryFee: number
  minAmount: number
  distance: string
  tags: string[]
  monthlySales: number
  avgPrice: number
  notice: string
  cover: string
}

export type ProductItem = {
  id: number
  storeId: number
  title: string
  subtitle: string
  price: number
  monthlySold: number
  stock: number
  category: string
}

export type StudentOrder = {
  id: string
  storeName: string
  status: 'PENDING_PAYMENT' | 'PREPARING' | 'DELIVERING' | 'COMPLETED' | 'CANCELLED'
  amount: number
  createdAt: string
  items: string[]
}

export const studentStores: StudentStore[] = [
  {
    id: 1,
    name: '一食堂轻食档口',
    desc: '招牌鸡腿饭 · 校园高复购',
    score: 4.8,
    deliveryTime: '24分钟',
    deliveryFee: 2,
    minAmount: 15,
    distance: '380m',
    tags: ['减配送费', '品质联盟', '30分钟达'],
    monthlySales: 1843,
    avgPrice: 19,
    notice: '新客立减3元，晚高峰配送稍有延迟',
    cover: 'linear-gradient(145deg, #ffd86b, #ffb84d)',
  },
  {
    id: 2,
    name: '南门牛肉面馆',
    desc: '鲜熬牛骨汤 · 手工劲道面',
    score: 4.7,
    deliveryTime: '28分钟',
    deliveryFee: 3,
    minAmount: 18,
    distance: '620m',
    tags: ['夜宵友好', '满25减6', '可开发票'],
    monthlySales: 1328,
    avgPrice: 22,
    notice: '午高峰可预约，减少等待时间',
    cover: 'linear-gradient(145deg, #f4b27f, #ef8f67)',
  },
  {
    id: 3,
    name: '轻盈沙拉能量碗',
    desc: '低脂高蛋白 · 健身优选',
    score: 4.9,
    deliveryTime: '22分钟',
    deliveryFee: 1,
    minAmount: 16,
    distance: '300m',
    tags: ['轻食', '新店优惠', '低卡套餐'],
    monthlySales: 967,
    avgPrice: 25,
    notice: '工作日11:00前下单赠鲜果杯',
    cover: 'linear-gradient(145deg, #a8dfba, #73c6a4)',
  },
]

export const productItems: ProductItem[] = [
  {
    id: 101,
    storeId: 1,
    title: '黄焖鸡腿饭',
    subtitle: '档口销量第一，配时蔬和温泉蛋',
    price: 18,
    monthlySold: 826,
    stock: 96,
    category: '盖饭',
  },
  {
    id: 102,
    storeId: 1,
    title: '香辣鸡腿饭',
    subtitle: '微辣口味，学生党高人气',
    price: 16,
    monthlySold: 721,
    stock: 118,
    category: '盖饭',
  },
  {
    id: 103,
    storeId: 1,
    title: '凉面轻食套餐',
    subtitle: '清爽不腻，午间快速出餐',
    price: 14,
    monthlySold: 532,
    stock: 72,
    category: '套餐',
  },
  {
    id: 201,
    storeId: 2,
    title: '招牌牛肉拉面',
    subtitle: '6小时牛骨汤底，劲道手擀面',
    price: 19,
    monthlySold: 663,
    stock: 56,
    category: '面食',
  },
  {
    id: 301,
    storeId: 3,
    title: '牛油果藜麦能量碗',
    subtitle: '高蛋白谷物搭配新鲜蔬果',
    price: 21,
    monthlySold: 389,
    stock: 42,
    category: '轻食',
  },
]

export const studentOrders: StudentOrder[] = [
  {
    id: '202604010001',
    storeName: '一食堂轻食档口',
    status: 'DELIVERING',
    amount: 36,
    createdAt: '2026-04-01 12:08',
    items: ['黄焖鸡腿饭 x1', '凉面轻食套餐 x1'],
  },
  {
    id: '202604009873',
    storeName: '南门牛肉面馆',
    status: 'COMPLETED',
    amount: 22,
    createdAt: '2026-03-31 18:42',
    items: ['招牌牛肉拉面 x1'],
  },
  {
    id: '202603308765',
    storeName: '轻盈沙拉能量碗',
    status: 'CANCELLED',
    amount: 23,
    createdAt: '2026-03-30 11:19',
    items: ['牛油果藜麦能量碗 x1'],
  },
]

export const workspaceStats = [
  { label: '今日订单', value: '268', trend: '+12.3%' },
  { label: '今日营业额', value: '5942元', trend: '+8.7%' },
  { label: '待处理订单', value: '19', trend: '-3.2%' },
  { label: '平均出餐时长', value: '14分钟', trend: '-1.1%' },
]

export const workspaceOrders = [
  {
    id: 'W260401001',
    customer: '6号宿舍楼302',
    amount: 34,
    status: 'PREPARING',
    createdAt: '12:36',
  },
  {
    id: 'W260401002',
    customer: '图书馆南门',
    amount: 19,
    status: 'WAITING_DELIVERY',
    createdAt: '12:41',
  },
  {
    id: 'W260401003',
    customer: '9号宿舍楼118',
    amount: 27,
    status: 'COMPLETED',
    createdAt: '11:58',
  },
]
