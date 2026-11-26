export function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n) => String(n).padStart(2, '0')
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
    + ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes())
}

export function formatDate(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n) => String(n).padStart(2, '0')
  return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
}

export function statusText(status) {
  return {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    CANCELLED: '已取消',
  }[status] || status || '-'
}

export function statusType(status) {
  return {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    CANCELLED: 'info',
  }[status] || 'info'
}

export function policyText(type) {
  if (type === 'APPROVAL') return '审核制'
  if (type === 'GRADE_LIMITED_DIRECT') return '年级限定'
  return '直接报名'
}

export function policyType(type) {
  if (type === 'APPROVAL') return 'warning'
  if (type === 'GRADE_LIMITED_DIRECT') return 'primary'
  return 'success'
}

export function activityState(activity) {
  if (!activity) return '已结束'
  if (!activity.open) return '已开始'
  if (['DIRECT', 'GRADE_LIMITED_DIRECT'].includes(activity.policyType) && !activity.hasCapacity) return '已满员'
  return '报名中'
}
