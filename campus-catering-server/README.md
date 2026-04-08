# Campus Catering Server

校园餐饮管理系统后端首版实现，基于 `Spring Boot + MyBatis + MySQL + Redis + WebSocket + JWT`。

## 已落地能力

- 认证与鉴权：学生、商户、管理员登录/登出，JWT 鉴权与令牌黑名单失效，预留 API 签名校验
- 商户入驻：提交申请、平台审核、自动初始化商户账号和门店
- 门店管理：商户查看/更新门店信息、配置营业时间，平台启停门店
- 商品管理：分类新增、单品/套餐新增（含套餐明细）、上下架
- 学生侧：门店列表、门店详情、菜品列表、购物车、订单预览、下单、模拟支付、取消订单
- 商户侧：订单列表、接单、拒单、备餐、出餐
- 平台侧：审核入驻、配送调度、完成配送、仪表盘统计
- 秒杀能力：Redis 预扣减库存、BlockingQueue 异步削峰、一人一单、防重复下单、秒杀结果轮询
- 过程留痕：订单日志、支付记录、日报统计
- 营销能力：优惠券领券、可用券查询、下单预览抵扣、下单锁券、取消返券
- 性能方案：Redis 缓存、空值缓存、互斥锁防击穿、Redis 全局订单号、WebSocket 来单提醒、超时订单定时取消、商户接单超时预警、配送超时预警、每日自动汇总统计、缓存预热任务

## 目录

```text
campus-catering-server/
├─ src/main/java/com/zhinengpt/campuscatering
├─ src/main/resources/application.yml
└─ src/main/resources/db/
   ├─ schema.sql
   └─ seed-data.sql
```

## 初始化

1. 创建数据库：`campus_catering`
2. 依次执行：
   `src/main/resources/db/schema.sql`
   `src/main/resources/db/seed-data.sql`
3. 修改 `application.yml` 中的 MySQL 和 Redis 连接信息

说明：
如果你之前已经执行过旧版 SQL，这次需要重新补执行最新的 [schema.sql](C:/Users/Administrator/Desktop/zhinengpt/campus-catering-server/src/main/resources/db/schema.sql)，因为新增了 `store_business_hours`、`combo`、`combo_item`、`seckill_activity`、`seckill_order`、`order_log`、`payment_record`、`daily_statistics`、`coupon`、`user_coupon` 等表。

## 默认账号

- 学生：`13800000001 / 123456`
- 商户：`merchant01 / 123456`
- 管理员：`admin / 123456`

## 关键接口

- `POST /api/auth/student/login`
- `POST /api/auth/merchant/login`
- `POST /api/auth/admin/login`
- `POST /api/auth/logout`
- `GET /api/student/store/list`
- `GET /api/student/product/list?storeId=1`
- `POST /api/student/cart/add`
- `POST /api/student/order/preview`
- `POST /api/student/order/create`
- `POST /api/student/order/pay/{orderId}`
- `GET /api/student/order/logs/{orderId}`
- `GET /api/student/coupon/center?storeId=1`
- `POST /api/student/coupon/claim/{couponId}`
- `GET /api/student/coupon/my?storeId=1`
- `GET /api/merchant/store/business-hours`
- `PUT /api/merchant/store/business-hours`
- `POST /api/merchant/product/save`（支持 `productType=2` + `comboItems` 创建套餐）
- `GET /api/student/seckill/activity/list?storeId=1`
- `POST /api/student/seckill/apply`
- `GET /api/student/seckill/result/{requestId}`
- `POST /api/merchant/order/accept/{orderId}`
- `GET /api/merchant/order/logs/{orderId}`
- `POST /api/admin/seckill/activity/save`
- `POST /api/admin/delivery/dispatch`
- `GET /api/admin/statistics/daily?days=7`
- `POST /api/admin/statistics/daily/rebuild?statDate=2026-04-01`
- `GET /api/admin/statistics/merchant-rank?days=7&limit=10`

## WebSocket

- 地址：`/ws/orders?token=<merchant-jwt>`
- 商户支付成功后会收到新订单提醒

## 秒杀测试建议

1. 学生登录拿到 JWT
2. 调用 `GET /api/student/seckill/activity/list?storeId=1`
3. 调用 `POST /api/student/seckill/apply`
4. 轮询 `GET /api/student/seckill/result/{requestId}`
5. 成功后调用 `POST /api/student/order/pay/{orderId}`

## 说明

当前工作区缺少本地 Java 运行环境，我已经把工程、SQL 和接口骨架补齐，但没有办法在当前终端里执行 `mvn test` 或直接启动服务。等你本机配置好 `JDK 17 + Maven` 后，可以直接继续联调。
