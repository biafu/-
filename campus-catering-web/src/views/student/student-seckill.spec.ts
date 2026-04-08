import { describe, expect, it } from 'vitest'
import type { StudentSeckillActivityResponse, StudentSeckillResultResponse } from '@/api/seckill'
import {
  buildStudentSeckillActivities,
  buildStudentSeckillApplyPayload,
  getStudentSeckillResultMeta,
  shouldPollStudentSeckillResult,
} from '@/views/student/student-seckill'

const buildActivity = (overrides: Partial<StudentSeckillActivityResponse>): StudentSeckillActivityResponse =>
  ({
    id: 1,
    storeId: 1001,
    skuId: 2001,
    activityName: '午间爆品秒杀',
    seckillPrice: 9.9,
    stock: 12,
    totalStock: 20,
    startTime: '2026-04-08T12:00:00',
    endTime: '2026-04-08T13:00:00',
    status: 1,
    ...overrides,
  }) satisfies StudentSeckillActivityResponse

describe('student seckill helpers', () => {
  it('sorts live activities before upcoming ones and computes stage labels', () => {
    const rows = buildStudentSeckillActivities(
      [
        buildActivity({ id: 2, activityName: '晚餐专场', startTime: '2026-04-08T18:00:00', endTime: '2026-04-08T19:00:00' }),
        buildActivity({ id: 3, activityName: '早餐返场', startTime: '2026-04-08T08:00:00', endTime: '2026-04-08T09:00:00' }),
        buildActivity({ id: 1, activityName: '午间爆品', startTime: '2026-04-08T12:00:00', endTime: '2026-04-08T13:00:00' }),
      ],
      '2026-04-08T12:15:00',
    )

    expect(rows.map((item) => item.id)).toEqual([1, 2, 3])
    expect(rows[0]).toMatchObject({
      id: 1,
      stage: 'ongoing',
      stageLabel: '抢购中',
      progressPercent: 40,
    })
    expect(rows[1]).toMatchObject({
      id: 2,
      stage: 'upcoming',
      stageLabel: '即将开始',
      progressPercent: 40,
    })
    expect(rows[2]).toMatchObject({
      id: 3,
      stage: 'finished',
      stageLabel: '已结束',
      progressPercent: 0,
    })
  })

  it('builds a trimmed apply payload and rejects incomplete forms', () => {
    expect(
      buildStudentSeckillApplyPayload(
        {
          receiverName: '  张三  ',
          receiverPhone: ' 13800000001 ',
          receiverAddress: '  图书馆门口 ',
          remark: '  少辣 ',
        },
        99,
      ),
    ).toEqual({
      activityId: 99,
      receiverName: '张三',
      receiverPhone: '13800000001',
      receiverAddress: '图书馆门口',
      remark: '少辣',
    })

    expect(
      buildStudentSeckillApplyPayload(
        {
          receiverName: '',
          receiverPhone: '13800000001',
          receiverAddress: '图书馆门口',
          remark: '',
        },
        99,
      ),
    ).toBeNull()
  })

  it('maps result states and knows when polling should continue', () => {
    const pending = {
      requestId: 'req-1',
      status: 'PENDING',
      message: '排队中',
    } satisfies StudentSeckillResultResponse

    const success = {
      requestId: 'req-2',
      status: 'SUCCESS',
      message: '秒杀成功，请尽快支付',
      orderId: 88,
      orderNo: 202604080001,
    } satisfies StudentSeckillResultResponse

    expect(shouldPollStudentSeckillResult(pending)).toBe(true)
    expect(shouldPollStudentSeckillResult(success)).toBe(false)
    expect(getStudentSeckillResultMeta(pending)).toMatchObject({
      title: '正在排队',
      tagType: 'warning',
      canPay: false,
    })
    expect(getStudentSeckillResultMeta(success)).toMatchObject({
      title: '抢购成功',
      tagType: 'success',
      canPay: true,
      orderRoute: '/student/orders/88',
    })
  })
})
