export const STUDENT_MESSAGE_EVENT = 'student-message-changed'

export function emitStudentMessageChanged() {
  if (typeof window === 'undefined') {
    return
  }
  window.dispatchEvent(new CustomEvent(STUDENT_MESSAGE_EVENT))
}
