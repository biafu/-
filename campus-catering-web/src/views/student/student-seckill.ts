import type { StudentSeckillActivityResponse, StudentSeckillApplyRequest, StudentSeckillResultResponse } from '@/api/seckill'

export type StudentSeckillApplyFormState = {
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  remark: string
}

export type StudentSeckillActivityCard = StudentSeckillActivityResponse & {
  stage: 'ongoing' | 'upcoming' | 'finished'
  stageLabel: string
  progressPercent: number
}

export function createStudentSeckillApplyFormState(): StudentSeckillApplyFormState {
  return {
    receiverName: '',
    receiverPhone: '',
    receiverAddress: '',
    remark: '',
  }
}

function toTime(raw: string) {
  return new Date(raw).getTime()
}

function resolveStage(activity: StudentSeckillActivityResponse, nowTime: number) {
  const startTime = toTime(activity.startTime)
  const endTime = toTime(activity.endTime)
  if (nowTime >= startTime && nowTime <= endTime && Number(activity.status) === 1) {
    return { stage: 'ongoing' as const, stageLabel: '抢购中' }
  }
  if (nowTime < startTime && Number(activity.status) === 1) {
    return { stage: 'upcoming' as const, stageLabel: '即将开始' }
  }
  return { stage: 'finished' as const, stageLabel: '已结束' }
}

function resolveProgressPercent(activity: StudentSeckillActivityResponse) {
  const total = Number(activity.totalStock ?? activity.stock ?? 0)
  const remaining = Number(activity.stock ?? 0)
  if (total <= 0) {
    return 0
  }
  return Math.max(0, Math.min(100, Math.round(((total - remaining) / total) * 100)))
}

function stageWeight(stage: StudentSeckillActivityCard['stage']) {
  if (stage === 'ongoing') {
    return 0
  }
  if (stage === 'upcoming') {
    return 1
  }
  return 2
}

export function buildStudentSeckillActivities(
  activities: StudentSeckillActivityResponse[],
  now = new Date().toISOString(),
): StudentSeckillActivityCard[] {
  const nowTime = toTime(now)

  return activities
    .map((activity) => {
      const stageInfo = resolveStage(activity, nowTime)
      return {
        ...activity,
        ...stageInfo,
        progressPercent: stageInfo.stage === 'finished' ? 0 : resolveProgressPercent(activity),
      }
    })
    .sort((left, right) => {
      const stageGap = stageWeight(left.stage) - stageWeight(right.stage)
      if (stageGap !== 0) {
        return stageGap
      }

      if (left.stage === 'upcoming') {
        return toTime(left.startTime) - toTime(right.startTime)
      }

      if (left.stage === 'ongoing') {
        return toTime(left.endTime) - toTime(right.endTime)
      }

      return toTime(right.endTime) - toTime(left.endTime)
    })
}

export function buildStudentSeckillApplyPayload(
  form: StudentSeckillApplyFormState,
  activityId: number,
): StudentSeckillApplyRequest | null {
  const receiverName = form.receiverName.trim()
  const receiverPhone = form.receiverPhone.trim()
  const receiverAddress = form.receiverAddress.trim()
  const remark = form.remark.trim()

  if (!activityId || !receiverName || !receiverPhone || !receiverAddress) {
    return null
  }

  return {
    activityId,
    receiverName,
    receiverPhone,
    receiverAddress,
    ...(remark ? { remark } : {}),
  }
}

export function shouldPollStudentSeckillResult(result: StudentSeckillResultResponse | null | undefined) {
  return String(result?.status || '').toUpperCase() === 'PENDING'
}

export function getStudentSeckillResultMeta(result: StudentSeckillResultResponse | null | undefined) {
  const status = String(result?.status || '').toUpperCase()
  if (status === 'SUCCESS') {
    return {
      title: '抢购成功',
      tagType: 'success' as const,
      canPay: true,
      orderRoute: result?.orderId ? `/student/orders/${result.orderId}` : '/student/orders',
    }
  }

  if (status === 'PENDING') {
    return {
      title: '正在排队',
      tagType: 'warning' as const,
      canPay: false,
      orderRoute: undefined,
    }
  }

  return {
    title: '抢购失败',
    tagType: 'danger' as const,
    canPay: false,
    orderRoute: undefined,
  }
}
