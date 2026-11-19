import http from './http'

export const listActivities = (params) => http.get('/activities', { params })
export const getActivity = (id) => http.get('/activities/' + id)

export const listAdminActivities = (params) => http.get('/admin/activities', { params })
export const getAdminActivity = (id) => http.get('/admin/activities/' + id)
export const createActivity = (data) => http.post('/admin/activities', data)
export const updateActivity = (id, data) => http.put('/admin/activities/' + id, data)
export const deleteActivity = (id) => http.delete('/admin/activities/' + id)
export const getStatistics = () => http.get('/admin/statistics')
