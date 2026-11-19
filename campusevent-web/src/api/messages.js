import http from './http'

export const listMessages = () => http.get('/student/messages')
export const markMessageRead = (id) => http.post('/student/messages/' + id + '/read')
export const markAllMessagesRead = () => http.post('/student/messages/read-all')
export const getUnreadCount = () => http.get('/student/messages/unread-count')
