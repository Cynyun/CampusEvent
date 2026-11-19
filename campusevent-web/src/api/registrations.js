import http from './http'

export const registerForActivity = (activityId) =>
  http.post('/student/activities/' + activityId + '/registrations')
export const listMyRegistrations = () => http.get('/student/registrations')
export const cancelRegistration = (id) => http.delete('/student/registrations/' + id)

export const listAdminRegistrations = (params) => http.get('/admin/registrations', { params })
export const approveRegistration = (id) => http.post('/admin/registrations/' + id + '/approve')
export const rejectRegistration = (id) => http.post('/admin/registrations/' + id + '/reject')
